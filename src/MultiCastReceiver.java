import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class MultiCastReceiver extends Thread {

    protected Set<Neighbor> senders;

    private Boolean stop;

    private MulticastSocket socket;

    private InetAddress group;

    public MultiCastReceiver() {

        this.senders = new HashSet<Neighbor>();
        this.stop = false;

        this.socket = null;
        try {
            this.socket = new MulticastSocket(4446);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.group = null;
        try {
            this.group = InetAddress.getByName("230.0.0.0");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        try {
            this.socket.joinGroup(this.group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Neighbor> getSenders() {
        return senders;
    }

    @Override
    public void run() {

        while (!stop) {
            byte[] buf = new byte[256];
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