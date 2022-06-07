package com.github.simulatan.semesterprojekt_server.cart.utils;

import com.github.simulatan.semesterprojekt_server.cart.objects.Cart;
import io.smallrye.mutiny.Uni;
import org.json.JSONObject;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.util.UUID;

@ApplicationScoped
public class CartWebsocketRequestCreator {

	public Uni<CartWebsocketRequest> create(Session session, String message) {
		UUID userId = (UUID) session.getUserProperties().get("user_id");
		JSONObject request = new JSONObject(message);
		new Thread(() -> {
			Cart.findAll().firstResult().await().indefinitely();
		}).start();
		try {
			return Cart.find("userId", userId)
				.firstResult()
				.onItem().failWith(() -> new IllegalArgumentException("No cart found for user " + userId))
				.map(Cart.class::cast)
				.map(cart -> {
					try {
						System.out.println("b4");
						CartWebsocketRequest req = new CartWebsocketRequest(session, message, request, cart, request.getLong("request_id"));
						System.out.println("AYOOO: " + req);
						return req;
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}