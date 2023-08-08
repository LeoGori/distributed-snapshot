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

    private int timeStamp;

    public Sender(InetAddress ipAddr) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket();
        messages = new LinkedList<>();
        stop = false;
        multicastGroup = InetAddress.getByName("239.0.0.0");
//        multicast = false;
        this.srcIpAddr = ipAddr;
        timeStamp = 0;
    }

    public void run() {

        int counter = 0;

        while (!stop) {

//            counter ++;
//            if (counter == 1000000000) {
//                System.out.println(messages);
//                counter = 0;
//            }


            this.sendMessage();

        }
    }

    public synchronized void addMessage(Neighbor dest, String msg) {

        InetAddress dest_ip = dest.getIpAddr();
        int recv_port = dest.getPort();
//            System.out.println("messages : " + messages);
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        msg += "-" + String.valueOf(timeStamp);
//        timeStamp ++;

        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), dest_ip, recv_port);

        System.out.println("Message added to the queue");
        messages.add(dp);

        System.out.println(messages);
        System.out.println(messages.isEmpty());
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
        multisocket.send(packet);
//        socket.close();

        System.out.println("Multicast message sent to group: " + packet.getAddress());
    }

    public synchronized void sendMessage() {
        if (!messages.isEmpty()) {
            System.out.println("Sending packet");
            DatagramPacket dp = messages.poll();

            if (dp != null) {
                String msg = new String(dp.getData(), 0, dp.getLength());
                InetAddress dest_ip = dp.getAddress();
                String port = String.valueOf(dp.getPort());
                System.out.println("sending " + msg + " to " + dest_ip.toString() + ":" + port);
                try {
                    socket.send(dp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
        InetAddress initiatorIP = ((ReceiverThread) receiver).getInputChannelManager().getFirstInitiator();

        if (initiatorIP == null) {

            if (timeStamp < token.getTimeStamp())
                timeStamp = token.getTimeStamp() + 1;

            for (Neighbor neighbor : ((ReceiverThread) receiver).getInputChannelManager().getFreeChannels()) {
                token.setTimeStamp(timeStamp);
                timeStamp ++;
                String shareToken = token.getSerialized();
                addMessage(neighbor, shareToken);
            }
        }
        else {
            Neighbor firstTokenSender = ((ReceiverThread) receiver).getInputChannelManager().getFirstTokenSender();

            if (token.getInitiator() != ((ReceiverThread) receiver).getInputChannelManager().getFirstInitiator()) {
                if (timeStamp < token.getTimeStamp())
                    timeStamp = token.getTimeStamp() + 1;

                token.setTimeStamp(timeStamp);
                timeStamp ++;
                String endToken = token.getSerialized();

                Neighbor sender = ((ReceiverThread) receiver).getInputChannelManager().getNeighbor(token.getSrcIpAddr());
                addMessage(sender, endToken);

                Neighbor initiator = ((ReceiverThread) receiver).getInputChannelManager().getNeighbor(initiatorIP);

                addMessage(initiator, ((ReceiverThread) receiver).getSnapshot());
            }
            else {
                if (((ReceiverThread) receiver).getInputChannelManager().getBlockedChannels().isEmpty()) {
                    if (timeStamp < token.getTimeStamp())
                        timeStamp = token.getTimeStamp() + 1;

                    token.setTimeStamp(timeStamp);
                    timeStamp++;
                    String endToken = token.getSerialized();

                    addMessage(firstTokenSender, endToken);
                }
            }
        }
    }

    @Override
    public void setSubject(Subject sub) {

        this.receiver = sub;

    }

}
