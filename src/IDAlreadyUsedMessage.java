/**
 * @author Shane Birdsall
 * ID: 14870204
 * IDAlreadyUsedMessage is used to notify clients that the name they chose is invalid due to already being used.
 */
class IDAlreadyUsedMessage extends ServerMessage {
    IDAlreadyUsedMessage() {
        super("Name Already Exists!");
    }
}
