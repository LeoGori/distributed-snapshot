import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        System.out.println("Hello world!");

        boolean end = false;

        Scanner input = new Scanner(System.in);

        Node n = new Node();

        while(!end) {
            System.out.println("Initialization phase");
            String menu = """
                    1. Notify neighbors
                    2. Exit
                    """;
//            String menu = "1. Send message\n2. Require snapshot\n3. Pass\n4. Exit ";
            System.out.println(menu);

            System.out.println(n);

            int choice = input.nextInt();

            switch (choice) {
                case 1 -> n.getSender().multicastOwnIP();
                case 2 -> end = true;
            }

        }

        end = false;

        n.setChannels();

        while (!end) {

            String menu = """
                    1. Send message
                    2. Request snapshot
                    3. Pass
                    4. Exit
                    """;
//            String menu = "1. Send message\n2. Require snapshot\n3. Pass\n4. Exit ";
            System.out.println(menu);

            System.out.println(n);

            int choice = input.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.println("Please choose the id of the node to send to:");
                    int id = input.nextInt();
                    Neighbor dest = n.getNeighbor(id);
                    System.out.println("Please enter the message you want to send:");
                    input.nextLine();
                    String msg = input.nextLine();
                    n.sendMessage(dest, msg);
                }

                case 2 -> {
                    n.initSnapshot();
                }


//                case 4 -> {
////                    n.initSnapshot();
//                    System.out.println("Please enter the destination IP:");
//                    input.nextLine();
//                    String destIP = input.nextLine();
//                    System.out.println("Please choose the message you want to send:");
//                    int val = input.nextInt();
//                    n.sendMessage_2(destIP, val);
//                }


                case 3 -> {
                    System.out.println("Pass");
                }

                case 4 -> {
                    end = true;
                }

            }
        }
        System.exit(0);
    }
}