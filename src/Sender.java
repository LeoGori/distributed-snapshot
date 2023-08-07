import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;


public class Sender extends Thread implements Observer {

    private Subject receiver;

    private final DatagramSocket socket;

    private Queue<DatagramPacket> messages;

    private InetAddress srcIpAddr;

    InetAddress multicastGroup;

    private Boolean stop;

    private Boolean multicast;

    public Sender() throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        messages = new LinkedList<>();
        stop = false;
        multicastGroup = InetAddress.getByName("239.0.0.0");
        multicast = false;
    }

    public void run() {

        while (!stop) {

            if (multicast) {
                try {
                    multicastOwnIP();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                multicast = false;
            }
            else {
                sendMessage();
            }
        }

    }

    public void addMessage(Neighbor dest, String msg) {

        InetAddress dest_ip = dest.getIpAddr();
        int recv_port = dest.getPort();
//            System.out.println("messages : " + messages);
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), dest_ip, recv_port);

        messages.add(dp);
    }

    public void multicastOwnIP() throws IOException {

        byte[] buf;

        MulticastSocket multisocket = new MulticastSocket(4446);
        multisocket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        multisocket.setInterface(srcIpAddr);
        multisocket.joinGroup(multicastGroup);

        String multicastMessage = "Hello";
        buf = multicastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, multicastGroup, 4446);
        socket.send(packet);
//        socket.close();

        System.out.println("Multicast message sent to group: " + packet.getAddress());
    }

    public void sendMessage() {
        DatagramPacket dp = messages.poll();

        String msg = new String(dp.getData(), 0, dp.getLength());
        InetAddress dest_ip = dp.getAddress();

        if (dp != null) {
            try {
                System.out.println("sending " + msg + " to " + dest_ip.toString());
                socket.send(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

//    public void sendMessage_2(String destinationIP, int value) throws UnknownHostException {
//
//        InetAddress dest_ip = InetAddress.getByName(destinationIP);
//        int recv_port = 12000;
////            System.out.println("messages : " + messages);
////        try {
////            Thread.sleep(1);
////        } catch (InterruptedException e) {
////            throw new RuntimeException(e);
////        }
//
//        String msg = Integer.toString(value);
//
//        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), dest_ip, recv_port);
//        try {
//            System.out.println("sending " + msg + " to " + dest_ip.toString());
//            socket.send(dp);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public synchronized void update() {

        Token token = ((ReceiverThread) receiver).getToken();
        InetAddress initiator = ((ReceiverThread) receiver).getFirstInitiator();

        if (initiator == null) {

            token.setTimeStamp(token.getTimeStamp() + 1);
            String shareToken = token.getSerialized();

            for (Neighbor neighbor : ((ReceiverThread) receiver).getOutputChannelManager().getFreeChannels()) {

                addMessage(neighbor, shareToken);
            }
        }
        else {
            if (((ReceiverThread) receiver).getInputChannelManager().getBlockedChannels().isEmpty()) {

                Neighbor firstTokenSender = ((ReceiverThread) receiver).getInputChannelManager().getFirstTokenSender();

                token.setTimeStamp(token.getTimeStamp() + 1);
                String endToken = token.getSerialized();

                addMessage(firstTokenSender, endToken);
            }
            else if (token.getInitiator() != ((ReceiverThread) receiver).getInputChannelManager().getFirstInitiator()) {

                token.setTimeStamp(token.getTimeStamp() + 1);
                String endToken = token.getSerialized();

                Neighbor sender = ((ReceiverThread) receiver).getInputChannelManager().getNeighbor(token.getSrcIpAddr());

                addMessage(sender, endToken);
            }
        }
    }

    @Override
    public void setSubject(Subject sub) {

        this.receiver = sub;

    }

}
