import java.io.InputStream;

public class DisplayIncomming extends Thread {

	static final int INPUT_BUFFER = 1024;
	private InputStream input;

	public DisplayIncomming(InputStream input) {
		this.input = input;
	}

	@Override
	public void run() {
		while (true) {
			if (Thread.interrupted()) {
				return;
			}
			byte[] bytes = new byte[INPUT_BUFFER];
			int readBytes;
			try {
				readBytes = input.read(bytes);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			System.out.print(new String(bytes, 0, readBytes));
		}
	}
}
