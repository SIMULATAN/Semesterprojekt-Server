package com.github.simulatan.semesterprojekt_server.cart;

import com.github.simulatan.semesterprojekt_server.cart.utils.CartWebsocketRequest;
import com.github.simulatan.semesterprojekt_server.cart.utils.CartWebsocketRequestCreator;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.websocket.*;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
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

	@Inject
	CartWebsocketRequestCreator creator;

	@Inject
	CartManager manager;

	@OnOpen
	public void onOpen(Session session) {
		sessions.computeIfAbsent((UUID) session.getUserProperties().get("user_id"), k -> new ArrayList<>()).add(session);
	}

	@OnMessage
	@Transactional
	public void onMessage(Session session, String message) {
		LOGGER.info("Message received from " + session.getUserProperties().get("user_id"));
		JSONObject json = new JSONObject(message);
		CartWebsocketRequest context = creator.create(session, message);
		if (json.has("action")) {
			String action = json.getString("action");
			if (action.equals("clear")) {
				manager.clearCart(context);
				return;
			} else if (action.equals("list")) {
				manager.listCart(context);
				return;
			}

			switch (action) {
				case "remove" -> manager.removeFromCart(context, json.getLong("cart_item_id"));
				case "update_quantity" -> manager.updateQuantity(context, json.getInt("new_quantity"), json.getLong("cart_item_id"));
				default -> context.error(404, "Unknown Action");
			}
		}
	}

	@OnError
	public void error(Session session, Throwable exception) {
		LOGGER.error("WebSocket error", exception);
		session.getAsyncRemote().sendText("{\"error\":true,\"class\":\"" + exception.getClass().getName() + "\",\"message\":\"" + exception.getMessage() + "\"}");
	}

	public static void broadcast(UUID userId, String message) {
		sessions.get(userId).forEach(s -> s.getAsyncRemote().sendText(message));
	}
}