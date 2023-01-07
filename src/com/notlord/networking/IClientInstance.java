package com.notlord.networking;

public interface IClientInstance {
	void close();
	void send(Object o);
	int getID();
}
