package com.github.simulatan.semesterprojekt_server.cart.utils;

import com.github.simulatan.semesterprojekt_server.cart.CartResponseCode;
import com.github.simulatan.semesterprojekt_server.cart.CartWebsocket;
import com.github.simulatan.semesterprojekt_server.cart.objects.Cart;
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

	CartWebsocketRequest(Session session, String message, JSONObject request, Cart cart, long requestId) {
		this.userId = cart.userId;
		this.session = session;
		this.message = message;
		this.request = request;
		this.requestId = requestId;
		this.cart = cart;
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
		CartWebsocket.broadcast(userId, message);
	}
}