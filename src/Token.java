import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Token {

    private int timeStamp;

    private InetAddress srcIpAddr;

    private InetAddress initiator;

    private Boolean isToken;

    public static Boolean isToken(String msg) {
        return msg.contains("-");
    }

    public Token(DatagramPacket packet) throws UnknownHostException {
        String msg = new String(packet.getData(), 0, packet.getLength());
//          if msg is not null, print message
//            dp.getAddress().getHostAddress(), dp.getPort()

        String[] parts = msg.split("-");

        initiator = InetAddress.getByName(parts[0]);
        timeStamp = Integer.parseInt(parts[1]);
//        isEndToken = parts[2].equals("end");

        srcIpAddr = packet.getAddress();
    }

    public String getSerialized() {
        return "token-" + initiator.getHostAddress() + "-" + timeStamp + "-";
    }

    public InetAddress getInitiator() {
        return initiator;
    }

    public InetAddress getSrcIpAddr() {
        return srcIpAddr;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }



}
