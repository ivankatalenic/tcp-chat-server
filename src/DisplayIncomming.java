import java.io.InputStream;

public class DisplayIncomming extends Thread {

	static final int INPUT_BUFFER = 1024;
	private InputStream input;
	private GUI gui;

	public DisplayIncomming(InputStream input, GUI gui) {
		this.input = input;
		this.gui = gui;
	}

	@Override
	public void run() {
		while (true) {
			byte[] bytes = new byte[INPUT_BUFFER];
			int readBytes;
			try {
				readBytes = input.read(bytes);
			} catch (Exception e) {
				return;
			}
			if (readBytes < 0) {
				return;
			} else {
				gui.addMessage("Client", new String(bytes, 0, readBytes));
			}
		}
	}
}
