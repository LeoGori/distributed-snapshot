import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println("Hello world!");

        boolean end = false;

        Scanner input = new Scanner(System.in);

        Node n = new Node();
        n.start();

        while (!end) {

            String menu = """
                    1. Send message
                    2. Add connection
                    3. Exit
                    """;
            System.out.println(menu);
            int choice = input.nextInt();

            switch (choice){
                case 1 -> {
                    System.out.println("Please choose the id of the node to send to:");
                    System.out.println(n);
                    int id = input.nextInt();
                    Node dest = n.getNeighbor(id);
                    System.out.println("Please enter the message you want to send:");
                    int msg = input.nextInt();
                    n.addMessage(dest, msg);
                }
                case 2 -> {

                    System.out.println("Please enter the ip of the node to add:");
                    String ip = input.next();
                    Node neighbor = new Node(ip);
                    n.addConnection(neighbor);
                }
                case 3 -> {
                    end = true;
                }
//                case 2 -> {
//                    n2.initSnapshot();
//                }
            }
        }


    }
}