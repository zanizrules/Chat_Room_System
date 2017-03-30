/**
 * @author Shane Birdsall
 * ID: 14870204
 * DisconnectedClientMessage represents the message sent when a user has disconnected from the chat server.
 * For example if user A disconnects then user B will be notified that user A has disconnected.
 */
class DisconnectedClientMessage extends ServerMessage {
    DisconnectedClientMessage(String clientID) {
        super(clientID + " has just disconnected from chat!");
    }
}
