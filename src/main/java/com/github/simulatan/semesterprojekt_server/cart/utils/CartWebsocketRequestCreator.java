package com.github.simulatan.semesterprojekt_server.cart.utils;

import com.github.simulatan.semesterprojekt_server.cart.objects.Cart;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.util.UUID;

@ApplicationScoped
public class CartWebsocketRequestCreator {

	public CartWebsocketRequest create(Session session, String message) {
		UUID userId = (UUID) session.getUserProperties().get("user_id");
		JSONObject request = new JSONObject(message);
		Cart cart = (Cart) Cart.find("userId", userId)
			.firstResult()
			.await()
			.indefinitely();
		return new CartWebsocketRequest(session, message, request, cart != null ? cart : createCart(userId), request.has("request_id") ? request.getLong("request_id") : -1);
	}

	private Cart createCart(UUID userId) {
		Cart result = new Cart();
		result.userId = userId;
		return (Cart) result.persist().await().indefinitely();
	}
}