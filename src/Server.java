import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.net.ServerSocket;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Class used for connecting clients.
 * 
 * @author Ivan Katalenić
 *
 */
public class Server extends Thread {

	final int INIT_CLIENT_SET_SIZE = 10;

	MessageDistributer msgDist;
	Set<Socket> connectedClients;

	boolean running = true;
	int port;

	GUI gui;
	Socket client;
	ServerSocket server;

	/**
	 * Constructs the server object with specified GUI object which provides a method for displaying messages.
	 * 
	 * @param gui GUI object which provides a method for displaying messages.
	 */
	public Server(GUI gui) {
		this.gui = gui;

		connectedClients = Collections.synchronizedSet(new HashSet<Socket>(INIT_CLIENT_SET_SIZE));
	}

	/**
	 * Opens the server with provided port.
	 * 
	 * @param port Port on which the server is listening for connections.
	 */
	public void open(int port) {
		this.port = port;

		start();

		msgDist = new MessageDistributer(this, gui);
		msgDist.start();

		gui.setMessageDistributer(msgDist);
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
		} catch (SocketException se) {
			gui.addMessage(Message.construct("ERROR", "Can not open server socket!"));

			return;
		} catch (IllegalArgumentException ae) {
			gui.addMessage(Message.construct("ERROR", "Bad port!"));

			return;
		} catch (Exception e) {
			gui.addMessage(Message.construct("ERROR", "Unknown error!"));

			return;
		}

		while (isRunning()) {
			try {
				client = server.accept();
			} catch (SecurityException se) {
				gui.addMessage(Message.construct("ERROR", "Security manager does not accept connection from the client."));
				
				continue;
			} catch (Exception e) {
				gui.addMessage(Message.construct("ERROR", "Can not accept the client due to unknow error."));
				
				continue;
			}

			new ClientHandler(this, client, gui, msgDist).start();

			gui.sendButton.setEnabled(true);
		}
	}

	/**
	 * Disconnects all connected clients and shuts down the server.
	 */
	public void close() {
		try {
			msgDist.putMessage("SERVER", "Server is shutting down!");
		} catch (InterruptedException e1) {
			
		}
		
		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) {
			
		}
		
		for (Socket client : connectedClients) {
			try {
				client.close();
			} catch (IOException e) {
				gui.addMessage(Message.construct("WARNING", "An unknown error has occured while closing the clients socket."));
			}
		}
		
		msgDist.interrupt();
		
		try {
			msgDist.join();
		} catch (InterruptedException e) {
			gui.addMessage(Message.construct("ERROR", "Server thread was interrupted while waiting for message distributer to close!"));
			// TODO What to do here?
		}
		
		try {
			server.close();
		} catch (IOException e) {
			gui.addMessage(Message.construct("WARNING", "An unknown error occured while closing main server socket!"));
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

		// Starting the GUI.
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