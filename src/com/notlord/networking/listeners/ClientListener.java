package com.notlord.networking.listeners;

public interface ClientListener {
	void connect();
	void disconnect();
	void receive(Object o);
}
