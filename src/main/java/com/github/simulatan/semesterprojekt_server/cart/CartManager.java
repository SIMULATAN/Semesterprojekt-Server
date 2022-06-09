package com.github.simulatan.semesterprojekt_server.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.simulatan.semesterprojekt_server.cart.objects.Cart;
import com.github.simulatan.semesterprojekt_server.cart.objects.CartItem;
import com.github.simulatan.semesterprojekt_server.cart.utils.CartWebsocketRequest;
import com.github.simulatan.semesterprojekt_server.product.objects.Product;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import net.jodah.expiringmap.ExpiringMap;
import org.jboss.resteasy.reactive.RestQuery;
import org.jose4j.jwt.MalformedClaimException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
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

	@POST
	@Path("/add")
	@Transactional
	@Authenticated
	public Uni<Response> addToCart(@RestQuery("product_id") long productId, @RestQuery @DefaultValue("1") int amount) throws MalformedClaimException {
		if (productId == 0) throw new NotFoundException("Product not found");
		UUID userId = UUID.fromString(((OidcJwtCallerPrincipal) identity.getPrincipal()).getClaims().getSubject());
		return Product.findById(productId)
			.onItem().ifNull().failWith(() -> new NotFoundException("Product not found"))
			.onItem().transformToUni(product -> Cart.find("userId", userId)
				.firstResult()
				.onItem().ifNull().switchTo(() -> createCart(userId))
				.onItem().transformToUni(cart -> Panache.withTransaction(new CartItem((Cart) cart, (Product) product, amount)::persist))
				.map(result -> Unchecked.supplier(() -> mapper.writeValueAsString(result)).get())
				.invoke(m -> CartWebsocket.broadcast(userId, "{\"action\":\"add\"" + ",\"data\":" + m + "}"))
				.map(r -> Response.ok(r).build())
			);
	}

	private Uni<Cart> createCart(UUID userId) {
		Cart result = new Cart();
		result.userId = userId;
		return result.persist();
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
		if (newQuantity < 0) throw new IllegalArgumentException("Quantity must be positive");
		if (newQuantity == 0) {
			removeFromCart(context, cartItemId);
			return;
		}
		UUID userId = context.userId;
		if (userId == null) {
			context.error(401, "No user found");
			return;
		}
		var result = Panache.withTransaction(() -> CartItem.update("quantity = ?1 where id = ?2 and cart_id = ?3", newQuantity, cartItemId, context.cart.id)).await().indefinitely();
		if (result > 0)
			context.send("update_quantity",
				// work around stupid checked exceptions
				Unchecked.supplier(() -> mapper.writeValueAsString(CartItem.findById(cartItemId).await().indefinitely())).get()
			);
	}
}