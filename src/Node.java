import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Set;


public class Node extends Neighbor {

    private final ReceiverThread receiverThread;

    private MultiCastReceiver multiReceiver;

    private final DatagramSocket socket;

    private NetworkInterface netInt;

    public Node() throws UnknownHostException {
        super();
        this.port = 12000;
        this.ipAddr = InetAddress.getLocalHost();
        try {
            this.getInterfaces();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.receiverThread = new ReceiverThread(port);
        this.receiverThread.start();
        this.multiReceiver =  new MultiCastReceiver();
        multiReceiver.start();
        try {
            socket = new DatagramSocket(port+1);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public Node(String ipAddr) throws UnknownHostException {
        super(ipAddr);
        this.port = 12000;
        this.receiverThread = new ReceiverThread(port);
        this.receiverThread.start();
        this.multiReceiver =  new MultiCastReceiver();
        multiReceiver.start();
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void getInterfaces() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(interfaces)) {
            displayInterfaceInformation(netint);
            if (netint.getName().equals("wlan0")) {
                this.netInt = netint;
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (inetAddress instanceof Inet4Address) {
                        this.ipAddr = inetAddress;
                    }
                }
            }
        }
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

    public void MulticastOwnIP() throws IOException {

        InetAddress group;
        byte[] buf;

        group = InetAddress.getByName("230.0.0.0");

        MulticastSocket multisocket = new MulticastSocket(4446);
//        multisocket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);


        String multicastMessage = "Hello";
        buf = multicastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, group, 4446);
        multisocket.send(packet);
//        socket.close();

        System.out.println("Multicast message sent to group: " + packet.getAddress());

    }

    public void sendMessage(Neighbor dest, int value) {

        InetAddress dest_ip = dest.getIpAddr();
        int recv_port = dest.getPort();
//            System.out.println("messages : " + messages);
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String msg = Integer.toString(value);

        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), dest_ip, recv_port);
        try {
            System.out.println("sending " + msg + " to " + dest_ip.toString());
            socket.send(dp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Neighbor getNeighbor(int id) {

        Set<Neighbor> node = multiReceiver.getSenders();
        Neighbor[] neighbors = node.toArray(new Neighbor[node.size()]);
        return neighbors[id];
    }

    public int getPort() {
        return receiverThread.getPort();
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

    public void sendMessage_2(String destinationIP, int value) throws UnknownHostException {

        InetAddress dest_ip = InetAddress.getByName(destinationIP);
        int recv_port = 12000;
//            System.out.println("messages : " + messages);
//        try {
//            Thread.sleep(1);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        String msg = Integer.toString(value);

        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), dest_ip, recv_port);
        try {
            System.out.println("sending " + msg + " to " + dest_ip.toString());
            socket.send(dp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public void initSnapshot() {
//        for (Neighbor n : neighbors) {
//            addMessage(n, 0);
//        }
//    }

//    public Vector<int> getStatus() {
//        int status = receiverThread.getStatus();
//        System.out.println("Status: " + status);
//    }
}
