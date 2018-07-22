import java.net.Socket;
import java.net.SocketException;
import javax.swing.SwingUtilities;
import java.net.ServerSocket;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Server extends Thread {
	
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
	
	@Override
	public void run() {
		try {
			s = new ServerSocket(port);
			
			gui.addMessage("INFO", "Waiting for client to connect!");
			client = s.accept();
			gui.addMessage("INFO", "Client connected from " + client.getInetAddress());
			gui.sendButton.setEnabled(true);
			
			DisplayIncomming print = new DisplayIncomming(client.getInputStream(), gui);
			print.start();
			print.join();
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
					GUI gui = new GUI();
					gui.setVisible(true);
				}
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
	}
	
	public void sendMessage(String msg) {
		try {
			client.getOutputStream().write(msg.getBytes());
			client.getOutputStream().flush();
		} catch (IOException e) {
			gui.addMessage("WARNING", "Client has closed the connection!");
			close();
		}
	}
}