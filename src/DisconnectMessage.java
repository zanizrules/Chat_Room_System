/**
 * @author Shane Birdsall
 * ID: 14870204
 * DisconnectMessage is used to notify when a client wants to leave the chat server
 * In other words when the user has clicked the Disconnect button.
 */
class DisconnectMessage extends ServerMessage {
    DisconnectMessage() {
        super("You have disconnected from the server! Have a nice day!!!");
    }
}
