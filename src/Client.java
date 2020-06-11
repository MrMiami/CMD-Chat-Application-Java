import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    private Socket clientSocket;
    private DataInputStream dIn;

    private DataOutputStream dOut;
    private String username = "a";
    private String temp_username;



    public void startConnection(String ip, int port){
        try {
            this.clientSocket = new Socket(ip, port);
            dOut = new DataOutputStream(clientSocket.getOutputStream());

            dIn = new DataInputStream(clientSocket.getInputStream());

            System.out.println();
            System.out.println("==== Welcome :D ====");
            System.out.print("Enter an username: ");
            new Read().start();
            new Write().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SetUpUserName(){
        try {
            PrintWriter outPW = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Scanner scanner = new Scanner(System.in);
            String input = clientSocket.getRemoteSocketAddress().toString(); //default username;
            String resp = "invalid";
            input = scanner.nextLine();
            outPW.println(input);
            resp = in.readLine();

            /*while (resp == null || !resp.equals("valid")){
                input = scanner.nextLine();
                outPW.println(input);
                resp = in.readLine();
            }*/
            //scanner.close();
            //in.close();
            //outPW.close();
            this.username = input;
            System.out.println("Nice one, " + this.username);
            this.username = "[" + this.username + "]: ";

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private class Read extends Thread{

        public void run(){
            while (true) {
                try{
                    DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
                    int length = dIn.readInt();
                    if (length > 0) {
                        byte[] message = new byte[length];
                        dIn.readFully(message, 0, message.length); // read the message
                        String s = new String(message);
                        if (username == null && s.equals("valid")){
                            username = temp_username;
                        } else if(username == null && s.equals("invalid")){
                            System.out.print("Enter another user name: ");
                        }
                        else {
                            System.out.println(s);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //System.out.println(username);
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
                    try {
                        if (username == null) {
                            temp_username = input;
                        }
                        else {
                            input = username + input;
                        }
                        dOut.writeInt(input.length());
                        dOut.write(input.getBytes());
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
