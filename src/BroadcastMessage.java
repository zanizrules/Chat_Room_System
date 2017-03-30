/**
 * @author Shane Birdsall
 * ID: 14870204
 * BroadcastMessage is used to send all clients in the chat server a message from the user.
 */
class BroadcastMessage extends Message {
    BroadcastMessage(String message, String sender) {
        super(message, sender);
    }
}
