import java.io.IOException;
import java.net.*;
import java.util.HashSet;

public class MultiCastReceiver extends Thread {

    protected HashSet<Neighbor> senders;

    private Boolean stop;

    private InetAddress group;

    private InetAddress inetAddr;

    private Neighbor tester;

    private boolean isTesterBound;

//    private NetworkInterface netInt;

    public MultiCastReceiver(InetAddress inetAddr) {

        this.senders = new HashSet<>();
        this.stop = false;
        this.inetAddr = inetAddr;
        this.tester = null;
        this.isTesterBound = false;
    }

    public HashSet<Neighbor> getSenders() {
        return senders;
    }

    public Neighbor getTester() {
        return tester;
    }

    @Override
    public void run() {

        byte[] buf = new byte[256];

        MulticastSocket socket;

        try {
            socket = new MulticastSocket(4446);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            socket.setInterface(this.inetAddr);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        this.group = null;
        try {
            this.group = InetAddress.getByName("239.0.0.0");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
//        SocketAddress socketAddress = new InetSocketAddress(group.getAddress(), groupPort);
        try {
            socket.joinGroup(this.group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (!stop) {

            DatagramPacket dp = new DatagramPacket(buf, buf.length);

            System.out.println("Multicast on line");

            try {
                socket.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String msg = new String(dp.getData(), 0, dp.getLength());
            System.out.println("Received: " + msg + " from " + dp.getAddress());

            if (msg.contains("tester")) {
                try {
                    tester = new Neighbor(dp.getAddress().getHostAddress());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    senders.add(new Neighbor(dp.getAddress().getHostAddress()));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println("Neighbors: " + senders);
            System.out.println("Tester: " + tester);

            isTesterBound = !isTesterBound();

        }

        try {
            socket.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        socket.close();
    }

    // synchronzed to allow main and multicast receiver to access it
    public synchronized boolean isTesterBound() {
        return isTesterBound;
    }


}