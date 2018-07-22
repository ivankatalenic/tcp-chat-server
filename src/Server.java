import java.net.Socket;
import java.net.SocketException;
import javax.swing.SwingUtilities;
import java.net.ServerSocket;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Server extends Thread {
	
	boolean running = true;
	private int port;
	private GUI gui;
	private Socket client;
	private ServerSocket s;

	public Server(GUI gui) {
		this.gui = gui;
	}
	
	public void open(int port) {
		this.port = port;
		this.start();
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
			s = new ServerSocket(port);
			
			while (isRunning()) {
				gui.addMessage("INFO", "Waiting for client to connect!");
				
				client = s.accept();
				
				gui.addMessage("INFO", "Client connected from " + client.getInetAddress());
				gui.sendButton.setEnabled(true);
				
				IncommingMessageHandler print;
				try {
					print = new IncommingMessageHandler(client, gui);
					print.start();
					print.join();
				} catch (IOException e) {
					gui.addMessage("ERROR", "Client has abruptly disconnected!");
					try {
						client.close();
					} catch (IOException e1) {
						// This triggers if socket cannot be closed.
						// Can't do anything meaningful then.
					}
				} catch (Exception other) {
					// Other errors about thread that shouldn't ever happen.
					System.exit(-1);
				}
			}
			
		} catch (SocketException se) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if (client != null && client.isConnected()) {
				client.close();
			}
			s.close();
		} catch (IOException e) {
			gui.addMessage("ERROR", "Can not close the server!");
		}
	}

	public static void main(String[] args) {
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
	
	public boolean sendMessage(String msg) {
		try {
			client.getOutputStream().write(msg.getBytes());
			client.getOutputStream().flush();
			return true;
		} catch (IOException e) {
			gui.addMessage("WARNING", "Client has closed the connection!");
			try {
				client.close();
			} catch (IOException e1) {
				// This triggers if socket cannot be closed.
				// Can't do anything meaningful then.
			}
			close();
			return false;
		}
	}
}