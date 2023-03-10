import java.net.UnknownHostException;
import java.util.Vector;

public class Node extends Neighbor {

    private final ReceiverThread receiverThread;

    private final Vector<SenderThread> senderThreads;
    private final Vector<Neighbor> neighbors;

    public Node() throws UnknownHostException {
        super();
        this.port = 12000;
        this.receiverThread = new ReceiverThread(port);
        this.receiverThread.start();
        this.senderThreads = new Vector<>();
        this.neighbors = new Vector<>();
    }

    public Node(String ipAddr) throws UnknownHostException {
        super(ipAddr);
        this.port = 12000;
        this.receiverThread = new ReceiverThread(port);
        this.receiverThread.start();
        this.senderThreads = new Vector<>();
        this.neighbors = new Vector<>();
    }

    public void addConnection(Neighbor n) {
        neighbors.add(n);
        SenderThread senderThread = new SenderThread(n);
        senderThread.start();
        senderThreads.add(senderThread);
    }

    public void addMessage(Neighbor dest, int value) {
        int index = neighbors.indexOf(dest);
        System.out.println(index);
        senderThreads.get(index).addMessage(value);
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
