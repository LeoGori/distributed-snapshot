import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

public class Node extends Neighbor {
    protected ReceiverThread receiverThread;

    protected final MultiCastReceiver multiReceiver;

    protected Sender sender;

    protected ChannelManager channelManager;

    public Node() throws IOException {
        super();
        try {
            ipAddr = this.getInterfaces();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        sender = new UdpSender(ipAddr);
        this.receiverThread = new UdpReceiverThread(port);

        receiverThread.register((Observer) this);
        sender.register((Observer) this);

        sender.start();
        this.receiverThread.start();
        this.multiReceiver =  new MultiCastReceiver(this.ipAddr);
        multiReceiver.start();

        channelManager = new ChannelManager();
    }

    public InetAddress getInterfaces() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(interfaces)) {
            displayInterfaceInformation(netint);
            if (netint.getName().equals("enp0s3")) {
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

        Set<Neighbor> nodes = multiReceiver.getSenders();
        System.out.println(nodes);
        Neighbor[] neighbors = nodes.toArray(new Neighbor[nodes.size()]);
        System.out.println(neighbors);
        return neighbors[id];
    }

    public int getPort() {
        return receiverThread.getPort();
    }


    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(super.toString());
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

    public void setChannels() throws UnknownHostException {
        channelManager.setChannels(multiReceiver.getSenders());
        channelManager.setTester(multiReceiver.getTester());
    }

    public HashSet<Neighbor> getChannels() {
        return multiReceiver.getSenders();
    }

}
