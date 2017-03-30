/**
 * @author Shane Birdsall
 * ID: 14870204
 * RequestUpdateMessage is the type of message a client sends when they wish to receive a status update from the server.
 */
class RequestUpdateMessage extends Message {
    RequestUpdateMessage(String sender) {
        super("Requesting Update", sender);
    }
}
