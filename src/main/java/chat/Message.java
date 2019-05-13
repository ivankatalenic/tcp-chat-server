package chat;

/**
 * Interface which provides function for constructing formated messages.
 * 
 * @author Ivan Katalenić
 *
 */
public interface Message {

	/**
	 * Constructs the message string from the author and message of the that author.
	 * 
	 * @param author Author of the message.
	 * @param msg    Message of the author.
	 * @return String including information about author and his message.
	 */
	public static String construct(String author, String msg) {
		String finalMsg;

		switch (author) {
		case "SERVER":
		case "ERROR":
		case "INFO":
		case "WARNING":
			finalMsg = "### " + author + ": " + msg;
			break;
		default:
			finalMsg = author + ": " + msg;
			break;
		}

		return finalMsg;
	}
}
