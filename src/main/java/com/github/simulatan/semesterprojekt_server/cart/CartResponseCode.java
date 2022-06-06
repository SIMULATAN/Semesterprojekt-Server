package com.github.simulatan.semesterprojekt_server.cart;

public enum CartResponseCode {
	CART_CLEARED("Cart Cleared!", 10);

	CartResponseCode(int code) {
		this.code = code;
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