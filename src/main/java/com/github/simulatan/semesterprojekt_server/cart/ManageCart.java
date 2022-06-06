package com.github.simulatan.semesterprojekt_server.cart;

import io.quarkus.security.identity.SecurityIdentity;

import javax.inject.Inject;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/api/cart/ws")
public class ManageCart {
	@Inject
	SecurityIdentity identity;

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("Session connected.");
	}
}
