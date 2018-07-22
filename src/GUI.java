import java.awt.*;
import javax.swing.*;

public class GUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JTextField messageField;
	private JTextArea chatHistoryArea;
	private Server server;
	public JButton sendButton;

	public void setServer(Server server) {
		this.server = server;
	}

	public GUI() {
		setTitle("Simple TCP Chat Server");
		setSize(500, 250);
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JPanel panel = (JPanel) getContentPane();
		
		JPanel northPanel = new JPanel();
		panel.add(northPanel, BorderLayout.NORTH);
		
		JLabel portLabel = new JLabel("Server Port:");
		northPanel.add(portLabel);
		
		JTextField portField = new JTextField();
		portField.setColumns(5);
		northPanel.add(portField);
		
		JButton openServerButton = new JButton("Open");
		northPanel.add(openServerButton);
		
		JButton closeServerButton = new JButton("Close");
		northPanel.add(closeServerButton);
		
		chatHistoryArea = new JTextArea();
		chatHistoryArea.setEditable(false);
		JScrollPane messageScrollPane = new JScrollPane(chatHistoryArea);
		add(messageScrollPane, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		panel.add(southPanel, BorderLayout.SOUTH);
		
		southPanel.add(new JLabel("Message:"));
		JPanel messageFieldPanel = new JPanel();
		southPanel.add(messageFieldPanel);
		
		messageField = new JTextField(20);
		messageFieldPanel.add(messageField);
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		messageFieldPanel.add(sendButton);
		
		// Events listeners
		openServerButton.addActionListener((eventObject) -> {
			boolean validPort = true;
			int port = -1;
			try {
				port = Integer.parseInt(portField.getText());
			} catch (NumberFormatException e) {
				addMessage("ERROR", "Please enter valid port!");
				validPort = false;
			}
			if (validPort && (port < 1 || port > 65535)) {
				validPort = false;
			}
			if (validPort) {
				openServerButton.setEnabled(false);
				server = new Server(this);
				server.open(port);
			}
		});
		closeServerButton.addActionListener((eventObject) -> {
			server.close();
			openServerButton.setEnabled(true);
			sendButton.setEnabled(false);
			addMessage("INFO", "Server closed!");
		});
		sendButton.addActionListener((eventObject) -> {
			addMessage("Server", messageField.getText());
			server.sendMessage(messageField.getText());
		});
	}
	
	public void addMessage(String author, String msg) {
		SwingUtilities.invokeLater(() -> {
			chatHistoryArea.append(author + ": " + msg + "\n");
		});
	}
}