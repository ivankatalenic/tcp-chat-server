public class Message {

	public Message() {
		
	}
	
	public static String construct(String author, String msg) {
		String finalMsg;
		switch (author) {
		case "SERVER": case "ERROR": case "INFO": case "WARNING":
			finalMsg = "### " + author + ": " + msg;
			break;
		default:
			finalMsg = author + ": " + msg;
			break;
		}
		return finalMsg;
	}
}
