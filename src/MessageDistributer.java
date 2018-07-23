import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MessageDistributer extends Thread {
	
	final int MSG_QUEUE_SIZE = 500;
	
	BlockingQueue<Message> msgQueue;
	Server server;
	GUI gui;

	public MessageDistributer(Server server, GUI gui) {
		this.server = server;
		this.gui = gui;
		msgQueue = new ArrayBlockingQueue<>(MSG_QUEUE_SIZE);
	}
	
	public void putMessage(String authorID, String msg) throws InterruptedException {
		putMessage(new Message(authorID, msg));
	}
	
	public void putMessage(Message msg) throws InterruptedException {
		msgQueue.put(msg);
	}

	@Override
	public void run() {
		while (true) {
			Message msg = null;
			
			if (this.isInterrupted()) {
				return;
			}
			
			try {
				msg = msgQueue.take();
			} catch (InterruptedException e) {
				// Signal that this thread should be terminated!
				return;
			}
			
			// TODO Create a method for formatting this kind of messages.
			String displayMessage = server.nameToIP.get(msg.getAuthorID()) + ": " + msg.getMessageString();
			
			// Sends messages to all clients connected!
			Iterator<Socket> it = server.connectedClients.iterator();
			while (it.hasNext()) {
				Socket client = it.next();
				if (client.isClosed()) {
					it.remove();
				} else {
					try {
						// TODO Store DataOutputStream object permanently for every client in connectedClients.
						DataOutputStream out = new DataOutputStream(client.getOutputStream());
						out.writeUTF(displayMessage);
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
			
			gui.addMessage(displayMessage);
		}
	}
}
