package com.notlord.networking.listeners;

import com.notlord.networking.IClientInstance;

public interface ServerListener {
	void clientConnect(IClientInstance client);
	void clientReceive(IClientInstance client, Object o);
	void clientDisconnect(IClientInstance client);
	void serverClose();
}
