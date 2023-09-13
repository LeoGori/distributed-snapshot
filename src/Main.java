import java.io.IOException;
import java.util.Scanner;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {

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

            System.out.println(menu);

            System.out.println(n);

            int choice = input.nextInt();

//            System.out.println(n.getClass());

            switch (choice) {
                case 1 -> {
                    ((UdpSender) n.getSender()).multicastOwnIP("hello");
                }
                case 2 -> end = true;
            }


        }

        end = false;

        n.setChannels();
        n.setTransmissionProtocol("tcp");

        while (!end) {

            String menu = """
                    1. Send message
                    2. Request snapshot
                    3. Test distributed snapshot
                    4. Exit
                    """;

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

//        CommandReaderThread rct = new CommandReaderThread();

//        rct.start();

        Random rand = new Random();

        int upperbound = 10;

        boolean end = false;
        float snapshotProbability = 0.2F;


        while (!end || n.isSnapshotInProgress()) {

            float randomFloat = rand.nextFloat();
//            System.out.println(randomFloat);

            if (randomFloat < snapshotProbability ) {
                n.initSnapshot();
            }
            else {

                int numberOfNeighbors = n.getChannels().size();
//                System.out.println(numberOfNeighbors);

                int randomNeighbor = rand.nextInt(numberOfNeighbors);

                Neighbor dest = n.getNeighbor(randomNeighbor);

                int randomMessage = rand.nextInt(upperbound) - upperbound / 2;

//                System.out.println(randomMessage);

                String msg = String.valueOf(randomMessage);
                n.sendMessage(dest, msg);
            }

            if (n.isSnapshotInProgress() && !end) {
                System.out.println("Snapshot in progress (end= " + end + ", SiP= " + n.isSnapshotInProgress() + ")");
                end = true;
                snapshotProbability = 0.0F;
            }



            Thread.sleep(2500);
        }

//        rct.interrupt();
        System.out.println("Exiting automatic snapshot");

    }

    public static void testerMenu() throws IOException, InterruptedException {

        Tester t = new Tester();

        boolean end = false;

        Scanner input = new Scanner(System.in);

        while (!end) {
            System.out.println("Initialization phase");
            String menu = """
                    1. Notify neighbors
                    2. Switch transmission mode
                    3. Exit
                    """;

            System.out.println(menu);

            System.out.println(t);

            int choice = input.nextInt();

            switch (choice) {
                case 1 -> {
                    ((UdpSender) t.getSender()).multicastOwnIP("tester");
                }

                case 2 -> {
                    System.out.println("Please enter the transmission mode (udp/tcp):");
                    input.nextLine();
                    String mode = input.nextLine();
                    t.setTransmissionProtocol(mode);
                }

                case 3 -> end = true;
            }


        }
    }
}