package com.github.simulatan.semesterprojekt_server.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.simulatan.semesterprojekt_server.cart.objects.CartItem;
import com.github.simulatan.semesterprojekt_server.cart.utils.CartWebsocketRequest;
import com.github.simulatan.semesterprojekt_server.product.objects.Product;
import io.quarkus.arc.Arc;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.unchecked.Unchecked;
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
		CartItem.delete("cart_id", context.cart.id)
			.onItem().invoke(item -> context.send(CartResponseCode.CART_CLEARED));
	}

	@Transactional
	public void addToCart(CartWebsocketRequest session, long productId) {
		UUID userId = session.userId;
		if (userId == null) {
			session.error(401, "No user found");
			return;
		}
		Product.findById(productId).onItem()
			.ifNull().failWith(() -> new IllegalArgumentException("Product with id " + productId + " does not exist"))
			.map(Product.class::cast)
			.map(product -> new CartItem(session.cart, product, session.request.has("amount") ? session.request.getInt("amount") : 1))
			.onItem().transformToUni(c -> c.persist())
			.map(CartItem.class::cast)
			.onItem().ifNotNull().invoke(Unchecked.consumer(o -> session.send(Arc.container().instance(ObjectMapper.class).get().writeValueAsString(o))));
	}

	@Transactional
	public void removeFromCart(CartWebsocketRequest session, long productId) {
		UUID userId = session.userId;
		if (userId == null) {
			session.error(401, "No user found");
			return;
		}
		CartItem.deleteById(productId)
			.onItem().ifNotNull().invoke(Unchecked.consumer(o -> session.send(o ? CartResponseCode.CART_ITEM_REMOVED : CartResponseCode.CART_ITEM_NOT_REMOVED)));
	}
}