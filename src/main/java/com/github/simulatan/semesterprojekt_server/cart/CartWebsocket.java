package com.github.simulatan.semesterprojekt_server.cart;

import org.jboss.logging.Logger;
import org.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.*;

@ServerEndpoint(value = "/api/cart/ws/{cart_id}", configurator = CartWebsocket.CartConfigurator.class)
public class CartWebsocket {

	private static final Logger LOGGER = Logger.getLogger(CartWebsocket.class);

	private static final Map<UUID, List<Session>> sessions = Collections.synchronizedMap(new HashMap<>());


	public static class CartConfigurator extends ServerEndpointConfig.Configurator {
		@Override
		public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
			super.modifyHandshake(sec, request, response);
			List<String> cartId = request.getParameterMap().get("cart_id");
			if (cartId == null || cartId.isEmpty()) {
				terminate(response);
				return;
			}
			UUID userId = CartManager.retrieveUserId(cartId.get(0));
			if (userId == null) {
				terminate(response);
				return;
			}
			sec.getUserProperties().put("cart_id", cartId.get(0));
			sec.getUserProperties().put("user_id", userId);
		}

		private static void terminate(HandshakeResponse response) {
			response.getHeaders().put(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, Collections.emptyList());
		}
	}

	@OnMessage
	public void onMessage(Session session, String message) {
		LOGGER.info("Message received from " + session.getUserProperties().get("user_id"));
		JSONObject json = new JSONObject(message);
		CartWebsocketRequest.create(session, message, broadcastMessage).invoke(context -> {
			if (json.has("action")) {
				String action = json.getString("action");
				if (action.equals("clear")) {
					CartManager.clearCart(context);
					return;
				}

				if (action.equals("add")) {
					CartManager.addToCart(context, json.getLong("product_id"));
				} else if (action.equals("remove")) {
					CartManager.removeFromCart(context, json.getString("product_id"));
				} else {
					context.error(404, "Unknown Action");
				}
			}
		});
	}

	@OnError
	public void error(Session session, Throwable exception) throws IOException {
		LOGGER.error("WebSocket error", exception);
		session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, exception.getMessage()));
	}

	interface IBroadcastMessage {
		void broadcast(UUID userId, String message);
	}

	private static final IBroadcastMessage broadcastMessage = (userId, message) -> sessions.get(userId).forEach(s -> s.getAsyncRemote().sendText(message));
}