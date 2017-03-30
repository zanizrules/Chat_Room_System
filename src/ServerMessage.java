/**
 * @author Shane Birdsall
 * ID: 14870204
 * ServerMessage is an abstract class that helps define specific messages sent from the server to clients.
 */
abstract class ServerMessage extends Message {
    ServerMessage(String message) {
        super(message, "Server");
    }
}
