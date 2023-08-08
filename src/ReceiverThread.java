import java.io.IOException;
import java.net.*;
import java.util.*;

public class ReceiverThread extends Thread implements Subject {

    private Observer observer;

    private final int port;
    private final Queue<Integer> messages;

    private final boolean stop;

    private Snapshot snapshot;

    private ChannelManager inputChannelManager;

    private int state;

    private Token token;

    public ReceiverThread() {
        this(12000);
    }

    public ReceiverThread(int port) {

        this.port = port;
        this.messages = new LinkedList<>();
        this.stop = false;
        this.state = 0;
        this.snapshot = null;
        inputChannelManager = new ChannelManager();
    }

    public void setChannels (HashSet<Neighbor> channels) {
        inputChannelManager.setChannels(channels);
    }

    public void initSnapshot(InetAddress ipAddr) {
        snapshot = new Snapshot(state);
        inputChannelManager.setFirstInitiator(ipAddr);
        inputChannelManager.blockAllChannels();
    }

    public ChannelManager getInputChannelManager() {
        return inputChannelManager;
    }

    public int popMessage() {
        return messages.poll().intValue();
    }

    public int getPort() {
        return port;
    }


    public void run() {

        byte[] buf = new byte[256];

        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        while (!stop) {

            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String msg = new String(dp.getData(), 0, dp.getLength());
//          if msg is not null, print message
//            dp.getAddress().getHostAddress(), dp.getPort()


            if (Token.isToken(msg)) {
                try {
                    token = new Token(dp);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }

                if (inputChannelManager.getFirstInitiator() == null) {
                    System.out.println("Blocking all channels");

                    snapshot = new Snapshot(state);

                    inputChannelManager.setFirstInitiator(token.getInitiator());
                    inputChannelManager.setFirstTokenSender(token.getSrcIpAddr());

                    System.out.println("First initiator: " + inputChannelManager.getFirstInitiator().getHostAddress());
                    System.out.println("First token sender: " + inputChannelManager.getFirstTokenSender().getIpAddr().getHostAddress());

                    inputChannelManager.blockAllChannels();
                }
                else {
                    if (inputChannelManager.getFirstInitiator().equals(token.getInitiator())) {
                        inputChannelManager.freeChannel(token.getSrcIpAddr());
                    }
                    else {
                        inputChannelManager.addBorder(token.getSrcIpAddr());
                    }
                }

                token.setTimeStamp(token.getTimeStamp() + 1);

                notifyObserver();
            }
            else {
                if (inputChannelManager.getBlockedChannels().contains(dp.getAddress())) {
                    snapshot.addChannelState(dp.getAddress(), Integer.parseInt(msg));
                }
                else {
                    state += Integer.parseInt(msg);
                }
            }

            System.out.println("Received: " + msg + " from " + dp.getAddress());
            // print status

            System.out.println("Status: " + state);

        }

        socket.close();

    }

    @Override
    public void register(Observer o) {
        if (observer == null) {
            observer = o;
        }
    }

    @Override
    public void unregister(Observer o) {
        if (observer != null) {
            observer = null;
        }
    }

    @Override
    public void notifyObserver() {
        if (observer != null) {
            observer.update();
        }
    }

    @Override
    public Object getUpdate() {
        return state;
    }

    public Token getToken() {
        return token;
    }

    public String getSnapshot() {
        return snapshot.toString();
    }

    //    public Queue<Integer> getStatus() {
//        return messages;
//    }

}


