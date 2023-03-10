import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.Queue;

public class Node {
    private int status;

    private ReceiverThread receiverThread;

    private Vector<SenderThread> senderThreads;

    private InetAddress ipAddr;

    private final Vector<Node> neighbors;

    public Node() throws UnknownHostException {
        this.status = 0;
        this.ipAddr = InetAddress.getLocalHost();
        this.receiverThread = new ReceiverThread(8080);
        this.receiverThread.start();
        this.senderThreads = new Vector<>();
        this.neighbors = new Vector<>();
    }

    public Node(String ipAddr) throws UnknownHostException {
        this.status = 0;
        this.ipAddr = InetAddress.getByName(ipAddr);
        this.receiverThread = new ReceiverThread(8080);
        this.receiverThread.start();
        this.senderThreads = new Vector<>();
        this.neighbors = new Vector<>();
    }

    public void addConnection(Node n) {
        neighbors.add(n);
        SenderThread senderThread = new SenderThread(n);
        senderThread.start();
        senderThreads.add(senderThread);
    }

    public Node getNeighbor(int id) {
        return neighbors.get(id);
    }

    public void addMessage(Node dest, int value) {
        int index = neighbors.indexOf(dest);
        senderThreads.get(index).addMessage(value);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public InetAddress getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(InetAddress ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getPort() {
        return receiverThread.getPort();
    }

    @Override public String toString() {
        String string = "Node " + " at " + ipAddr + ":" + receiverThread.getPort() + "\n";
        string += " has neighbors: \n";
        int index = 0;
        for (Node n : neighbors) {
            string += "Node " + index + " at " + n.getIpAddr() + ":" + n.getPort() + "\n";
            index++;
        }
        return string;
    }

}
