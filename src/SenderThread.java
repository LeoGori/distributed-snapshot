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
        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while(!stop) {

            InetAddress recv_ip = neighbor.getIpAddr();
            int recv_port = neighbor.getPort();
//            System.out.println("messages : " + messages);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (!messages.isEmpty()) {
                System.out.println("check for messages");
                String msg = String.valueOf(messages.poll());

                DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), recv_ip, recv_port);
                try {
                    System.out.println("sending " + msg + " to " + recv_ip.toString());
                    socket.send(dp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
        socket.close();
    }

}
