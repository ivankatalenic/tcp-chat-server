import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {

	static final int INPUT_BUFFER = 1024;
	
	Server server;
	Socket client;
	GUI gui;
	MessageDistributer msgDist;
	
	DataInputStream input;

	public ClientHandler(Server server, Socket client, GUI gui, MessageDistributer msgDist) {
		this.server = server;
		this.client = client;
		this.gui = gui;
		this.msgDist = msgDist;
	}
	
	public static String checkUsername(String username) {
		String newUsername;
		switch (username) {
		case "SERVER": case "ERROR": case "WARNING": case "INFO":
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
		server.nameToIP.put(client.getInetAddress().toString(), checkUsername(msg));
		try {
			msgDist.putMessage("SERVER", server.nameToIP.get(client.getInetAddress().toString()) + " has connected to chat server!");
		} catch (InterruptedException e3) {
			// TODO Auto-generated catch block
		}
		
		gui.addMessage(Message.construct("INFO", "Client " + server.nameToIP.get(client.getInetAddress().toString()) + " has connected from " + client.getInetAddress()));
		
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
					msgDist.putMessage("SERVER", server.nameToIP.get(client.getInetAddress().toString()) + " has disconnected from chat server!");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
				}
				server.connectedClients.remove(client);
				server.nameToIP.remove(client.getInetAddress().toString());
				return;
			}
			
			try {
				msgDist.putMessage(client.getInetAddress().toString(), msg);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
