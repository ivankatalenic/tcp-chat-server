import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageDistributer extends Thread {

	static final int MSG_QUEUE_SIZE = 512;
	static final String stopMessage = "fer.unizg.hr";

	BlockingQueue<String> msgQueue;
	Server server;
	GUI gui;
	
	boolean stopping = false;

	public MessageDistributer(Server server, GUI gui) {
		this.server = server;
		this.gui = gui;
		msgQueue = new ArrayBlockingQueue<>(MSG_QUEUE_SIZE);
	}
	
	void stopCommunication() throws InterruptedException {
		stopping = true;
		msgQueue.put(stopMessage);
	}

	public void putMessage(String author, String msg) throws InterruptedException {
		putMessage(Message.construct(author, msg));
	}

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
			Iterator<Socket> it = server.connectedClients.iterator();
			synchronized (server.connectedClients) {
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

			gui.addMessage(msg);
		}
	}
}
