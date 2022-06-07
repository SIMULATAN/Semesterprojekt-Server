package com.github.simulatan.semesterprojekt_server.cart;

public enum CartResponseCode {
	CART_ITEM_NOT_FOUND(5),
	CART_CLEARED("Cart Cleared!", 10),
	NOT_YOUR_CART("Not your cart!", 20),
	CART_ITEM_REMOVED("Cart Item removed.", 20),
	CART_ITEM_NOT_REMOVED("Cart item not found.", 21),
	PRODUCT_NOT_FOUND("Product not found.", 30),
	CART_ITEM_UPDATED("Cart item updated.", 41),
	CART_ITEM_NOT_UPDATED("Cart item not updated.", 42);

	CartResponseCode(int code) {
		this(null, code);
	}

	CartResponseCode(String message, int code) {
		if (message == null) message = this.name().toLowerCase().replace("_", " ");
		this.message = message;
		this.code = code;
	}

	private final String message;
	private final int code;

	public String getMessage() {
		return message;
	}

	public int getCode() {
		return code;
	}
}