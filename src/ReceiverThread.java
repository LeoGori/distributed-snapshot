import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.LinkedList;

public class ReceiverThread extends Thread {

    private int port;
    private Queue<Integer> messages;

    private boolean stop;

    public ReceiverThread(int port) {
        this.port = port;
        this.messages = new LinkedList<>();
    }

    public int popMessage() {
        return messages.poll().intValue();
    }

    public int getPort() {
        return port;
    }


    public void run() {
        while (!stop) {
            System.out.println("Waiting for message...");
            DatagramSocket socket;
            try {
                socket = new DatagramSocket(port);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            byte[] buf = new byte[256];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String msg = new String(dp.getData(), 0, dp.getLength());
            System.out.println("Received: " + msg);
            socket.close();
        }
    }
}
