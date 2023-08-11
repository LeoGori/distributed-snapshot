import java.net.*;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Set;

public class SnapshotNode extends Node implements Observer{

    private Snapshot snapshot;

    public SnapshotNode() throws UnknownHostException, SocketException {
        super();
    }

    @Override
    public synchronized void update() {
        DatagramPacket dp = receiverThread.getDatagramPacket();

        String msg = new String(dp.getData(), 0, dp.getLength());
        Token token;

        System.out.println("Received: " + msg + " from " + dp.getAddress());

        if (Token.isToken(msg)) {
            try {
                token = new Token(dp);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            // if it is the first token to be sent
            if (inputChannelManager.getFirstInitiator() == null) {
                System.out.println("Blocking all channels");

                snapshot = new Snapshot(state);

                inputChannelManager.setFirstInitiator(token.getInitiator());
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

            // if it is the last token, end the snapshot
            if (inputChannelManager.getBlockedChannels().isEmpty()) {
//                if (sender.getTimeStamp() < token.getTimeStamp())
//                    sender.setTimeStamp(token.getTimeStamp() + 1);

                token.setTimeStamp(sender.getTimeStamp());
                sender.incrementTimeStamp();
                String endToken = token.getSerialized();

                sender.addMessage(inputChannelManager.getFirstTokenSender(), endToken);

                snapshot.setBorderList(inputChannelManager.getBorderList());

                sender.addMessage(inputChannelManager.getTester(), snapshot.getSerialized());


//                        Neighbor initiator = receiverThread.getInputChannelManager().getNeighbor(initiatorIP);
//
//                        sender.addMessage(initiator, receiverThread.getSnapshot());

            }
        }
        else {
            if (inputChannelManager.getBlockedChannels().contains(dp.getAddress())) {
                snapshot.addChannelState(dp.getAddress(), Integer.parseInt(msg));
            }
            else {
                state += Integer.parseInt(msg);
            }
        }

        System.out.println("State: " + state);
    }

    public void initSnapshot() {

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
