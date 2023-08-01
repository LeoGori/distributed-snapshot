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

        MulticastSocket multiSocket = null;
        try {
            multiSocket = new MulticastSocket(4446);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InetAddress group = null;
        try {
            group = InetAddress.getByName("230.0.0.0");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        try {
            multiSocket.joinGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (!stop) {
            DatagramPacket dp = new DatagramPacket(buf, buf.length);

            try {
                multiSocket.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String msg = new String(dp.getData(), 0, dp.getLength());
            System.out.println("Received: " + msg + " from " + dp.getAddress().getHostAddress());

            try {
                senders.add(new Neighbor(dp.getAddress().getHostAddress()));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Neighbors: " + senders);
        }

        try {
            multiSocket.leaveGroup(group);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        multiSocket.close();
    }
}