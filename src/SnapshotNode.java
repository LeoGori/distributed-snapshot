import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;


public class SnapshotNode extends Node implements Observer{

    private Snapshot snapshot;

    private int state;

    private boolean snapshotInProgress;

    private boolean automaticModeOn;

    public SnapshotNode() throws IOException {
        super();
        snapshot = null;
        snapshotInProgress = false;
        automaticModeOn = false;
    }

    @Override
    public synchronized void update(Subject subject) {
//        DatagramPacket dp = receiverThread.getDatagramPacket();

        if (subject != null) {

//            System.out.println(subject.getClass() + " notified the observer");
            if (subject instanceof ReceiverThread) {

                Packet packet = receiverThread.getPacket();

                String msg = packet.getMsg();
                Token token;

                System.out.println("Received: " + msg + " from " + packet.getIpAddr());

                if (Token.isToken(msg)) {

                    try {
                        token = new Token(packet);
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }

                    // if it is the first token to be sent, initialize the initiator,
                    // the sender of the first token
                    // and propagate the snapshot to all channels except the first token sender
                    if (channelManager.getFirstInitiator() == null) {

                        snapshotInProgress = !isSnapshotInProgress();

                        snapshot = new Snapshot(state);

                        channelManager.setFirstInitiator(token.getInitiator());
                        snapshot.setInitiator(token.getInitiator());

                        channelManager.setFirstTokenSender(token.getSrcIpAddr());

                        System.out.println("First initiator: " + channelManager.getFirstInitiator().getHostAddress());
                        System.out.println("First token sender: " + channelManager.getFirstTokenSender().getIpAddr().getHostAddress());


                        if (sender.getTimeStamp() < token.getTimeStamp())
                            sender.setTimeStamp(token.getTimeStamp());

                        sender.incrementTimeStamp();

                        ArrayList<String> shareTokens = new ArrayList<>();
                        ArrayList<Neighbor> neighbors = new ArrayList<>();

                        for (Neighbor neighbor : channelManager.getFreeChannels()) {
                            token.setTimeStamp(sender.getTimeStamp());
                            sender.incrementTimeStamp();
                            shareTokens.add(token.getSerialized());
                            neighbors.add(neighbor);
                        }
                        sender.addMessage(neighbors, shareTokens);

//                        System.out.println("Blocking all channels");
                        channelManager.blockAllChannels();


                    } else { // if it is not the first token to be sent

                        // if the token is the same of the first initiator, free the channel
                        if (channelManager.getFirstInitiator().equals(token.getInitiator())) {
                            channelManager.freeChannel(token.getSrcIpAddr());

                            // if the token of a concurrent snapshot is received
                        } else {
                            channelManager.addBorder(token.getSrcIpAddr());
                            channelManager.freeChannel(token.getSrcIpAddr());

                            System.out.println("Received concurrent token " + token.getInitiator().getHostAddress() +
                                    " while first initiator is " + channelManager.getFirstInitiator().getHostAddress());

//                    if (sender.getTimeStamp() < token.getTimeStamp())
//                        sender.setTimeStamp(token.getTimeStamp() + 1);

                            token.setTimeStamp(token.getTimeStamp() + 1);

                            ArrayList<String> endToken = new ArrayList<>();
                            endToken.add(token.getSerialized());

                            ArrayList<Neighbor> tokenSender = new ArrayList<>();
                            tokenSender.add(channelManager.getNeighbor(token.getSrcIpAddr()));

                            sender.addMessage(tokenSender, endToken);

//                    sender.incrementTimeStamp();
                        }

                    }

                    System.out.println("Blocked channels: " + channelManager.getBlockedChannels());

                    // if it is the last token, end the snapshot
                    if (channelManager.getBlockedChannels().isEmpty()) {
//                if (sender.getTimeStamp() < token.getTimeStamp())
//                    sender.setTimeStamp(token.getTimeStamp() + 1);

                        token.setTimeStamp(sender.getTimeStamp());
                        sender.incrementTimeStamp();

                        ArrayList<String> endToken = new ArrayList<>();
                        endToken.add(token.getSerialized());

                        ArrayList<Neighbor> tokenSender = new ArrayList<>();
                        tokenSender.add(channelManager.getFirstTokenSender());

                        if (channelManager.getFirstInitiator() != ipAddr)
                            sender.addMessage(tokenSender, endToken);

                        System.out.println("Ending snapshot");

                        snapshot.setBorderList(channelManager.getBorderList());

                        ArrayList<String> serializedSnapshot = new ArrayList<>();
                        serializedSnapshot.add(snapshot.getSerialized());

                        ArrayList<Neighbor> tester = new ArrayList<>();
                        tester.add(channelManager.getTester());

                        sender.addMessage(tester, serializedSnapshot);

                        snapshotInProgress = !isSnapshotInProgress();

                        if (isAutomaticModeOn())
                            automaticModeOn = !isAutomaticModeOn();

                        channelManager.resetChannels();

                        updateState(snapshot.getChannelsTotalState());

                    }
                } else {
                    if (msg.contains("automatic_mode")) {
//                        System.out.println("Automatic mode on");
                        automaticModeOn = !isAutomaticModeOn();
                    } else {

                        Set<InetAddress> channels = channelManager.getBlockedChannels().stream()
                                .map(Neighbor::getIpAddr)
                                .collect(Collectors.toSet());

                        System.out.println("blockedChannels: " + channels);
                        System.out.println("Packet ip: " + packet.getIpAddr());

                        if (channels.contains(packet.getIpAddr())) {
                            snapshot.addChannelState(packet.getIpAddr(), Integer.parseInt(msg));
                        } else {
                            updateState(Integer.parseInt(msg));
                        }
                    }
                }
            } else {
                int value = ((Sender) subject).getLastValue();
                System.out.println("updating state and adding message to the queue");
                updateState(-value);
            }
            System.out.println("State: " + state);
        }
        else {
            if (channelManager.getFirstInitiator() == null) {

                System.out.println("Initiating snapshot");

                snapshot = new Snapshot(state);
                snapshot.setInitiator(ipAddr);
                channelManager.setFirstInitiator(ipAddr);
                channelManager.setFirstTokenSender(ipAddr);
                channelManager.blockAllChannels();

                ArrayList<String> startTokens = new ArrayList<>();

                ArrayList<Neighbor> neighbors = new ArrayList<>();

                for (Neighbor n : channelManager.getChannels()) {
                    String token = ipAddr.getHostAddress() + "--" + sender.getTimeStamp();
                    sender.setTimeStamp(sender.getTimeStamp() + 1);

                    startTokens.add(token);
                    neighbors.add(n);
                }
                sender.addMessage(neighbors, startTokens);

                snapshotInProgress = !isSnapshotInProgress();
            }
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTransmissionProtocol(String type) throws IOException, InterruptedException {

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

    public synchronized void initSnapshot() {

        update(null);
    }

    public void sendMessage(Neighbor n, String message) {

        ArrayList<String> msg = new ArrayList<>();
        ArrayList<Neighbor> neighbor = new ArrayList<>();

        msg.add(message);
        neighbor.add(n);

        sender.addMessage(neighbor, msg);
    }

    private void updateState(int value) {
        state += value;
    }

    public synchronized boolean isSnapshotInProgress() {
        return snapshotInProgress;
    }

    // synchronized for allowing main and snaphshot node to acess it
    public synchronized boolean isAutomaticModeOn() {
        return automaticModeOn;
    }

    public boolean isTesterBound() {
        return multiReceiver.isTesterBound();
    }



//    public Vector<int> getStatus() {
//        int status = receiverThread.getStatus();
//        System.out.println("Status: " + status);
//    }
}
