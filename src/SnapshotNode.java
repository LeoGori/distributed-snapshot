import java.io.IOException;
import java.net.*;


public class SnapshotNode extends Node implements Observer{

    private Snapshot snapshot;

    public SnapshotNode() throws IOException {
        super();
        snapshot = null;
    }

    @Override
    public synchronized void update() {
//        DatagramPacket dp = receiverThread.getDatagramPacket();

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

                snapshot = new Snapshot(state);

                inputChannelManager.setFirstInitiator(token.getInitiator());
                snapshot.setInitiator(token.getInitiator());

                inputChannelManager.setFirstTokenSender(token.getSrcIpAddr());

                System.out.println("First initiator: " + inputChannelManager.getFirstInitiator().getHostAddress());
                System.out.println("First token sender: " + inputChannelManager.getFirstTokenSender().getIpAddr().getHostAddress());

                inputChannelManager.blockAllChannels();

                if (sender.getTimeStamp() < token.getTimeStamp())
                    sender.setTimeStamp(token.getTimeStamp());

                sender.incrementTimeStamp();

                for (Neighbor neighbor : inputChannelManager.getFreeChannels()) {
                    token.setTimeStamp(sender.getTimeStamp());
                    sender.incrementTimeStamp();
                    String shareToken = token.getSerialized();
                    sender.addMessage(neighbor, shareToken);
                }
            // if it is not the first token to be sent
            } else {
                // if the token is the same of the first initiator
                if (inputChannelManager.getFirstInitiator().equals(token.getInitiator())) {
                    inputChannelManager.freeChannel(token.getSrcIpAddr());

                // if the token of a concurrent snapshot is received
                } else {
                    inputChannelManager.addBorder(token.getSrcIpAddr());

//                    if (sender.getTimeStamp() < token.getTimeStamp())
//                        sender.setTimeStamp(token.getTimeStamp() + 1);

                    token.setTimeStamp(token.getTimeStamp() + 1);

                    String endToken = token.getSerialized();

                    Neighbor tokenSender = inputChannelManager.getNeighbor(token.getSrcIpAddr());
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
                String endToken = token.getSerialized();

                if (inputChannelManager.getFirstInitiator() != ipAddr)
                    sender.addMessage(inputChannelManager.getFirstTokenSender(), endToken);

                System.out.println("Ending snapshot");

                snapshot.setBorderList(inputChannelManager.getBorderList());

                sender.addMessage(inputChannelManager.getTester(), snapshot.getSerialized());


//                        Neighbor initiator = receiverThread.getInputChannelManager().getNeighbor(initiatorIP);
//
//                        sender.addMessage(initiator, receiverThread.getSnapshot());
            }
        }
        else {
            if (inputChannelManager.getBlockedChannels().contains(packet.getIpAddr())) {
                snapshot.addChannelState(packet.getIpAddr(), Integer.parseInt(msg));
            }
            else {
                state += Integer.parseInt(msg);
            }
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

        sender.start();
        receiverThread.start();
    }

    public void initSnapshot() {

        System.out.println("Initiating snapshot");

        snapshot = new Snapshot(state);
        inputChannelManager.setFirstInitiator(ipAddr);
        inputChannelManager.blockAllChannels();

        for (Neighbor n : inputChannelManager.getChannels()) {
            String token = ipAddr.getHostAddress() + "--" + String.valueOf(sender.getTimeStamp());
            sender.setTimeStamp(sender.getTimeStamp() + 1);
            sender.addMessage(n, token);
        }
    }

//    public Vector<int> getStatus() {
//        int status = receiverThread.getStatus();
//        System.out.println("Status: " + status);
//    }
}
