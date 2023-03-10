import java.net.InetAddress;
import java.net.UnknownHostException;

public class Neighbor {
    protected int status;

    protected InetAddress ipAddr;

    protected int port;

    public int getPort() {
        return port;
    }

    public Neighbor() throws UnknownHostException {
        this.status = 0;
        this.ipAddr = InetAddress.getLocalHost();
        this.port = 12000;
    }

    public Neighbor(String ipAddr) throws UnknownHostException {
        this.status = 0;
        this.ipAddr = InetAddress.getByName(ipAddr);
        this.port = 12000;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public InetAddress getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(InetAddress ipAddr) {
        this.ipAddr = ipAddr;
    }

}
