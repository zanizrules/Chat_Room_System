import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Shane Birdsall
 * ID: 14870204
 * The Client class allows users to interact with the chat server. Client is a GUI which takes typed messages and sends
 * them via TCP to a chat server. Messages can be sent privately to other users or broadcasted publicly to all online
 * users. Client will also request updates from the server and receive packets over UDP which will update a JList
 * containing other online users.
 */
public class Client extends JPanel implements ActionListener, Runnable {

    // Server/Client
    private static final int PORT = 8765;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean connected;
    private static String clientID = "", hostIP = "";
    private static ClientSet connectedClients = new ClientSet();

    // GUI Components & Frame
    private static JFrame frame;
    private static JList<String> listOfOtherClients;
    private static JTextArea messageScreen,userInput;
    private static JButton sendButton, broadcastButton, disconnectButton;

    // Setters/Getters
    private static void setClientID(String id) { clientID = id; }
    private static void setHostIP(String ip) { hostIP = ip; }
    private String getClientID() { return clientID; }
    private String getHostIP() { return hostIP; }

    private Client() {
        super(new BorderLayout());

        // Get panel sizes
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        final int FRAME_HEIGHT = dimension.height/2, FRAME_WIDTH = dimension.width/2;
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        final int LIST_WIDTH = FRAME_WIDTH /5;

        // Add list panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setPreferredSize(new Dimension(LIST_WIDTH, FRAME_HEIGHT));
        listPanel.setBorder(BorderFactory.createTitledBorder("Users Online"));
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setPreferredSize(new Dimension(LIST_WIDTH, 30));
        disconnectButton.addActionListener(this);
        listPanel.add(disconnectButton, BorderLayout.SOUTH);
        add(listPanel, BorderLayout.WEST);

        // Create and add online users list
        listOfOtherClients = new JList<String>(connectedClients.getClientListModel());
        listPanel.add(listOfOtherClients, BorderLayout.NORTH);

        // Add message screen
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageScreen = new JTextArea();
        messageScreen.setEditable(false);
        messageScreen.setLineWrap(true);
        messageScreen.setPreferredSize(new Dimension(FRAME_WIDTH - LIST_WIDTH, FRAME_HEIGHT / 2));
        messageScreen.setBorder(BorderFactory.createTitledBorder("Live Chat"));
        JScrollPane scrollTextPane = new JScrollPane(messageScreen);
        scrollTextPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollTextPane, BorderLayout.CENTER);
        messagePanel.add(messageScreen, BorderLayout.CENTER);

        // Add user input section
        JPanel inputPanel = new JPanel(new BorderLayout());
        userInput = new JTextArea();
        userInput.setLineWrap(true);
        JScrollPane inputPane = new JScrollPane(userInput);
        inputPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        inputPane.setPreferredSize(new Dimension(FRAME_WIDTH - LIST_WIDTH - 110, 60));
        inputPanel.add(inputPane, BorderLayout.WEST);

