package simpleDatabase;

public class HelpMessage {
    private String senderUsername;
    private String topic; // For generic messages
    private String message; // For specific messages
    private String response;

    public HelpMessage(String senderUsername, String topic, String message) {
        this.senderUsername = senderUsername;
        this.topic = topic;
        this.message = message;
        this.response = null;
    }

    // Getters and setters
    public String getSenderUsername() {
        return senderUsername;
    }

    public String getTopic() {
        return topic;
    }

    public String getMessage() {
        return message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
