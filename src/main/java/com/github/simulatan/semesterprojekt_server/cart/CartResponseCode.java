package com.github.simulatan.semesterprojekt_server.cart;

public enum CartResponseCode {
	CART_CLEARED("Cart Cleared!", 10),
	CART_ITEM_REMOVED("Cart Item removed.", 20),
	CART_ITEM_NOT_REMOVED("Cart iteme not found.", 21);

	CartResponseCode(int code) {
		this(null, code);
	}

	CartResponseCode(String message, int code) {
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