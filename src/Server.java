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

	MessageHandler messageHandler;
	Socket client;
	ServerSocket server;

	/**
	 * Constructs the server object with specified GUI object which provides a
	 * method for displaying messages.
	 * 
	 * @param messageHandler MessageHandler object which provides a method for
	 *                       displaying messages.
	 */
	public Server(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	/**
	 * Opens the server with provided port.
	 * 
	 * @param port Port on which the server is listening for connections.
	 * 
	 * @throws IllegalArgumentException If the port is not in the valid range [0 -
	 *                                  65535].
	 */
	public void open(int port) {
		this.port = port;

		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port is not in the valid range!");
		}

		connectedClients = Collections.synchronizedSet(new HashSet<Socket>(INIT_CLIENT_SET_SIZE));
		clientUsernames = Collections.synchronizedSet(new HashSet<String>(INIT_CLIENT_SET_SIZE));

		stopQueue = new ArrayBlockingQueue<>(1);

		stopping = false;

		start();

		msgDist = new MessageDistributer(this, messageHandler);
		msgDist.start();

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
			messageHandler.displayMessage(Message.construct("ERROR",
					"Server thread was interrupted while waiting for message distributer to close!"));
		}

		try {
			server.close();
		} catch (IOException e) {
			messageHandler.displayMessage(
					Message.construct("WARNING", "An unknown error has occured while closing server socket!"));
		}

		setOpen(false);

		messageHandler.displayMessage(Message.construct("INFO", "Server has been closed!"));
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

	public MessageDistributer getMsgDist() {
		if (isOpen()) {
			return msgDist;
		} else {
			return null;
		}
	}

	@Override
	public void run() {
		messageHandler.displayMessage(Message.construct("INFO", "Server has been started!"));

		try {
			server = new ServerSocket(port);
		} catch (SocketException se) {
			messageHandler.displayMessage(Message.construct("ERROR", "Can not open server socket!"));

			return;
		} catch (IllegalArgumentException ae) {
			messageHandler.displayMessage(Message.construct("ERROR", "Bad port!"));

			return;
		} catch (Exception e) {
			messageHandler.displayMessage(Message.construct("ERROR", "Unknown error!"));

			return;
		}

		setRunning(true);

		while (!stopping) {
			try {
				client = server.accept();
			} catch (SecurityException se) {
				messageHandler.displayMessage(
						Message.construct("ERROR", "Security manager does not accept connection from the client."));

				continue;
			} catch (Exception e) {
				if (!stopping) {
					messageHandler
							.displayMessage(Message.construct("WARNING", "Server can not accept new clients anymore!"));
				}

				break;
			}

			new ClientHandler(this, client, messageHandler, msgDist).start();
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
			System.exit(-1);
		}
	}
}