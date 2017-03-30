import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Shane Birdsall
 * ID: 14870204
 * The server class is responsible for maintaining a collection of connected clients, and passing messages between
 * clients using TCP. The server class is also responsible for keeping clients up-to date with the server status
 * by using UDP.
 */
public class Server {
    private static final int PORT = 8765;
    private DatagramSocket udpDatagramSocket;
    private ServerSocket serverSocket;

    private boolean stopServer = false;
    private SendStatus sendStatus;
    private ConcurrentHashMap<String, Connection> connectedClients;

    private Server() {
        connectedClients = new ConcurrentHashMap<>();
        try {
            udpDatagramSocket = new DatagramSocket(PORT);
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendStatus = new SendStatus();
    }

    private void startServer() {
        try {
            System.out.println("Server started at " + InetAddress.getLocalHost() + " on port " + PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        new Thread(sendStatus).start(); // Start sending status updates to all connected clients
        while(!stopServer) {
            try {
                Socket socket = serverSocket.accept(); // Wait until a new client has connected
                System.out.println("Client with address: " + socket.getInetAddress() + " has connected");
                Connection connection = new Connection(socket); // Crete new connection for that client
                new Thread(connection).start(); // Begin connection thread between that client and the server
            } catch (SocketTimeoutException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.err.println("Can't accept client connection: " + e);
                stopServer = true;
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server shutting down...");
    }

    public static void main(String[] args)  {
        Server server = new Server();
        server.startServer();
    }

    /**
     * @author Shane Birdsall
     * ID: 14870204
     * The Connection represents a connection between a single client and the server and will be run as a thread.
     * The thread will process any messages that client wants to send.
     */
    private class Connection implements Runnable {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket clientSocket;
        String clientID;

        Connection(Socket socket) {
            try {
                clientSocket = socket;
                in = new ObjectInputStream(clientSocket.getInputStream());
                out = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                Message receivedMessage;
                clientID = (String) in.readObject(); // Receive client name
                System.out.println("Starting up new connection with " + clientID);
                connectedClients.put(clientID, this); // Map client ID to clients connection
                out.writeObject(new WelcomeMessage(clientID)); // Send welcome message to connected client
                do {
                    receivedMessage = (Message) in.readObject(); // Receive message from connected client
                    if(receivedMessage instanceof MessageTo) { // Handle private message
                        connectedClients.get(((MessageTo) receivedMessage).getReceiver()).out.writeObject(receivedMessage);
                    } else if(receivedMessage instanceof BroadcastMessage) { // Handle public message
                        for (Object o : connectedClients.entrySet()) {
                            HashMap.Entry pair = (HashMap.Entry) o;
                            Connection c = (Connection) pair.getValue();
                            if(!c.clientID.equals(clientID)) { // Stops sending broadcast to yourself
                                c.out.writeObject(receivedMessage); // Send out broadcast
                            }
                        }
                    }
                    Thread.sleep(1000); // Wait before processing another message from the client
                } while(!(receivedMessage instanceof DisconnectMessage)); // Stop after the user sends a disconnect request
                System.out.println("Closing connection with " + clientSocket.getInetAddress());
            } catch(IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            } finally { // Close all connections. Stop all threads.
                try {
                    stopServer = true;
                    if(out != null) out.close();
                    if(in != null) in.close();
                    if(clientSocket != null) clientSocket.close();
                    if(connectedClients != null && this.clientID != null) connectedClients.remove(this.clientID);
                } catch(IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    /**
     * @author Shane Birdsall
     * ID: 14870204
     * The SendStatus class is used for sending packets to clients to update them on server status (connected clients)
     */
    private class SendStatus implements Runnable {
        @Override
        public void run() {
            try {
                while(!stopServer) { // While server is active
                    byte[] receivedRequest = new byte[100];
                    DatagramPacket receivedPacket = new DatagramPacket(receivedRequest, receivedRequest.length);
                    udpDatagramSocket.receive(receivedPacket); // Wait until update request is received

                    // Convert list of connected clients to a byte array
                    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
                    objectOutputStream.writeObject(new ArrayList<>(connectedClients.keySet()));
                    byte[] buffer = byteOutputStream.toByteArray();

                    // send packet containing update to the the client who requested it
                    DatagramPacket clientStatus = new DatagramPacket(buffer, buffer.length, receivedPacket.getAddress(),
                            receivedPacket.getPort());
                    udpDatagramSocket.send(clientStatus);
                    Thread.sleep(300); // Wait before starting over
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
