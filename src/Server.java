import java.io.IOException;

import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.lang.reflect.InvocationTargetException;

/**
 * Class used for connecting clients.
 * 
 * @author Ivan Katalenić
 */
public class Server extends Thread {

	final int INIT_CLIENT_SET_SIZE = 10;

	MessageDistributer msgDist;
	Set<Socket> connectedClients;
	Set<String> clientUsernames;
	
	BlockingQueue<Object> stopQueue;

	boolean running = false;
	boolean open = false;
	boolean stopping = false;
	
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
	}

	/**
	 * Opens the server with provided port.
	 * 
	 * @param port Port on which the server is listening for connections.
	 * 
	 * @throws IllegalArgumentException If the port is not in the valid range [0 - 65535].
	 */
	public void open(int port) {
		this.port = port;
		
		connectedClients = Collections.synchronizedSet(new HashSet<Socket>(INIT_CLIENT_SET_SIZE));
		clientUsernames = Collections.synchronizedSet(new HashSet<String>(INIT_CLIENT_SET_SIZE));
		
		stopQueue = new ArrayBlockingQueue<>(1);
		
		stopping = false;
		
		start();

		msgDist = new MessageDistributer(this, gui);
		msgDist.start();

		gui.setMessageDistributer(msgDist);
		
		setOpen(true);
	}
	
	/**
	 * Disconnects all connected clients and shuts down the server.
	 */
	public void close() {
		stopping = true;
		
		try {
			msgDist.putMessage("WARNING", "Server is shutting down!");
			msgDist.stopCommunication();
		} catch (InterruptedException e1) {
			
		}
		
		for (Socket client : connectedClients) {
			try {
				client.close();
			} catch (IOException e) {
				
			}
		}
		
		try {
			msgDist.join();
		} catch (InterruptedException e) {
			gui.addMessage(Message.construct("ERROR", "Server thread was interrupted while waiting for message distributer to close!"));
		}
		
		try {
			server.close();
		} catch (IOException e) {
			gui.addMessage(Message.construct("WARNING", "An unknown error has occured while closing server socket!"));
		}
		
		setOpen(false);

		gui.addMessage(Message.construct("INFO", "Server has been closed!"));
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	@Override
	public void run() {
		gui.addMessage(Message.construct("INFO", "Server has been started!"));
		
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
		
		gui.sendButton.setEnabled(true);

		setRunning(true);
		
		while (!stopping) {
			try {
				client = server.accept();
			} catch (SecurityException se) {
				gui.addMessage(Message.construct("ERROR", "Security manager does not accept connection from the client."));
				
				continue;
			} catch (Exception e) {
				if (!stopping) {
					gui.addMessage(Message.construct("WARNING", "Server can not accept new clients anymore!"));
				}
				
				break;
			}

			new ClientHandler(this, client, gui, msgDist).start();
		}
		
		setRunning(false);
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