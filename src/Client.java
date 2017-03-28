import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by shane on 28/03/2017.
 *
 */
public class Client extends JPanel {
    private static final int PORT = 8765;
    private static final String HOST_NAME = "";
    private static int FRAME_WIDTH, FRAME_HEIGHT;
    private static JFrame frame;
    final String DONE = "done";

    Client() {
        super();
        add(new JLabel("YOOOOOOO GUIS ARE SHIT"));
    }

    public void startClient() {
        Socket socket = null;
        Scanner keyboardInput = new Scanner(System.in);
        try {
            socket = new Socket(HOST_NAME, PORT);
        } catch(IOException e) {
            System.out.println("Client could not make connection: " + e.getMessage());
        }
        DataOutputStream output = null;
        DataInputStream input = null;
        try {
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(socket.getInputStream());
            System.out.println("Enter message to send or " + DONE + " to Exit");
            String clientRequest;
            do {
                clientRequest = keyboardInput.nextLine();
                output.writeUTF(clientRequest);
                String serverResponse = input.readUTF();
                System.out.println("Server response: " + serverResponse);
                serverResponse = input.readUTF();
                System.out.println("Server response: " + serverResponse);
            } while(!DONE.equalsIgnoreCase(clientRequest.trim()));
        } catch(IOException e) {
            System.out.println("Client Error: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {  Client client = new Client();


        frame = new JFrame("Chat Client");
        frame.setSize(WIDTH, HEIGHT);
        frame.setFocusable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(client);
        //gets the dimensions for screen width and height to calculate center
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        int screenHeight = dimension.height;
        int screenWidth = dimension.width;

        FRAME_HEIGHT = screenHeight/2;
        FRAME_WIDTH = screenWidth/2;

        frame.pack(); //resize frame apropriately for its content
        //positions frame in center of screen
        frame.setLocation(new Point((screenWidth/2)-(frame.getWidth()/2),
                (screenHeight/2)-(frame.getHeight()/2)));
        frame.setVisible(true);
        frame.setResizable(false);

        client.startClient();
    }
}
