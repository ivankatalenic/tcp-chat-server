public interface MessageHandler {
	
	public void displayMessage(String msg);
	
	public default void displayMessage(String author, String msg) {
		displayMessage(Message.construct(author, msg));
	}
}
