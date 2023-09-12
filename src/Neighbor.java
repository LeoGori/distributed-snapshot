import java.net.InetAddress;
import java.net.UnknownHostException;

public class Neighbor {

    protected InetAddress ipAddr;

    protected int port;

    public int getPort() {
        return port;
    }

    public Neighbor() throws UnknownHostException {
        this.port = 12000;
    }

    public Neighbor(String ipAddr) throws UnknownHostException {
        this.ipAddr = InetAddress.getByName(ipAddr);
        this.port = 12000;
    }

    @Override
    public boolean equals(Object o) {

        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Neighbor)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members
        Neighbor c = (Neighbor) o;

        // Compare the data members and return accordingly
        return ipAddr.equals(c.ipAddr) && port == c.port;
    }

    @Override
    public int hashCode()
    {

        // We are returning the Geek_id
        // as a hashcode value.
        // we can also return some
        // other calculated value or may
        // be memory address of the
        // Object on which it is invoked.
        // it depends on how you implement
        // hashCode() method.
        String ip = ipAddr.getHostAddress();
        System.out.println(ip);
        String[] ipParts = ip.split("\\.");
        int ipPart1 = Integer.parseInt(ipParts[0]);
        int ipPart2 = Integer.parseInt(ipParts[1]);
        int ipPart3 = Integer.parseInt(ipParts[2]);
        int ipPart4 = Integer.parseInt(ipParts[3]);

        return ipPart1 + ipPart2 + ipPart3 + ipPart4 + this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public InetAddress getIpAddr() {
        return ipAddr;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("Node " + " at " + ipAddr + ":" + port + "\n");
        return string.toString();
    }

    public void setIpAddr(InetAddress ipAddr) {
        this.ipAddr = ipAddr;
    }

}
