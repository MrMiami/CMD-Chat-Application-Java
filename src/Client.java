import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    private Socket clientSocket;
    private DataInputStream dIn;
    private DataOutputStream dOut;
    private String username;
    private Scanner scanner;



    public void startConnection(String ip, int port){
        try {
            this.clientSocket = new Socket(ip, port);
            this.username = this.clientSocket.getRemoteSocketAddress().toString(); // default username
            this.dOut = new DataOutputStream(clientSocket.getOutputStream());
            this.dIn = new DataInputStream(clientSocket.getInputStream());
            this.scanner = new Scanner(System.in);
            System.out.println();
            System.out.println("==== Welcome :D ====");
            SetUpUserName();
            // Generate 2 different threads to read incoming messages and to send out messages to ClientHandler
            new Read().start();
            new Write().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SetUpUserName(){
        System.out.print("Enter an username: ");
        String input = "";
        String server_response = "";
        // keep prompting user until the user choose a valid username
        // (an username that has not been taken)
        // user will choose an username and sends it to the server
        // the server will decide if the username is valid or not by sending
        // a response "valid" or "invalid"
        while (!server_response.equals("valid")){
            input = this.scanner.nextLine();
            SendMessage(input);
            server_response = ReadMessage();
            if (server_response.equals("invalid")){
                System.out.print("This name has already been taken, choose another one: ");
            }
            else {
                this.username = "[" + input + "]: ";
                System.out.println("Nice name, " + input + ". You're ready to chat!");
            }
        }
    }

    private String ReadMessage() {
        String result = null;
        try {
            int length = dIn.readInt();
            if (length > 0) {
                byte[] message = new byte[length];
                dIn.readFully(message, 0, message.length); // read the message
                result = new String(message);
            }
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
        return result;
    }

    private void SendMessage(String msg){
        try {
            dOut.writeInt(msg.length());
            dOut.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private class Read extends Thread{
        public void run(){
            while (true) {
                String message = ReadMessage();
                if (message != null) System.out.println(message);
            }
        }
    }
    private class Write extends Thread{
        public void run(){
            Scanner scanner = new Scanner(System.in);
            String input;
            while(true){
                input = scanner.nextLine();
                if (input != null){
                    input = username + input;
                    System.out.println(input);
                    SendMessage(input);
                }
            }
        }
    }
}
