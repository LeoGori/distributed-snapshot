import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;

public class Tester extends Node implements Observer {

    private HashMap<InetAddress, HashMap<InetAddress, Snapshot>> incrementalSnapshots;

    private HashMap<InetAddress, Snapshot> lastSnapshot;

    public Tester() throws UnknownHostException, SocketException {
        incrementalSnapshots = new HashMap<>();
        lastSnapshot = new HashMap();
    }

    public void update() throws UnknownHostException {
        DatagramPacket dp = receiverThread.getDatagramPacket();

        String msg = new String(dp.getData(), 0, dp.getLength());

        System.out.println("Received: " + msg + " from " + dp.getAddress());

        Snapshot snapshot = new Snapshot(dp);

        InetAddress senderAddress = dp.getAddress();
        InetAddress initiator = snapshot.getInitiator();

        if (!incrementalSnapshots.keySet().contains(initiator)) {
            incrementalSnapshots.put(initiator, new HashMap<>());
        }

        incrementalSnapshots.get(initiator).put(senderAddress, snapshot);

        for (InetAddress init : incrementalSnapshots.keySet()) {
            if (incrementalSnapshots.get(init).keySet().size() == inputChannelManager.getChannels().size()) {
                System.out.println(incrementalSnapshots.get(init));
                lastSnapshot = incrementalSnapshots.get(init);

                if (checkConsistency()) {
                    System.out.println("Consistent snapshot");
                } else {
                    System.out.println("Inconsistent snapshot");
                }
            }
        }
    }

    public boolean checkConsistency() {
        int totalBalance = 0;
        for (InetAddress addr : lastSnapshot.keySet()) {
            totalBalance += lastSnapshot.get(addr).getBalance();
        }
        return totalBalance == 0;
    }

}
