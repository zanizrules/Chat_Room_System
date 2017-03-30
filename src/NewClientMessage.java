/**
 * @author Shane Birdsall
 * ID: 14870204
 * NewClientMessage represents the message that will be sent to clients when a new user comes online.
 */
class NewClientMessage extends ServerMessage {
    NewClientMessage(String clientID) {
        super(clientID + " has connected to the chat server!");
    }
}
