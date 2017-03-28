/**
 * Created by shane on 28/03/2017.
 * DisconnectMessage is used to notify the server when a client wants to exit.
 */
public class DisconnectMessage extends Message {

    DisconnectMessage(String message) {
        super(message);
    }
}
