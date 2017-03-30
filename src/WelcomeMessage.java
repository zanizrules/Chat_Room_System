/**
 * @author Shane Birdsall
 * ID: 14870204
 * WelcomeMessage represents a message that will welcome users to the server when they connect.
 */
class WelcomeMessage extends ServerMessage {
    WelcomeMessage(String clientID) {
        super("Welcome To The Live Chat Room " + clientID + "!");
    }
}
