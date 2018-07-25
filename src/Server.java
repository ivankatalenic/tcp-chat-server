import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.net.ServerSocket;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Server extends Thread {

	final int INIT_CLIENT_SET_SIZE = 10;

	MessageDistributer msgDist;
	ConcurrentHashMap<String, String> nameToIP;
	Set<Socket> connectedClients;

	boolean running = true;
	int port;
	GUI gui;
	Socket client;
	ServerSocket server;

	public Server(GUI gui) {
		this.gui = gui;
		nameToIP = new ConcurrentHashMap<>(INIT_CLIENT_SET_SIZE);
		nameToIP.put("SERVER", "SERVER");
		connectedClients = Collections.synchronizedSet(new HashSet<Socket>(INIT_CLIENT_SET_SIZE));
	}

	public void open(int port) {
		this.port = port;
		this.start();
		msgDist = new MessageDistributer(this, gui);
		msgDist.start();
		gui.setMsgDist(msgDist);
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		try {
			server = new ServerSocket(port);

			while (isRunning()) {
				try {
					client = server.accept();
				} catch (IOException io) {
					// This happens when GUI decides to close the server.
					// TODO What to do with running MessageDistributer thread which has reference to this class?
					return;
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				
				new ClientHandler(this, client, gui, msgDist).start();

				gui.sendButton.setEnabled(true);
			}

		} catch (SocketException se) {
			// TODO Print appropriate error message!
			System.exit(-1);
		} catch (Exception e) {
			// TODO Print appropriate error message!
			System.exit(-1);
		}
	}

	public void close() {
		for (Socket client : connectedClients) {
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		try {
			msgDist.interrupt();
			try {
				msgDist.join();
			} catch (InterruptedException e) {
				// TODO Print appropriate error message!
				System.exit(-1);
			}
			server.close();
		} catch (IOException e) {
			gui.addMessage(Message.construct("ERROR", "Can not close the server!"));
			System.exit(-1);
		}
		
		gui.addMessage(Message.construct("INFO", "Server has been closed!"));
	}

	public static void main(String[] args) {
		// Setting Nimbus Look&Feel.
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					new GUI();
				}
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			// TODO Auto-generated catch block
			System.exit(-1);
		}
	}
}