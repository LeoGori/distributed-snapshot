import java.net.InetAddress;
import java.net.UnknownHostException;

public class Token {

    private int timeStamp;

    private InetAddress srcIpAddr;

    private InetAddress initiator;

    public static Boolean isToken(String msg) {
        return msg.contains("--") || msg.contains("||");
    }

    public Token(Packet packet) throws UnknownHostException {
        String msg = packet.getMsg();
//          if msg is not null, print message
//            dp.getAddress().getHostAddress(), dp.getPort()

        String[] parts = msg.split("--");

        initiator = InetAddress.getByName(parts[0]);
        timeStamp = Integer.parseInt(parts[1]);
//        isEndToken = parts[2].equals("end");

        srcIpAddr = packet.getIpAddr();
    }

    public String getSerialized() {
        return initiator.getHostAddress() + "--" + timeStamp;
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
