/**
 * @author Shane Birdsall
 * ID: 14870204
 * MessageTo is used for private messages between two clients.
 */
class MessageTo extends Message {
    private String receiver;
    MessageTo(String message, String sender, String receiver) {
        super(message, sender);
        this.receiver = receiver;
    }
    String getReceiver() { return receiver; }
}
