import java.net.Socket;
import java.util.Scanner;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Server {

	public Server() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		final int port = 777;

		try {
			ServerSocket s = new ServerSocket(port);

			boolean ret = true;
			while (ret) {
				System.out.println("Waiting for client to connect!");
				Socket client = s.accept();
				System.out.println("Connected from: " + client.getInetAddress());

				ret = serve(client);
			}

			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean serve(Socket client) throws IOException {
		
		OutputStream clientOutputStream = client.getOutputStream();
		InputStream clientInputStream = client.getInputStream();
		
		clientOutputStream.write("Welcome!\n".getBytes());
		clientOutputStream.flush();
		
		DisplayIncomming print = new DisplayIncomming(clientInputStream);
		print.start();
		
		try (Scanner sc = new Scanner(System.in)) {
			while (sc.hasNext()) {
				String msgToClient = sc.nextLine();
				if (msgToClient.equals("end")) {
					break;
				}
				clientOutputStream.write(msgToClient.getBytes());
				clientOutputStream.write('\n');
				clientOutputStream.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			print.interrupt();
			return true;
		}

		client.close();

		return true;
	}

}