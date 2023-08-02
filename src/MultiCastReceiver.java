import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class MultiCastReceiver extends Thread {

    protected Set<Neighbor> senders;

    private Boolean stop;

    private InetAddress group;

    public MultiCastReceiver() {

        this.senders = new HashSet<Neighbor>();
        this.stop = false;


    }

    public Set<Neighbor> getSenders() {
        return senders;
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
        this.group = null;
        try {
            this.group = InetAddress.getByName("230.0.0.0");
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
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true);
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

            try {
                senders.add(new Neighbor(dp.getAddress().getHostAddress()));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Neighbors: " + senders);
        }

        try {
            socket.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        socket.close();
    }
}