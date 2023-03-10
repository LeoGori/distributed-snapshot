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
    }

    public void run() {

        while(!stop) {
            InetAddress recv_ip = neighbor.getIpAddr();
            int recv_port = neighbor.getPort();

            DatagramSocket socket;
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }

            String msg = String.valueOf(messages.poll());

            DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), recv_ip, recv_port);
            try {
                socket.send(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            socket.close();
        }

    }

}
