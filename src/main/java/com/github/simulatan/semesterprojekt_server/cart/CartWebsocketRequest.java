package com.github.simulatan.semesterprojekt_server.cart;

import com.github.simulatan.semesterprojekt_server.cart.objects.Cart;
import io.smallrye.mutiny.Uni;
import org.json.JSONObject;

import javax.websocket.Session;
import java.util.UUID;

public class CartWebsocketRequest {
	public final Session session;
	public final UUID userId;
	public final String message;
	public final JSONObject request;
	public final long requestId;
	public final Cart cart;
	public final CartWebsocket.IBroadcastMessage broadcast;

	private CartWebsocketRequest(Session session, String message, JSONObject request, Cart cart, long requestId, CartWebsocket.IBroadcastMessage broadcast) {
		this.userId = cart.userId;
		this.session = session;
		this.message = message;
		this.request = request;
		this.requestId = requestId;
		this.cart = cart;
		this.broadcast = broadcast;
	}

	public static Uni<CartWebsocketRequest> create(Session session, String message, CartWebsocket.IBroadcastMessage broadcast) {
		UUID userId = UUID.fromString((String) session.getUserProperties().get("user_id"));
		JSONObject request = new JSONObject(message);
		return Cart.find("userId", userId).firstResult()
			.map(Cart.class::cast)
			.map(cart -> new CartWebsocketRequest(session, message, request, cart, request.getLong("request_id"), broadcast));
	}

	public void error(int httpCode, String message) {
		send("""
			{
			  "code": %d
			  "message": %s
			}""", httpCode, message);
	}

	public void send(String format, Object... args) {
		send(String.format(format, args));
	}

	public void send(String message) {
		broadcast("{\"request_id\":" + requestId + "," + message + "}");
	}

	public void send(CartResponseCode code) {
		broadcast("{\"request_id\":" + requestId + ",\"code\":" + code. getCode() + ",\"message:\":\"" + code.getMessage() + "\"}");
	}

	private void broadcast(String message) {
		broadcast.broadcast(userId, message);
	}
}