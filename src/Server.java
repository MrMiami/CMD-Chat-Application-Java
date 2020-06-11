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
        private String client_username;
        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            sendTo(this.clientSocket, "gg".getBytes());
            byte [] msg = "gg".getBytes();
            DataOutputStream dOut = new DataOutputStream(this.clientSocket.getOutputStream());
            dOut.writeInt(msg.length);
            dOut.write(msg);
            dOut.close();


        }

        private void GetUserName(){
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                String cur_username;
                while (true){
                    if((cur_username = in.readLine()) != null) {
                        if (user_name.contains(cur_username)){
                            out.println("invalid");
                        } else{
                            out.println("valid");
                            user_name.add(cur_username);
                            break;
                        }
                    }
                }
                out.close();
                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        public void run(){
            try {
                dIn = new DataInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true) {
                try {
                    int length = dIn.readInt();
                    if (length >= 0) {
                        byte [] message = new byte[length];
                        dIn.readFully(message, 0, message.length);
                        String str = new String(message);
                        test_str = str;
                        System.out.println("[Client says]: " + str);

                        // Doesnt have username
                        /*if (!clients.containsKey(clientSocket.getRemoteSocketAddress().toString())
                        && user_name.contains(str)){
                            System.out.println("[A]");
                            sendTo(this.clientSocket, "invalid".getBytes());
                        } else if (!clients.containsKey(clientSocket.getRemoteSocketAddress().toString())
                        && !user_name.contains(str)){
                            System.out.println("[B]");
                            clients.put(clientSocket.getRemoteSocketAddress().toString(), clientSocket);
                            user_name.add(str);
                            sendTo(this.clientSocket, "valid".getBytes());
                        }*/
                        //else {

                            for (String s : clients.keySet()) {
                                if (!s.equals(clientSocket.getRemoteSocketAddress().toString())) {
                                    sendTo(clients.get(s), message);
                                }
                            }
                        //}
                    }
                } catch (IOException e) {
                    //System.out.println(test_str);
                    e.printStackTrace();
                }

            }
        }



        public void sendTo(Socket receiver_socket, byte [] msg){
            try {
                DataOutputStream dOut = new DataOutputStream(receiver_socket.getOutputStream());
                dOut.writeInt(msg.length);
                dOut.write(msg);
                dOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

}
