import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shane on 28/03/2017.
 * The server class is responsible for maintaining a collection of connected clients, and passing messages between
 * clients using TCP. The server class is also responsible for keeping clients up-to date with the server status.
 */
public class Server {
    private static final int PORT = 8765;
    private boolean stopServer = false;

   // HashMap<Integer, Connection> connectedClients = new HashMap<>();
    ArrayList<Connection> connectionArrayList = new ArrayList<>();

    public void startServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);

            System.out.println("Server started at " + InetAddress.getLocalHost() + " on port " + PORT);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        while(!stopServer) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Connection made with " + socket.getInetAddress());
                Connection connection = new Connection(socket);
                connectionArrayList.add(connection);

                System.out.println("Processed the client and started Connection");
                System.out.println("client added to arraylist");
            } catch (SocketTimeoutException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.err.println("Can't accept client connection: " + e);
                stopServer = true;
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {}
        System.out.println("Server finishing...");
    }

    private class Connection extends Thread {
        final String DONE = "done";
        DataInputStream in;
        DataOutputStream out;
        Socket clientSocket;

        public Connection(Socket socket) {
            try {
                clientSocket = socket;
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());
                this.start();
            } catch(IOException e) {
                System.out.println("Connection: " + e.getMessage());
            }
        }

        public void run() {
            try {
                String clientRequest;
                do {
                    clientRequest = in.readUTF();
                    System.out.println("Received in Connection Thread line: " + clientRequest);
                    String serverResponse = clientRequest.toUpperCase();
                    out.writeUTF(serverResponse);
                    serverResponse = clientRequest.toLowerCase();
                    out.writeUTF(serverResponse);
                } while(clientRequest != null && !DONE.equalsIgnoreCase(clientRequest.trim()));
                System.out.println("Closing connection with " + clientSocket.getInetAddress());
            } catch(IOException e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    if(out != null) out.close();
                    if(in != null) in.close();
                    if(clientSocket != null) clientSocket.close();
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args)  {
        Server server = new Server();
        server.startServer();
    }
}
