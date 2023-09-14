import java.io.IOException;
import java.net.*;
import java.util.ArrayList;


public class SnapshotNode extends Node implements Observer{

    private Snapshot snapshot;

    private int state;

    private boolean snapshotInProgress;

    public SnapshotNode() throws IOException {
        super();
        snapshot = null;
        snapshotInProgress = false;
    }

    @Override
    public synchronized void update(Subject subject) {
//        DatagramPacket dp = receiverThread.getDatagramPacket();

        System.out.println(subject.getClass() + " notified the observer");

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
                if (inputChannelManager.getFirstInitiator() == null) {
                    System.out.println("Blocking all channels");

                    snapshotInProgress = true;

                    snapshot = new Snapshot(state);

                    inputChannelManager.setFirstInitiator(token.getInitiator());
                    snapshot.setInitiator(token.getInitiator());

                    inputChannelManager.setFirstTokenSender(token.getSrcIpAddr());

                    System.out.println("First initiator: " + inputChannelManager.getFirstInitiator().getHostAddress());
                    System.out.println("First token sender: " + inputChannelManager.getFirstTokenSender().getIpAddr().getHostAddress());


                    if (sender.getTimeStamp() < token.getTimeStamp())
                        sender.setTimeStamp(token.getTimeStamp());

                    sender.incrementTimeStamp();

                    ArrayList<String> shareTokens = new ArrayList<>();
                    ArrayList<Neighbor> neighbors = new ArrayList<>();

                    for (Neighbor neighbor : inputChannelManager.getFreeChannels()) {
                        token.setTimeStamp(sender.getTimeStamp());
                        sender.incrementTimeStamp();
                        shareTokens.add(token.getSerialized());
                        neighbors.add(neighbor);
                    }
                    sender.addMessage(neighbors, shareTokens);

                    inputChannelManager.blockAllChannels();
                    // if it is not the first token to be sent
                } else {
                    // if the token is the same of the first initiator
                    if (inputChannelManager.getFirstInitiator().equals(token.getInitiator())) {
                        inputChannelManager.freeChannel(token.getSrcIpAddr());

                        // if the token of a concurrent snapshot is received
                    } else {
                        inputChannelManager.addBorder(token.getSrcIpAddr());
                        inputChannelManager.freeChannel(token.getSrcIpAddr());

                        System.out.println("Received concurrent token " + token.getInitiator().getHostAddress() +
                                " while first initiator is " + inputChannelManager.getFirstInitiator().getHostAddress());

//                    if (sender.getTimeStamp() < token.getTimeStamp())
//                        sender.setTimeStamp(token.getTimeStamp() + 1);

                        token.setTimeStamp(token.getTimeStamp() + 1);

                        ArrayList<String> endToken = new ArrayList<>();
                        endToken.add(token.getSerialized());

                        ArrayList<Neighbor> tokenSender = new ArrayList<>();
                        tokenSender.add(inputChannelManager.getNeighbor(token.getSrcIpAddr()));

                        sender.addMessage(tokenSender, endToken);

//                    sender.incrementTimeStamp();
                    }

                }

                System.out.println("Blocked channels: " + inputChannelManager.getBlockedChannels());

                // if it is the last token, end the snapshot
                if (inputChannelManager.getBlockedChannels().isEmpty()) {
//                if (sender.getTimeStamp() < token.getTimeStamp())
//                    sender.setTimeStamp(token.getTimeStamp() + 1);

                    token.setTimeStamp(sender.getTimeStamp());
                    sender.incrementTimeStamp();

                    ArrayList<String> endToken = new ArrayList<>();
                    endToken.add(token.getSerialized());

                    ArrayList<Neighbor> tokenSender = new ArrayList<>();
                    tokenSender.add(inputChannelManager.getFirstTokenSender());

                    if (inputChannelManager.getFirstInitiator() != ipAddr)
                        sender.addMessage(tokenSender, endToken);

                    System.out.println("Ending snapshot");

                    snapshot.setBorderList(inputChannelManager.getBorderList());

                    ArrayList<String> serializedSnapshot = new ArrayList<>();
                    serializedSnapshot.add(snapshot.getSerialized());

                    ArrayList<Neighbor> tester = new ArrayList<>();
                    tester.add(inputChannelManager.getTester());

                    sender.addMessage(tester, serializedSnapshot);

                    snapshotInProgress = false;

                    inputChannelManager.resetChannels();

                }
            } else {
                if (inputChannelManager.getBlockedChannels().contains(packet.getIpAddr())) {
                    snapshot.addChannelState(packet.getIpAddr(), Integer.parseInt(msg));
                } else {
                    updateState(Integer.parseInt(msg));
                }
            }
        }
        else {
            int value = ((Sender) subject).getLastValue();
            System.out.println("updating state after sending message");
            updateState(-value);
        }
        System.out.println("State: " + state);
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

        if (inputChannelManager.getFirstInitiator() == null) {

            System.out.println("Initiating snapshot");

            snapshot = new Snapshot(state);
            snapshot.setInitiator(ipAddr);
            inputChannelManager.setFirstInitiator(ipAddr);
            inputChannelManager.blockAllChannels();

            ArrayList<String> startTokens = new ArrayList<>();

            ArrayList<Neighbor> neighbors = new ArrayList<>();

            for (Neighbor n : inputChannelManager.getChannels()) {
                String token = ipAddr.getHostAddress() + "--" + sender.getTimeStamp();
                sender.setTimeStamp(sender.getTimeStamp() + 1);

                startTokens.add(token);
                neighbors.add(n);
            }
            sender.addMessage(neighbors, startTokens);

            snapshotInProgress = true;
        }
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

//    public Vector<int> getStatus() {
//        int status = receiverThread.getStatus();
//        System.out.println("Status: " + status);
//    }
}
