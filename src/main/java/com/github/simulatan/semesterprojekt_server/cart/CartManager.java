package com.github.simulatan.semesterprojekt_server.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.simulatan.semesterprojekt_server.cart.objects.CartItem;
import com.github.simulatan.semesterprojekt_server.cart.utils.CartWebsocketRequest;
import com.github.simulatan.semesterprojekt_server.product.objects.Product;
import io.quarkus.arc.Arc;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import net.jodah.expiringmap.ExpiringMap;
import org.jose4j.jwt.MalformedClaimException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Path("/api/cart")
public class CartManager {

	@Inject
	SecurityIdentity identity;

	@Inject
	ObjectMapper mapper;

	private static final Map<String, UUID> cartIdToUserId = ExpiringMap.builder()
		.expiration(30, TimeUnit.SECONDS)
		.build();

	/**
	 * Retrieves the userId of the cart with the given id AND DELETES THE ENTRY
	 */
	public static UUID retrieveUserId(String cartId) {
		return cartIdToUserId.remove(cartId);
	}

	@POST
	@Authenticated
	public String createSession() throws MalformedClaimException {
		String cartId = generateCartId();
		cartIdToUserId.put(cartId, UUID.fromString(((OidcJwtCallerPrincipal) identity.getPrincipal()).getClaims().getSubject()));
		return cartId;
	}

	private String generateCartId() {
		String cartId = UUID.randomUUID().toString();
		while (cartIdToUserId.containsKey(cartId)) {
			cartId = UUID.randomUUID().toString();
		}
		return cartId;
	}

	@Transactional
	public void clearCart(CartWebsocketRequest context) {
		UUID userId = context.userId;
		if (userId == null) {
			context.error(401, "No user found");
			return;
		}
		CartItem.delete("cart_id", context.cart.id).await().indefinitely();
		context.send("clear", CartResponseCode.CART_CLEARED);
	}

	@Transactional
	public void addToCart(CartWebsocketRequest session, long productId) {
		UUID userId = session.userId;
		if (userId == null) {
			session.error(401, "No user found");
			return;
		}
		Product product = (Product) Product.findById(productId).await().indefinitely();
		if (product == null) {
			session.send("add", CartResponseCode.PRODUCT_NOT_FOUND);
			return;
		}
		CartItem item = new CartItem(session.cart, product, session.request.has("amount") ? session.request.getInt("amount") : 1);
		var result = Panache.withTransaction(item::persist).await().indefinitely();
		try {
			session.send("add", Arc.container()
				.instance(ObjectMapper.class)
				.get()
				.writeValueAsString(result));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeFromCart(CartWebsocketRequest session, long cartItemId) {
		UUID userId = session.userId;
		if (userId == null) {
			session.error(401, "No user found");
			return;
		}
		CartItem item = (CartItem) CartItem.find("id", cartItemId).firstResult().await().indefinitely();
		if (item == null || !item.cart.id.equals(session.cart.id)) {
			session.send("remove", CartResponseCode.CART_ITEM_NOT_FOUND);
			return;
		}
		var result = Panache.withTransaction(() -> CartItem.deleteById(cartItemId)).await().indefinitely();
		session.send("remove", Boolean.TRUE.equals(result) ? CartResponseCode.CART_ITEM_REMOVED : CartResponseCode.CART_ITEM_NOT_REMOVED);
	}

	public void listCart(CartWebsocketRequest session) {
		UUID userId = session.userId;
		if (userId == null) {
			session.error(401, "No user found");
			return;
		}
		try {
			session.send("list", mapper.writeValueAsString(CartItem.list("cart_id", session.cart.id).await().indefinitely()));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void updateQuantity(CartWebsocketRequest context, int newQuantity, long cartItemId) {
		UUID userId = context.userId;
		if (userId == null) {
			context.error(401, "No user found");
			return;
		}
		var result = Panache.withTransaction(() -> CartItem.update("quantity = ?1 where id = ?2 and cart_id = ?3", newQuantity, cartItemId, context.cart.id)).await().indefinitely();
		context.send("update_quantity", result > 0 ? CartResponseCode.CART_ITEM_UPDATED : CartResponseCode.CART_ITEM_NOT_UPDATED);
	}
}