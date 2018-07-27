import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {

	static final int INPUT_BUFFER = 1024;

	Server server;
	Socket client;
	GUI gui;
	MessageDistributer msgDist;

	DataInputStream input;
	DataOutputStream output;
	
	String username;

	public ClientHandler(Server server, Socket client, GUI gui, MessageDistributer msgDist) {
		this.server = server;
		this.client = client;
		this.gui = gui;
		this.msgDist = msgDist;
	}

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
			gui.addMessage(Message.construct("INFO", "Client has failed to properly connect!"));

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
			gui.addMessage(Message.construct("INFO", "Client has failed to properly connect!"));

			return;
		} catch (Exception other) {
			// TODO Print appropriate error message!
			System.exit(-1);
		}

		server.connectedClients.add(client);
		username = checkUsername(msg);
		
		// Check if user with the same username is connected to the server.
		if (server.connectedClients.contains((Object) username)) {
			try {
				output.writeUTF(
						Message.construct("SERVER", "Client with the same username (" + username + ") is already connected to the chat!"));
				try {
					client.close();
				} catch (IOException io) {

				}
				gui.addMessage(Message.construct("INFO", "Client has failed to properly connect!"));
			} catch (IOException e) {

			}

			return;
		}
		
		// Greet with welcome message.
		try {
			msgDist.putMessage("INFO", username + " has connected to chat server!");
		} catch (InterruptedException e3) {
			// TODO Auto-generated catch block
		}

		gui.addMessage(Message.construct("INFO", "Client " + username + " has connected from " + client.getInetAddress()));

		while (true) {
			try {
				msg = input.readUTF();
			} catch (IOException e) {
				try {
					client.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
				}
				try {
					msgDist.putMessage("SERVER", username + " has disconnected from chat server!");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
				}
				server.connectedClients.remove(client);
				
				return;
			}

			try {
				msgDist.putMessage(username, msg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
