import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;


public class Tester extends Node implements Observer {

    private HashMap<String, HashMap<InetAddress, Snapshot>> incrementalSnapshots;

    private HashMap<InetAddress, Snapshot> lastSnapshot;

    private HashSet<InetAddress> incrementalChannels;

    public Tester() throws IOException {
        super();

        incrementalSnapshots = new HashMap<>();
        lastSnapshot = new HashMap();
    }

    public void update(Subject subject) throws UnknownHostException {
        Packet p = receiverThread.getPacket();

        String msg = p.getMsg();
        System.out.println("Received: " + msg + " from " + p.getIpAddr());

        Snapshot snapshot = new Snapshot(p);

        InetAddress senderAddress = p.getIpAddr();
        InetAddress initiator = snapshot.getInitiator();

        HashSet<Neighbor> borderList = snapshot.getBorderList();

//        String inits;
//        if (!borderList.isEmpty()) {
//            ArrayList<String> initiators = new ArrayList<>();
//            initiators.add(initiator.getHostAddress());
//            for (Neighbor n : borderList) {
//                initiators.add(n.getIpAddr().getHostAddress());
//            }
//            Collections.sort(initiators);
//            inits = String.join("-", initiators);
//        } else {
//            inits = initiator.getHostAddress();
//        }

        String inits = initiator.getHostAddress();

        if (!incrementalSnapshots.containsKey(inits)) {
            incrementalSnapshots.put(inits, new HashMap<>());
            System.out.println("added incremental snapshot on key " + inits);
        }

        incrementalSnapshots.get(inits).put(senderAddress, snapshot);

//        assert incrementalSnapshots.keySet().size() <= 1;

//        String init = incrementalSnapshots.keySet().iterator().next();

        // generate set of ipAddresses from the list of neighbors of input channel manager

        Set<InetAddress> channels = channelManager.getChannels().stream()
                .map(Neighbor::getIpAddr)
                .collect(Collectors.toSet());

//            System.out.println(incrementalSnapshots);

//            System.out.println(channels);
//            System.out.println(incrementalSnapshots.get(init).keySet());

//        if (incrementalSnapshots.get(init).keySet().equals(channels)) {
        if (incrementalChannels.equals(channels)) {
            lastSnapshot = new HashMap<>();
            System.out.println(incrementalSnapshots);
            for (String key : incrementalSnapshots.keySet()) {
                lastSnapshot.putAll(incrementalSnapshots.get(key));
            }
//            lastSnapshot = incrementalSnapshots.get(inits);
//                incrementalSnapshots.remove(init);

            if (checkConsistency()) {
                System.out.println("Consistent snapshot");
            } else {
                System.out.println("Inconsistent snapshot");
            }
            incrementalSnapshots.clear();
        }
    }

    public boolean checkConsistency() {
        int totalBalance = 0;
        for (InetAddress addr : lastSnapshot.keySet()) {
            totalBalance += lastSnapshot.get(addr).getBalance();
        }
        return totalBalance == 0;
    }

    public void setTransmissionProtocol(String type) throws IOException, InterruptedException {

        channelManager.setChannels(multiReceiver.getSenders());
        channelManager.setTester(multiReceiver.getTester());

        sender.interrupt();
        receiverThread.closeSocket();
        receiverThread.interrupt();

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
        sender.register(this);

        sender.start();
        receiverThread.start();
    }

    public void automaticSnapshot() {

        if (sender instanceof TcpSender) {

            ArrayList<Neighbor> destinations = new ArrayList<>();
            ArrayList<String> messages = new ArrayList<>();

            for (Neighbor neighbor : channelManager.getChannels()) {
                destinations.add(neighbor);
                messages.add("automatic_mode");
            }

            sender.addMessage(destinations, messages);

        }

    }

}
