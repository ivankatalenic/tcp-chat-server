package chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;

/**
 * Class responsible for handling the client socket after it was connected.
 * 
 * @author Ivan Katalenić
 * 
 */
public class ClientHandler extends Thread {

	static final int INPUT_BUFFER = 1024;

	Server server;
	Socket client;
	MessageHandler messageHandler;
	MessageDistributer msgDist;

	DataInputStream input;
	DataOutputStream output;

	String username;

	public ClientHandler(Server server, Socket client, MessageHandler messageHandler, MessageDistributer msgDist) {
		this.server = server;
		this.client = client;
		this.messageHandler = messageHandler;
		this.msgDist = msgDist;
	}

	/**
	 * Checks if the provided username is not the name of important message
	 * category.
	 * 
	 * @param username Username to be checked.
	 * @return Quoted username if the username is the same as the name of some
	 *         important message category, or else the same username as provided.
	 */
	public static String checkUsername(String username) {
		String newUsername;

		switch (username) {
		case "SERVER":
		case "ERROR":
		case "WARNING":
		case "INFO":
			newUsername = "\"" + username + "\"";
			break;
		default:
			newUsername = username;
			break;
		}

		return newUsername;
	}

	@Override
	public void run() {
		try {
			input = new DataInputStream(client.getInputStream());
			output = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			try {
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
			}
			messageHandler.displayMessage("INFO", "Client with IP address " + client.getInetAddress().toString()
					+ " has failed to properly connect!");

			return;
		}

		String msg = null;

		try {
			msg = input.readUTF();
		} catch (IOException e) {
			try {
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
			}
			messageHandler.displayMessage("INFO", "Client with IP address " + client.getInetAddress().toString()
					+ " has failed to properly connect!");

			return;
		} catch (Exception other) {
			try {
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
			}
			messageHandler.displayMessage("INFO", "Client with IP address " + client.getInetAddress().toString()
					+ " has failed to properly connect!");

			return;
		}

		username = checkUsername(msg);

		// Check if user with the same username is connected to the server.
		if (server.clientUsernames.contains((Object) username)) {
			try {
				output.writeUTF(Message.construct("SERVER",
						"Client with the same username (" + username + ") is already connected to the chat!"));
				try {
					client.close();
				} catch (IOException io) {

				}
			} catch (IOException e) {

			}

			messageHandler.displayMessage("INFO", "Client with IP address " + client.getInetAddress().toString()
					+ " has failed to properly connect!");

			return;
		}

		server.connectedClients.add(client);
		server.clientUsernames.add(username);

		// Welcome message.
		try {
			msgDist.putMessage("INFO", username + " has connected to chat server!");
		} catch (InterruptedException e3) {
			// TODO Auto-generated catch block
		}

		messageHandler.displayMessage("INFO", "Client " + username + " has connected from " + client.getInetAddress());

		// Main loop where the messages from the client are being read and sent to
		// message distributer.
		while (true) {
			try {
				msg = input.readUTF();
			} catch (IOException e) {
				try {
					client.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
				}

				if (!server.stopping) {
					try {
						msgDist.putMessage("SERVER", username + " has disconnected from chat server!");
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
					}

					server.connectedClients.remove(client);
					server.clientUsernames.remove(username);
				}

				return;
			}

			try {
				msgDist.putMessage(username, msg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
			}
		}
	}
}
