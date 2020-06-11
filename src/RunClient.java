import java.io.IOException;
import java.util.Scanner;

public class RunClient {
    public static void main(String [] args) throws IOException {
        Client client = new Client();
        client.startConnection("localhost", 12345);
    }
}
