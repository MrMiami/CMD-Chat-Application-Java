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
    private byte [] PROMPT_USERNAME;
    private byte [] REPROMPT_USERNAME;
    private Map<String, Socket> clients;
    private Set<String> user_name;
    private String test_str;

    public void start (){
        clients = new HashMap<>();
        try {
            serverSocket = new ServerSocket(PORT);
            user_name = new HashSet<>();
            this.PROMPT_USERNAME = "Choose a cool username: \n".getBytes();
            this.REPROMPT_USERNAME = "This name has already been used, choose a better than them: \n".getBytes();
            System.out.println("Server running...");
            while(true){
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

        private void GetUserName() {
            String server_response = "invalid";
            try {
                //thisdOut = new DataOutputStream(this.clientSocket.getOutputStream());
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
                try {
                    int length = dIn.readInt();
                    if (length >= 0) {
                        byte [] message = new byte[length];
                        dIn.readFully(message, 0, message.length);
                        //String str = new String(message);
                        //System.out.println("[Client says]: " + str);
                        for (String s : clients.keySet()) {
                            if (!s.equals(clientSocket.getRemoteSocketAddress().toString())) {
                                sendTo(clients.get(s), message);
                            }
                        }
                    }
                } catch (IOException e) {
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
