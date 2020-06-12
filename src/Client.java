import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    private Socket clientSocket;
    private DataInputStream dIn;

    private DataOutputStream dOut;
    private String username;



    public void startConnection(String ip, int port){
        try {
            this.clientSocket = new Socket(ip, port);
            this.username = this.clientSocket.getRemoteSocketAddress().toString(); // default username
            dOut = new DataOutputStream(clientSocket.getOutputStream());
            dIn = new DataInputStream(clientSocket.getInputStream());
            System.out.println();
            System.out.println("==== Welcome :D ====");
            SetUpUserName();
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
        Scanner scanner = new Scanner(System.in);
        while (!server_response.equals("valid")){
            input = scanner.nextLine();
            try {
                dOut.writeInt(input.length());
                dOut.write(input.getBytes());
                int length = dIn.readInt();
                if (length > 0){
                    byte [] message = new byte[length];
                    dIn.readFully(message, 0, message.length);
                    server_response = new String(message);
                }
                if (server_response.equals("invalid")){
                    System.out.print("This name has already been taken, choose another one: ");
                }
                else {
                    this.username = "[" + input + "]: ";
                    System.out.println("Nice name, " + input + ". You're ready to chat!");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //this.username = "[" + input + "]: ";
        //System.out.println("Nice name, " + input + ". You're ready to chat!");
    }


    private class Read extends Thread{
        public void run(){
            while (true) {
                try{
                    int length = dIn.readInt();
                    if (length > 0) {
                        byte[] message = new byte[length];
                        dIn.readFully(message, 0, message.length); // read the message
                        String s = new String(message);
                        System.out.println(s);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
                    try{
                        System.out.println(input);
                        dOut.writeInt(input.length());
                        dOut.write(input.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
