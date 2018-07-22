import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class IncommingMessageHandler extends Thread {

	static final int INPUT_BUFFER = 1024;
	private Socket client;
	private InputStream input;
	private GUI gui;

	public IncommingMessageHandler(Socket client, GUI gui) throws IOException {
		this.client = client;
		this.input = client.getInputStream();
		this.gui = gui;
	}

	@Override
	public void run() {
		while (true) {
			byte[] bytes = new byte[INPUT_BUFFER];
			int readBytes;
			try {
				readBytes = input.read(bytes);
			} catch (IOException e) {
				try {
					client.close();
				} catch (IOException e1) {
					System.exit(-1);
				}
				gui.addMessage("WARNING", "Client has closed connection!");
				return;
			}
			if (readBytes < 0) {
				try {
					client.close();
				} catch (IOException e1) {
					System.exit(-1);
				}
				gui.addMessage("WARNING", "Client has closed connection!");
				return;
			} else {
				gui.addMessage("Client", new String(bytes, 0, readBytes));
			}
		}
	}
}
