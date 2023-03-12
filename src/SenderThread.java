import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Queue;
import java.util.LinkedList;

public class SenderThread extends Thread {
    private final Neighbor neighbor;

    private final boolean stop;

    private final Queue<Integer> messages;

    public SenderThread(Neighbor neighbor) {
        this.neighbor = neighbor;
        this.messages = new LinkedList<>();
        this.stop = false;
    }

    public void addMessage(int value) {
        messages.add(value);
        System.out.println("Added value of " + value + " to messages");
    }

    public void run() {

    }

}
