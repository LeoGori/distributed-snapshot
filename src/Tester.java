import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Tester extends Node implements Observer {

    private HashMap<InetAddress, HashMap<InetAddress, Snapshot>> incrementalSnapshots;

    private HashMap<InetAddress, Snapshot> lastSnapshot;

    public Tester() throws IOException {
        super();

        sender = new UdpSender(ipAddr);
        this.receiverThread = new UdpReceiverThread(port);

        incrementalSnapshots = new HashMap<>();
        lastSnapshot = new HashMap();
    }

    public void update() throws UnknownHostException {
        Packet p = receiverThread.getPacket();

        String msg = p.getMsg();
        System.out.println("Received: " + msg + " from " + p.getIpAddr());

        Snapshot snapshot = new Snapshot(p);

        InetAddress senderAddress = p.getIpAddr();
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

    public void setTransmissionProtocol(String type) throws IOException {
        if (type.equals("udp")) {
            sender = new UdpSender(ipAddr);
            receiverThread = new UdpReceiverThread(port);

        }
        else if (type.equals("tcp")) {
            sender = new TcpSender(ipAddr);
            receiverThread = new TcpReceiverThread(port);
        }
        else {
            throw new RuntimeException("Invalid sender type");
        }
        receiverThread.register(this);

        sender.start();
        receiverThread.start();
    }

}
