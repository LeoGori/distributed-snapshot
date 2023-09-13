import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;


public class Tester extends Node implements Observer {

    private HashMap<InetAddress, HashMap<InetAddress, Snapshot>> incrementalSnapshots;

    private HashMap<InetAddress, Snapshot> lastSnapshot;

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


        String inits;
        if (!borderList.isEmpty()){
            ArrayList<String> initiators = new ArrayList<>();
            initiators.add(initiator.getHostAddress());
            for (Neighbor n : borderList) {
                initiators.add(n.getIpAddr().getHostAddress());
            }
            Collections.sort(initiators);
            inits = String.join("-", initiators);
        }
        else{
            inits = initiator.getHostAddress();
        }

        if (!incrementalSnapshots.keySet().contains(inits)) {
            incrementalSnapshots.put(initiator, new HashMap<>());
        }

        incrementalSnapshots.get(initiator).put(senderAddress, snapshot);

        for (InetAddress init : incrementalSnapshots.keySet()) {

            // generate set of ipAddresses from the list of neighbors of input channel manager

            Set<InetAddress> channels = inputChannelManager.getChannels().stream()
                    .map(Neighbor::getIpAddr)
                    .collect(Collectors.toSet());

//            System.out.println(incrementalSnapshots);

            System.out.println(channels);
            System.out.println(incrementalSnapshots.get(init).keySet());

            if (incrementalSnapshots.get(init).keySet().equals(channels)) {
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

    public void setTransmissionProtocol(String type) throws IOException, InterruptedException {

        inputChannelManager.setChannels(multiReceiver.getSenders());
        inputChannelManager.setTester(multiReceiver.getTester());

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

}
