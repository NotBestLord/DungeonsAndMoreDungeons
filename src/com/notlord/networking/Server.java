package com.notlord.networking;

import com.google.gson.Gson;
import com.notlord.networking.listeners.ServerListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
	private final String separatorId = UUID.randomUUID() + "-sepId";
	private static final Gson gson = new Gson();
	private volatile boolean running = false;
	private ServerSocket socket;
	private int port;
	private final List<ServerListener> listeners = new ArrayList<>();
	private final List<ClientInstance> clients = new CopyOnWriteArrayList<>();
	private int id = 0;

	/**
	 * creates a server.
	 * @param port port the server listens to
	 */
	public Server(int port) {
		this.port = port;
	}

	/**
	 * set port of server
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * adds a listener to the server.
	 * @param l listener to add
	 */
	public void addListener(ServerListener l){
		listeners.add(l);
	}
	private void initialize() throws IOException {
		socket = new ServerSocket(port);
		running = true;
	}

	/**
	 * starts the server.
	 */
	public void start(){
		if(!running) {
			running = true;
			new Thread(this::serverClientConnectionHandle,"Server").start();
		}
	}

	/**
	 * stops the server and closes all clients.
	 */
	public void close() {
		if(running) {
			running = false;
			try {
				for (ClientInstance client : clients) {
					client.close();
				}
				clients.clear();
				socket.close();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	private void serverClientConnectionHandle() {
		try {
			initialize();
		} catch (IOException e) {
			running = false;
			e.printStackTrace();
		}
		while (running) {
			try {
				clientConnect(new ClientInstance(this, socket.accept(), id));
				id++;
			} catch (IOException e) {
				if (!e.getMessage().equals("Socket closed"))
					e.printStackTrace();
				break;
			}
		}
		listeners.forEach(ServerListener::serverClose);
	}

	protected void clientConnect(ClientInstance clientSocket) throws IOException{
		clientSocket.writer.println(separatorId);
		clientSocket.start();
		clients.add(clientSocket);
		listeners.forEach((listener -> listener.clientConnect(clientSocket)));
	}

	protected void clientDisconnect(ClientInstance clientSocket){
		listeners.forEach((listener -> listener.clientDisconnect(clientSocket)));
		clients.remove(clientSocket);
	}

	protected void clientInput(ClientInstance clientSocket, Object o){
		listeners.forEach((listener -> listener.clientReceive(clientSocket, o)));
	}

	/**
	 * sends a packet to all client instances.
	 * @param o the packet
	 */
	public void sendAll(Object o){
		clients.forEach(clientInstance -> clientInstance.send(o));
	}

	/**
	 * sends a packet to all client instances but the ones specified.
	 * @param o the packet
	 * @param excludedClients the client instances that should not receive the packet.
	 */
	public void sendAllExclude(Object o, IClientInstance... excludedClients){
		List<IClientInstance> excludedInstances = new ArrayList<>(List.of(excludedClients));
		clients.forEach(clientInstance -> {
			if(!excludedInstances.contains(clientInstance)){
				clientInstance.send(o);
			}
		});
	}

	/**
	 * returns if the server is running.
	 */
	public boolean isRunning(){
		return running;
	}

	public static class ClientInstance extends Thread implements IClientInstance{
		private final Server parentServer;
		private final Socket socket;
		private final PrintWriter writer;
		private final BufferedReader reader;
		private final int id;
		private boolean running = true;
		/**
		 * an instance of a client, on the server side.
		 * @param parentServer the server the client instance is tied to.
		 * @param socket the socket of the instance.
		 * @param id the id of the instance.
		 * @throws IOException thrown when an error with creating an input/output stream occurs.
		 */
		protected ClientInstance(Server parentServer, Socket socket, int id) throws IOException {
			this.id = id;
			this.parentServer = parentServer;
			this.socket = socket;
			writer = new PrintWriter(socket.getOutputStream(),true);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}

		@Override
		public void run() {
			clientRunHandle();
		}

		private void clientRunHandle() {
			String inputLine;
			while (!socket.isClosed()){
				try {
					inputLine = reader.readLine();
				}
				catch (Exception e) {
					if(e.getMessage() != null && !e.getMessage().equals("Connection reset") && !e.getMessage().equals("Socket closed")){
						e.printStackTrace();
					}
					break;
				}
				if(inputLine == null)  break;
				try {
					Object o = objectFromString(inputLine);
					parentServer.clientInput(this, o);
				} catch (Exception ignored) {}
			}
			parentServer.clientDisconnect(this);
			close();
		}

		/**
		 * send a packet to the client the instance is connected to.
		 * can send any object.
		 */
		public void send(Object o){
			writer.println(objectToString(o));
		}

		public String objectToString(Object o){
			return (gson.toJson(o) + parentServer.separatorId + o.getClass().toString().split(" ")[1]);
		}

		public Object objectFromString(String input) throws Exception {
			return gson.fromJson(input.split(parentServer.separatorId)[0], Class.forName(input.split(parentServer.separatorId)[1]));
		}

		public void close() {
			if(running) {
				try {
					writer.close();
					reader.close();
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				running = false;
			}
		}

		/**
		 * get id of the instance.
		 * every instance has a unique id.
		 */
		public int getID(){
			return id;
		}
	}
}
