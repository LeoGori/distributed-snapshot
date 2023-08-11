import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Set;

public class Node extends Neighbor implements Observer {

    private final ReceiverThread receiverThread;

    private final MultiCastReceiver multiReceiver;

    private final Sender sender;

    private ChannelManager inputChannelManager;

    private Snapshot snapshot;

    public Node() throws UnknownHostException, SocketException {
        super();
        try {
            ipAddr = this.getInterfaces();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        sender = new Sender(ipAddr);
        this.receiverThread = new ReceiverThread(port);

        receiverThread.register(this);

        sender.start();
        this.receiverThread.start();
        this.multiReceiver =  new MultiCastReceiver(this.ipAddr);
        multiReceiver.start();

        inputChannelManager = new ChannelManager();
    }

    public InetAddress getInterfaces() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        for (NetworkInterface netint : Collections.list(interfaces)) {
            displayInterfaceInformation(netint);
            if (netint.getName().equals("wlan0")) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (inetAddress instanceof Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        }
        return null;
    }

    static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.println("Display name:" + netint.getDisplayName());
        System.out.println("Name: " + netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.println("InetAddress: " + inetAddress);
        }
        System.out.println("\n");
    }

    public Neighbor getNeighbor(int id) {

        Set<Neighbor> nodes = multiReceiver.getSenders();
        Neighbor[] neighbors = nodes.toArray(new Neighbor[nodes.size()]);
        return neighbors[id];
    }

    public int getPort() {
        return receiverThread.getPort();
    }

    public void sendMessage(Neighbor n, String message) {
        sender.addMessage(n, message);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder(super.toString());
        string.append(" has neighbors: \n");
        int index = 0;
        for (Neighbor n : multiReceiver.getSenders()) {
            string.append("Node ").append(index).append(" at ").append(n.getIpAddr()).append(":").append(port).append("\n");
            index++;
        }
        return string.toString();
    }

    public Sender getSender() {
        return sender;
    }




    public void setChannels() {
        inputChannelManager.setChannels(multiReceiver.getSenders());
    }

    @Override
    public synchronized void update() {

        DatagramPacket dp = receiverThread.getDatagramPacket();
        InetAddress initiatorIP = inputChannelManager.getFirstInitiator();

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

        System.out.println("Status: " + state);
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
