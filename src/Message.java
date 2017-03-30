import java.io.Serializable;

/**
 * @author Shane Birdsall
 * ID: 14870204
 * The Message class is an abstract class aimed to define the requirements of a message.
 * All messages must be Serializable in order to be sent between the server and clients.
 */
abstract class Message implements Serializable {
    private String message, sender;
    Message(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }
    String getMessage() { return message; }
    String getSender() { return sender; }
}
