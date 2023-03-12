import java.io.IOException;
import java.net.*;
import java.util.Vector;

public class Node extends Neighbor {

    private final ReceiverThread receiverThread;

    private final Vector<Neighbor> neighbors;

    public Node() throws UnknownHostException {
        super();
        this.port = 12000;
        this.ipAddr = InetAddress.getLocalHost();
        this.receiverThread = new ReceiverThread(port);
        this.receiverThread.start();
        this.neighbors = new Vector<>();
    }

    public Node(String ipAddr) throws UnknownHostException {
        super(ipAddr);
        this.port = 12000;
        this.receiverThread = new ReceiverThread(port);
        this.receiverThread.start();
        this.neighbors = new Vector<>();
    }

    public void addConnection(Neighbor n) {
        neighbors.add(n);
    }

    public void addMessage(Neighbor dest, int value) {
        int index = neighbors.indexOf(dest);
        System.out.println(index);

        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        InetAddress recv_ip = dest.getIpAddr();
        int recv_port = dest.getPort();
//            System.out.println("messages : " + messages);
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String msg = Integer.toString(value);

        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), recv_ip, recv_port);
        try {
            System.out.println("sending " + msg + " to " + recv_ip.toString());
            socket.send(dp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        socket.close();
    }

    public Neighbor getNeighbor(int id) {
        return neighbors.get(id);
    }

    public int getPort() {
        return receiverThread.getPort();
    }

    @Override public String toString() {
        StringBuilder string = new StringBuilder("Node " + " at " + ipAddr + ":" + port + "\n");
        string.append(" has neighbors: \n");
        int index = 0;
        for (Neighbor n : neighbors) {
            string.append("Node ").append(index).append(" at ").append(n.getIpAddr()).append(":").append(port).append("\n");
            index++;
        }
        return string.toString();
    }

}
