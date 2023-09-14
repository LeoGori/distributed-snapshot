import java.io.IOException;
import java.net.*;
import java.util.ArrayList;


public class SnapshotNode extends Node implements Observer{

    private Snapshot snapshot;

    private int state;

    private boolean snapshotInProgress;

    boolean automaticModeOn;

    public SnapshotNode() throws IOException {
        super();
        snapshot = null;
        snapshotInProgress = false;
        automaticModeOn = false;
    }

    @Override
    public synchronized void update(Subject subject) {
//        DatagramPacket dp = receiverThread.getDatagramPacket();

        System.out.println(subject.getClass() + " notified the observer");

        if (subject != null) {
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

                    // if it is the first token to be sent
                    if (channelManager.getFirstInitiator() == null) {
                        System.out.println("Blocking all channels");

                        snapshotInProgress = true;

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

                        channelManager.blockAllChannels();
                        // if it is not the first token to be sent
                    } else {
                        // if the token is the same of the first initiator
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

                        snapshotInProgress = false;

                        if (isAutomaticModeOn())
                            automaticModeOn = !isAutomaticModeOn();

                        channelManager.resetChannels();

                    }
                } else {
                    if (msg.contains("automatic_mode")) {
                        System.out.println("Automatic mode on");
                        automaticModeOn = !isAutomaticModeOn();
                    } else {
                        if (channelManager.getBlockedChannels().contains(packet.getIpAddr())) {
                            snapshot.addChannelState(packet.getIpAddr(), Integer.parseInt(msg));
                        } else {
                            updateState(Integer.parseInt(msg));
                        }
                    }
                }
            } else {
                int value = ((Sender) subject).getLastValue();
                System.out.println("updating state after sending message");
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

                snapshotInProgress = true;
            }
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

    private synchronized void updateState(int value) {
        state += value;
    }

    public boolean isSnapshotInProgress() {
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
