import java.util.Scanner;

public class CommandReaderThread extends Thread {

    private boolean end;

    private Scanner scanner;

    public CommandReaderThread() {

        this.end = false;
        this.scanner = new Scanner(System.in);
    }

    public void run() {

        while (!end) {

            scanner.nextLine();
            String command = scanner.nextLine();

            if (command.equals("q")) {
                end = true;
            }
        }

        scanner.close();

    }

    public boolean getEnd() {
        return end;
    }
}
