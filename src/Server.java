import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Server {
    private ServerSocket serverSocket;
    private static final int PORT = 12345;
    private Map<String, Socket> clients;
    private Set<String> user_name;

    public void start (){
        clients = new HashMap<>();
        try {
            serverSocket = new ServerSocket(PORT);
            user_name = new HashSet<>();
            System.out.println("Server is established...");
            while(true){
                // Waiting and accepting connection from clients
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Connect From RemoteSoketAddress]: " + (clientSocket.getRemoteSocketAddress()));
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public class ClientHandler extends Thread {
        private Socket clientSocket;
        private DataInputStream dIn;
        private DataOutputStream dOut;
        private String username;
        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
        }



        public void run(){
            try {
                dIn = new DataInputStream(clientSocket.getInputStream());
                dOut = new DataOutputStream(clientSocket.getOutputStream());
                GetUserName();
                clients.put(this.clientSocket.getRemoteSocketAddress().toString(), this.clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true) {
                // Distribute messages sent from this user to all other
                // users in the chat room (exclude the sender)
                try {
                    int length = dIn.readInt();
                    if (length >= 0) {
                        byte [] message = new byte[length];
                        dIn.readFully(message, 0, message.length);
                        for (String s : clients.keySet()) {
                            if (!s.equals(clientSocket.getRemoteSocketAddress().toString())) {
                                sendTo(clients.get(s), message);
                            }
                        }
                    }
                }
                // When this client disconnect from the chat room
                // close he client's socket and remove all client's information
                catch (IOException e) {
                    user_name.remove(username);
                    clients.remove(this.clientSocket.getRemoteSocketAddress().toString());
                    System.out.println("[Connection remove]: " +
                            this.clientSocket.getRemoteSocketAddress().toString());
                    try {
                        dIn.close();
                        dOut.close();
                        this.clientSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    return;
                }
            }
        }

        private void GetUserName() {
            String server_response = "invalid";
            try {
                // read username sent from users, and check to see if it's that username
                // is taken. sending "invalid" message to users
                // until they give a valid username
                while (!server_response.equals("valid")) {
                    int length = dIn.readInt();
                    if (length > 0) {
                        byte[] message = new byte[length];
                        dIn.readFully(message, 0, message.length);
                        String cur_username = new String(message);
                        if (!user_name.contains(cur_username)) {
                            server_response = "valid";
                            user_name.add(cur_username);
                            this.username = cur_username;
                        }
                        dOut.writeInt(server_response.length());
                        dOut.write(server_response.getBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // send msg to specified sockets
        private void sendTo(Socket receiver_socket, byte [] msg){
            try {
                DataOutputStream dOut = new DataOutputStream(receiver_socket.getOutputStream());
                dOut.writeInt(msg.length);
                dOut.write(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
