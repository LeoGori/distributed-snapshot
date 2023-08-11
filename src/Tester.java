import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Tester extends Node implements Observer {

    private HashMap<InetAddress, HashSet<Snapshot>> incrementalSnapshots;

    private HashSet<Snapshot> lastSnapshot;

    public Tester(String ipAddr) throws UnknownHostException, SocketException {
        incrementalSnapshots = new HashMap<>();
        lastSnapshot = new HashSet<>();
    }

    public void update() throws UnknownHostException {
        DatagramPacket dp = receiverThread.getDatagramPacket();

        String msg = new String(dp.getData(), 0, dp.getLength());

        System.out.println("Received: " + msg + " from " + dp.getAddress());

        Snapshot snapshot = new Snapshot(dp);

        InetAddress senderAddress = dp.getAddress();
        InetAddress initiator = snapshot.getInitiator();

        if (!incrementalSnapshots.keySet().contains(initiator)) {
            incrementalSnapshots.put(initiator, new HashSet<>());
        }

        incrementalSnapshots.get(initiator).add(snapshot);

        for (InetAddress init : incrementalSnapshots.keySet()) {
            if (incrementalSnapshots.get(init).size() == inputChannelManager.getChannels().size()) {
                System.out.println(incrementalSnapshots.get(init));
                lastSnapshot = incrementalSnapshots.get(init);
            }
        }
    }

}
