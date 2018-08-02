/**
 * Interface which declares a method which is used for displaying strings.
 * 
 * @author Ivan Katalenić
 *
 */
public interface MessageHandler {

	/**
	 * Method for displaying strings.
	 * 
	 * @param msg String to be displayed.
	 */
	public void displayMessage(String msg);

	public default void displayMessage(String author, String msg) {
		displayMessage(Message.construct(author, msg));
	}
}
