import java.io.IOException;
import java.util.Scanner;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Hello world!");

        boolean isTester = false;

        if (isTester)
            testerMenu();
        else
            snapshotNodeMenu();

        System.exit(0);
    }

    public static void snapshotNodeMenu() throws IOException, InterruptedException {

        SnapshotNode n = new SnapshotNode();

        boolean end = false;

        Scanner input = new Scanner(System.in);

        while (!end) {
            System.out.println("Initialization phase");
            String menu = """
                    1. Notify neighbors
                    2. Exit
                    """;
//            String menu = "1. Send message\n2. Require snapshot\n3. Pass\n4. Exit ";
            System.out.println(menu);

            System.out.println(n);

            int choice = input.nextInt();

            System.out.println(n.getClass());

            switch (choice) {
                case 1 -> {
                    n.getSender().multicastOwnIP("hello");
                }
                case 2 -> end = true;
            }

        }

        end = false;

        n.setChannels();

        while (!end) {

            String menu = """
                    1. Send message
                    2. Request snapshot
                    3. Test distributed snapshot
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

                case 3 -> {
                    automaticSnapshot(n);
                }

                case 4 -> {
                    end = true;
                }

            }
        }
    }

    public static void automaticSnapshot(SnapshotNode n) throws InterruptedException {

        boolean end = false;

        CommandReaderThread rct = new CommandReaderThread();

        rct.start();

        Random rand = new Random();

        int upperbound = 10;

        while (!rct.getEnd()) {

            if (rand.nextFloat() < 0.1) {
                n.initSnapshot();
            }
            else {

                int numberOfNeighbors = n.getChannels().size();

                int randomNeighbor = rand.nextInt(numberOfNeighbors);

                Neighbor dest = n.getNeighbor(randomNeighbor);

                int randomMessage = rand.nextInt(upperbound);
                randomMessage -= upperbound / 2;

                String msg = String.valueOf(randomMessage);
                n.sendMessage(dest, msg);
            }

            Thread.sleep(5000);
        }
    }

    public static void testerMenu() throws IOException {

        Tester t = new Tester();

        boolean end = false;

        Scanner input = new Scanner(System.in);

        while (!end) {
            System.out.println("Initialization phase");
            String menu = """
                    1. Notify neighbors
                    2. Exit
                    """;
//            String menu = "1. Send message\n2. Require snapshot\n3. Pass\n4. Exit ";
            System.out.println(menu);

            System.out.println(t);

            int choice = input.nextInt();

            switch (choice) {
                case 1 -> {
                    t.getSender().multicastOwnIP("tester");
                }
                case 2 -> end = true;
            }

        }
    }
}