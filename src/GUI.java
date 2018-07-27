import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class GUI extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	Server server = null;
	MessageDistributer msgDist = null;
	
	JTextField messageField;
	JTextArea chatHistoryArea;
	JButton sendButton;
	JTextField portField;
	JButton openServerButton;
	JButton closeServerButton;

	public void setServer(Server server) {
		this.server = server;
	}

	public void setMessageDistributer(MessageDistributer msgDist) {
		this.msgDist = msgDist;
	}

	public GUI() {
		setTitle("Simple Chat Server");
		setSize(500, 250);
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JPanel panel = (JPanel) getContentPane();
		
		JPanel northPanel = new JPanel();
		panel.add(northPanel, BorderLayout.NORTH);
		
		JLabel portLabel = new JLabel("Server Port:");
		northPanel.add(portLabel);
		
		portField = new JTextField();
		portField.setColumns(5);
		northPanel.add(portField);
		
		openServerButton = new JButton("Open");
		northPanel.add(openServerButton);
		
		closeServerButton = new JButton("Close");
		northPanel.add(closeServerButton);
		closeServerButton.setEnabled(false);
		
		// Main chat
		chatHistoryArea = new JTextArea();
		chatHistoryArea.setEditable(false);
		chatHistoryArea.setLineWrap(true);
		chatHistoryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
		JScrollPane messageScrollPane = new JScrollPane(chatHistoryArea);
		add(messageScrollPane, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		panel.add(southPanel, BorderLayout.SOUTH);
		
		// User message
		southPanel.add(new JLabel("Message:"));
		JPanel messageFieldPanel = new JPanel();
		southPanel.add(messageFieldPanel);
		
		messageField = new JTextField(25);
		messageFieldPanel.add(messageField);
		sendButton = new JButton("Send");
		sendButton.setEnabled(false);
		messageFieldPanel.add(sendButton);
		
		DefaultCaret caret = (DefaultCaret) chatHistoryArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		// Events listeners
		openServerButton.addActionListener((eventObject) -> {
			boolean validPort = true;
			int port = -1;
			try {
				port = Integer.parseInt(portField.getText());
			} catch (NumberFormatException e) {
				addMessage(Message.construct("ERROR", "Please enter valid port!"));
				validPort = false;
			}
			if (validPort && (port < 1 || port > 65535)) {
				validPort = false;
			}
			if (validPort) {
				openServerButton.setEnabled(false);
				server = new Server(this);
				server.open(port);
				closeServerButton.setEnabled(true);
				
				sendButton.setEnabled(true);
			}
		});
		
		closeServerButton.addActionListener((eventObject) -> {
			closeServerButton.setEnabled(false);
			sendButton.setEnabled(false);
			
			server.close();
			
			openServerButton.setEnabled(true);
			
			portField.requestFocusInWindow();
		});
		
		sendButton.addActionListener((eventObject) -> {
			if (msgDist != null) {
				try {
					String msg = messageField.getText();
					if (msg.length() > 0) {
						msgDist.putMessage("SERVER", msg);
						messageField.setText("");
					}
				} catch (InterruptedException ie) {
					// TODO Auto-generated catch block
					ie.printStackTrace();
				}
			}
		});
		
		// Key bindings
		messageField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
		messageField.getActionMap().put("enterPressed", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (msgDist != null) {
					try {
						msgDist.putMessage("SERVER", messageField.getText());
						messageField.setText("");
					} catch (InterruptedException ie) {
						// TODO Auto-generated catch block
						ie.printStackTrace();
					}
				}
			}
			
		});
		
		portField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enterPressed");
		GUI secondThis = this;
		portField.getActionMap().put("enterPressed", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (server == null || !server.isOpen()) {
					boolean validPort = true;
					int port = -1;
					try {
						port = Integer.parseInt(portField.getText());
					} catch (NumberFormatException ef) {
						addMessage(Message.construct("ERROR", "Please enter valid port!"));
						validPort = false;
					}
					if (validPort && (port < 1 || port > 65535)) {
						validPort = false;
					}
					if (validPort) {
						openServerButton.setEnabled(false);
						server = new Server(secondThis);
						server.open(port);
						closeServerButton.setEnabled(true);
						
						sendButton.setEnabled(true);
						messageField.requestFocusInWindow();
					}
				}
			}
			
		});
	}
	
	public void addMessage(String msg) {
		SwingUtilities.invokeLater(() -> {
			chatHistoryArea.append(msg + "\n");
		});
	}
}
