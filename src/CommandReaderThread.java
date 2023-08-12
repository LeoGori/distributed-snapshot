import java.util.Scanner;

public class CommandReaderThread extends Thread {

    private boolean end;

    public CommandReaderThread() {
        this.end = false;
    }

    public void run() {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            String command = scanner.nextLine();

            if (command.equals("q")) {
                end = true;
            }
        }

    }

    public boolean getEnd() {
        return end;
    }
}
