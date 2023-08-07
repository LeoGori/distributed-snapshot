import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Set;


public class Node extends Neighbor {

    private final ReceiverThread receiverThread;

    private final MultiCastReceiver multiReceiver;

    private final Sender sender;

    public Node() throws UnknownHostException, SocketException {
        super();
        try {
            ipAddr = this.getInterfaces();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.receiverThread = new ReceiverThread(port);
        this.receiverThread.start();
        this.multiReceiver =  new MultiCastReceiver(this.ipAddr);
        multiReceiver.start();
        sender = new Sender(ipAddr);
        sender.start();
    }

    public InetAddress getInterfaces() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(interfaces)) {
            displayInterfaceInformation(netint);
            if (netint.getName().equals("wlan0")) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        }
        return null;
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.println("Display name:" + netint.getDisplayName());
        System.out.println("Name: " + netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.println("InetAddress: " + inetAddress);
        }
        System.out.println("\n");
    }

    public Neighbor getNeighbor(int id) {

        Set<Neighbor> node = multiReceiver.getSenders();
        Neighbor[] neighbors = node.toArray(new Neighbor[node.size()]);
        return neighbors[id];
    }

    public int getPort() {
        return receiverThread.getPort();
    }

    public void sendMessage(Neighbor n, String message) {
        sender.addMessage(n, message);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Node " + " at " + ipAddr + ":" + port + "\n");
        string.append(" has neighbors: \n");
        int index = 0;
        for (Neighbor n : multiReceiver.getSenders()) {
            string.append("Node ").append(index).append(" at ").append(n.getIpAddr()).append(":").append(port).append("\n");
            index++;
        }
        return string.toString();
    }

    public Sender getSender() {
        return sender;
    }



//    public void initSnapshot() {
//        for (Neighbor n : multiReceiver.getSenders()) {
//            addMessage(n, 0);
//        }
//    }

//    public Vector<int> getStatus() {
//        int status = receiverThread.getStatus();
//        System.out.println("Status: " + status);
//    }
}
