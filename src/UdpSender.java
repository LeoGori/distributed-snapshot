import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class UdpSender extends Sender {

    private final DatagramSocket socket;

    private InetAddress multicastGroup;


    public UdpSender(InetAddress ipAddr) throws SocketException, UnknownHostException {
        super(ipAddr);
        this.socket = new DatagramSocket();
        multicastGroup = InetAddress.getByName("239.0.0.0");
    }

    public void multicastOwnIP(String msg) throws IOException {

        byte[] buf;

        MulticastSocket multisocket = new MulticastSocket(4446);
        multisocket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false);
        multisocket.setInterface(srcIpAddr);
        multisocket.joinGroup(multicastGroup);

        buf = msg.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, multicastGroup, 4446);
        multisocket.send(packet);
//        socket.close();

        System.out.println("Multicast message sent to group: " + packet.getAddress());
    }

    public synchronized void sendMessage() {
        if (!messages.isEmpty()) {
            System.out.println("Sending packet");
            Packet p = messages.poll();

            if (p != null) {
                String msg = p.getMsg();
                InetAddress dest_ip = p.getIpAddr();
                String port = String.valueOf(p.getPort());
                System.out.println("sending " + msg + " to " + dest_ip.toString() + ":" + port + " through UDP");

                DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), dest_ip, p.getPort());

                try {
                    socket.send(dp);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
