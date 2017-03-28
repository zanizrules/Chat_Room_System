/**
 * Created by shane on 28/03/2017.
 * The Message class is an abstract class aimed to define the requirements of a message.
 * Proposed subclasses are a DisconnectMessage, BroadcastMessage, and MessageTo.
 */
abstract class Message {
    private String message;

    Message(String message) {
        this.message = message;
    }
}
