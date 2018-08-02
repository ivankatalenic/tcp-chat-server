import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class used for accepting and distributing messages to all clients connected
 * to the server.
 * 
 * @author Ivan Katalenić
 *
 */
public class MessageDistributer extends Thread {

	static final int MSG_QUEUE_SIZE = 512;
	static final String stopMessage = "fer.unizg.hr";

	BlockingQueue<String> msgQueue;
	Server server;
	MessageHandler messageHandler;

	boolean stopping = false;

	public MessageDistributer(Server server, MessageHandler messageHandler) {
		this.server = server;
		this.messageHandler = messageHandler;
		msgQueue = new ArrayBlockingQueue<>(MSG_QUEUE_SIZE);
	}

	/**
	 * Stops the message distributer from distributing messages to connected
	 * clients.
	 * 
	 * @throws InterruptedException
	 */
	void stopCommunication() throws InterruptedException {
		stopping = true;
		msgQueue.put(stopMessage);
	}

	/**
	 * Method which formats the message with Message.construct function and sends
	 * that message to all connected clients.
	 * 
	 * @param author Author of the message.
	 * @param msg    Message which will be sent to all clients.
	 * @throws InterruptedException
	 */
	public void putMessage(String author, String msg) throws InterruptedException {
		putMessage(Message.construct(author, msg));
	}

	/**
	 * Puts the message in the queue for distribution to connected clients. This
	 * message is sent to all clients.
	 * 
	 * @param msg Message which is sent to all clients.
	 * @throws InterruptedException
	 */
	public void putMessage(String msg) throws InterruptedException {
		if (!stopping) {
			msgQueue.put(msg);
		}
	}

	@Override
	public void run() {
		while (true) {
			String msg = null;

			if (this.isInterrupted()) {
				return;
			}

			try {
				msg = msgQueue.take();
			} catch (InterruptedException e) {
				// Signal that this thread should be terminated!
				return;
			}

			if (msg.equals(stopMessage)) {
				return;
			}

			// Sends messages to all clients connected!
			synchronized (server.connectedClients) {
				Iterator<Socket> it = server.connectedClients.iterator();
				while (it.hasNext()) {
					Socket client = it.next();
					if (client.isClosed()) {
						it.remove();
					} else {
						try {
							// TODO Store DataOutputStream object permanently for every client in
							// connectedClients.
							DataOutputStream out = new DataOutputStream(client.getOutputStream());
							out.writeUTF(msg);
						} catch (IOException e) {
							// Client has probably now closed the connection!
							try {
								// This will signal clients ClientHandler which will print the message.
								client.close();
							} catch (IOException e1) {

							}
						}
					}
				}
			}

			messageHandler.displayMessage(msg);
		}
	}
}
