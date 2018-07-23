public class Message {
	private String messageString;
	private String authorID;

	public Message(String authorID, String msg) {
		this.authorID = authorID;
		this.messageString = msg;
	}

	public String getMessageString() {
		return messageString;
	}

	public String getAuthorID() {
		return authorID;
	}

	@Override
	public String toString() {
		return authorID + ":" + messageString;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authorID == null) ? 0 : authorID.hashCode());
		result = prime * result + ((messageString == null) ? 0 : messageString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Message)) {
			return false;
		}
		
		// TODO Possible error: Method accesses class member fields that are set private.
		
		Message other = (Message) obj;
		if (authorID == null) {
			if (other.authorID != null) {
				return false;
			}
		} else if (!authorID.equals(other.authorID)) {
			return false;
		}
		if (messageString == null) {
			if (other.messageString != null) {
				return false;
			}
		} else if (!messageString.equals(other.messageString)) {
			return false;
		}
		return true;
	}
}
