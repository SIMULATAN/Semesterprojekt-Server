package com.github.simulatan.semesterprojekt_server.cart;

import com.github.simulatan.semesterprojekt_server.cart.objects.Cart;
import com.github.simulatan.semesterprojekt_server.cart.utils.CartWebsocketRequest;
import com.github.simulatan.semesterprojekt_server.cart.utils.CartWebsocketRequestCreator;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.json.JSONObject;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.websocket.HandshakeResponse;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
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

	@Inject
	CartWebsocketRequestCreator creator;

	@Inject
	CartManager manager;

	@OnMessage
	@Transactional
	public void onMessage(Session session, String message) {
		LOGGER.info("Message received from " + session.getUserProperties().get("user_id"));
		JSONObject json = new JSONObject(message);
		Uni<CartWebsocketRequest> cartWebsocketRequestUni = creator.create(session, message);
		cartWebsocketRequestUni.invoke(context -> {
			System.out.println("CONTEXT: " + context);
			if (json.has("action")) {
				String action = json.getString("action");
				if (action.equals("clear")) {
					manager.clearCart(context);
					return;
				}

				if (action.equals("add")) {
					manager.addToCart(context, json.getLong("product_id"));
				} else if (action.equals("remove")) {
					manager.removeFromCart(context, json.getLong("product_id"));
				} else {
					context.error(404, "Unknown Action");
				}
			}
		});
	}

	@ActivateRequestContext
	private void doShit() {
		System.out.println(Cart.findAll().firstResult().await().indefinitely());
	}

	@OnError
	public void error(Session session, Throwable exception) throws IOException {
		LOGGER.error("WebSocket error", exception);
		// session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, exception.getMessage()));
	}

	public static void broadcast(UUID userId, String message) {
		sessions.get(userId).forEach(s -> s.getAsyncRemote().sendText(message));
	}
}