        // Add buttons
        sendButton = new JButton("Private");
        sendButton.setPreferredSize(new Dimension(105, 29));
        sendButton.addActionListener(this);
        broadcastButton = new JButton("Broadcast");
        broadcastButton.setPreferredSize(new Dimension(105, 29));
        broadcastButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(sendButton, BorderLayout.NORTH);
        buttonPanel.add(broadcastButton, BorderLayout.SOUTH);
        // Combine
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createTitledBorder("Message"));
        messagePanel.add(inputPanel, BorderLayout.AFTER_LAST_LINE);
        add(messagePanel);
    }

    @Override
    public void run() { // Process receiving messages
        try {
            Message received;
            output.writeObject(getClientID()); // Send client name
            received = (Message) input.readObject(); // Receive message
            if(received instanceof WelcomeMessage) {
                updateMessageScreen(received); // Print welcome message to GUI
            } else if(received instanceof IDAlreadyUsedMessage) {
                connected = false;
                JOptionPane.showMessageDialog(frame, "The username you have chosen is already in use! " +
                        "\nPlease try reconnecting using a different name");
                System.exit(-1);
            }
            while (connected) { // While connected to server
                received = (Message) input.readObject(); // Receive message
                updateMessageScreen(received); // Print message to GUI
                Thread.sleep(300);
            }
        } catch (EOFException e) {
            connected = false; // End of stream reached. Disconnect.
        } catch (ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(frame, "Connection to the chat server has been lost");
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source == disconnectButton) {
            disconnect();
        } else if(source == sendButton) {
            sendMessageTo();
        } else if(source == broadcastButton) {
            sendBroadcastMessage();
        }
    }

    /* ---------------------- Server related methods ---------------------- */
    private void connectToServer() {
        Socket socket;
        try {
            socket = new Socket(hostIP, PORT);

            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                connected = true;
                new Thread(new UpdateStatus()).start(); // Start receiving status updates from server
                new Thread(this).start(); // Start receiving messages from server
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch(IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not connect to a chat server running at: " + getHostIP());
        }
    }

    private void sendBroadcastMessage() {
        if(output != null) {
            try {
                if(connectedClients.size() == 0) { // No users to broadcast to
                    JOptionPane.showMessageDialog(frame, "There is currently no one else online!");
                } else {
                    BroadcastMessage message = new BroadcastMessage(userInput.getText(), getClientID());
                    userInput.setText(""); // Clear user input box
                    output.writeObject(message); // Send broadcast
                    updateMessageScreen(message); // Write broadcast to GUI
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error: You are not connected to the chat server!");
            }
        }
    }

    private void sendMessageTo() {
        if(output != null) {
            try {
                if(listOfOtherClients.getSelectedValue() == null) { // User has not selected someone to send a message to
                    JOptionPane.showMessageDialog(frame, "Please select someone to send a message to or click broadcast to send to everyone!");
                } else {
                    MessageTo message = new MessageTo(userInput.getText(), getClientID(), listOfOtherClients.getSelectedValue());
                    userInput.setText(""); // Clear user input box
                    output.writeObject(message); // Send private message
                    updateMessageScreen(message); // Write private message to GUI
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error: You are not connected to the chat server!");
            }
        }
    }

    private ClientStatusUpdate compareServerStatusWithClientStatus(ArrayList<String> serverStatus) {
        ClientStatusUpdate update = new ClientStatusUpdate();
        for(String activeClient : serverStatus) {
            // Once all active clients are removed only disconnected clients will be left
            update.disconnectedClients.remove(activeClient);
            if(!connectedClients.contains(activeClient)) {
                update.newClients.add(activeClient); // Add any users that the client does not already know about
            }
        }
        return update;
    }

    private void disconnect() {
        if(output != null) {
            try {
                DisconnectMessage dis = new DisconnectMessage();
                output.writeObject(dis); // Request that you disconnect from the server
                updateMessageScreen(dis);
            } catch (IOException e) {
                System.err.println("Error Disconnecting");
            }
        }

    }

    /* ---------------------- GUI related methods ---------------------- */
    synchronized private void updateMessageScreen(Message mes) {
        if (mes.getSender().equals(getClientID())) { // If it is a message you sent prefix it with "Me"
            messageScreen.append("Me");
        } else messageScreen.append(mes.getSender()); // Otherwise prefix it with the senders ID
        if (mes instanceof MessageTo) {
            if (!mes.getSender().equals(getClientID())) {
                // If it is a private message I've received prefix it with <Private>
                messageScreen.append(" <Private> ");
            } else { // Otherwise I also want to know how I sent it to so prefix with <Private to 'name'>
                messageScreen.append(" <Private to " + ((MessageTo) mes).getReceiver() + "> ");
            }
        } else if (mes instanceof BroadcastMessage) { // Prefix with <Public> if a broadcast
            messageScreen.append(" <Public> ");
        }
        messageScreen.append(": " + mes.getMessage() + "\n"); // Add message and new line
    }

    private static void startClientGUI() {
        Client client = new Client(); // Construct GUI
        client.connectToServer(); // Establish connection with server
        frame = new JFrame("Chat Client");
        positionFrame(client); // Position and display window
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                client.disconnect();
                System.exit(0);
            }
        });
    }

    private static void positionFrame(JPanel panel) {
        frame.setFocusable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame.pack(); //resize frame appropriately for its content
        frame.setLocation(new Point((dimension.width/2)-(frame.getWidth()/2),
                (dimension.height/2)-(frame.getHeight()/2)));   // positions frame in center of screen
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public static void main(String[] args) {
        ConnectScreen con = new ConnectScreen();
        frame = new JFrame("Connect");
        positionFrame(con);
    }

    /**
     * @author Shane Birdsall
     * ID: 14870204
     * The ClientStatusUpdate class was created so that a collection of new clients and a collection of
     * disconnected clients could be paired. It allows updates to be processed easily and for the code
     * to be a lot cleaner.
     */
    private class ClientStatusUpdate {
        ArrayList<String> newClients;
        ArrayList<String> disconnectedClients;

        ClientStatusUpdate() {
            newClients = new ArrayList<>();
            disconnectedClients = connectedClients.getListOfClients();
        }
        ArrayList<String> getNewClients() { return newClients; }
        ArrayList<String> getDisconnectedClients() { return disconnectedClients; }
    }

    /**
     * @author Shane Birdsall
     * ID: 14870204
     * The UpdateStatus class was created for the use of having a thread running to handle status updates.
     * The thread will periodically request updates from the server, and then process those updates as needed.
     */
    private class UpdateStatus implements Runnable {
        DatagramSocket udpSocket;
        boolean initialConnect;

        UpdateStatus() {
            initialConnect = true;
            try {
                udpSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);
                objectOutputStream.writeObject(new RequestUpdateMessage(getClientID()));
                byte[] buffer = byteOutputStream.toByteArray();

                // Used to receive connected client list.
                byte[] receivedStatus = new byte[1024]; // 1024/8 = 128. So should be able to support up to 128 users
                DatagramPacket clientStatus = new DatagramPacket(receivedStatus, receivedStatus.length);
                while (connected) {
                    // Request for an update
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(hostIP), PORT);
                    udpSocket.send(packet); // Send request

                    udpSocket.receive(clientStatus); // Wait for update

                    byte[] receivedUpdate = clientStatus.getData();
                    // Convert byte array into ArrayList
                    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(receivedUpdate);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
                    ArrayList<String> clientList = (ArrayList<String>) objectInputStream.readObject();

                    clientList.remove(getClientID()); // Remove current client from being compared and added to GUI

                    // Compare server status against client status
                    ClientStatusUpdate update = compareServerStatusWithClientStatus(clientList);

                    // Notify client of newly connected users
                    for(String newClient : update.getNewClients()) {
                        connectedClients.add(newClient);
                        if(!initialConnect) { // If just connected don't notify for all clients already connected
                            updateMessageScreen(new NewClientMessage(newClient));
                        }
                    } if(initialConnect) initialConnect = false;
                    // Notify client of users who have disconnected
                    for(String disconnectedClient : update.getDisconnectedClients()) {
                        connectedClients.remove(disconnectedClient);
                        updateMessageScreen(new DisconnectedClientMessage(disconnectedClient));
                    }
                    Thread.sleep(300); // Wait before requesting another update
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @author Shane Birdsall
     * ID: 14870204
     * The ConnectScreen class was created to be used as the entry point to the application.
     * It is a simple GUI that allows users to specify their name/client ID and the host IP address to connect to.
     */
    private static class ConnectScreen extends JPanel implements ActionListener {
        private static final int PANEL_WIDTH = 200, PANEL_HEIGHT = 150;
        JLabel clientIDLabel, IPLabel;
        JTextField clientIDTextField, IPTextField;
        JButton enterButton;

        String getUsername() { return clientIDTextField.getText(); }
        String getHostIP() { return IPTextField.getText(); }

        ConnectScreen() {
            super();
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            clientIDLabel = new JLabel("Username/ID:");
            IPLabel = new JLabel("Host IP:");
            clientIDTextField = new JTextField();
            clientIDTextField.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT/5));
            IPTextField = new JTextField();
            IPTextField.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT/5));
            enterButton = new JButton("Connect");
            enterButton.setPreferredSize(new Dimension(PANEL_WIDTH/2, PANEL_HEIGHT/5));
            enterButton.addActionListener(this);
            add(clientIDLabel);
            add(clientIDTextField);
            add(IPLabel);
            add(IPTextField);
            add(enterButton);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if(source == enterButton) {
                if(getUsername().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter a username!");
                } else if(getHostIP().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter the servers IP Address!");
                } else {
                    setClientID(getUsername());
                    setHostIP(getHostIP());
                    frame.dispose();
                    startClientGUI();
                }
            }
        }
    }
}
