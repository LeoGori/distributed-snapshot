import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Packet {

    private String msg;

    private InetAddress IpAddr;

    private int port;

    public Packet(DatagramPacket dp) {
        msg = new String(dp.getData(), 0, dp.getLength());
        IpAddr = dp.getAddress();
        port = dp.getPort();
    }

    public Packet(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        msg = in.readLine();
        IpAddr = socket.getInetAddress();
        port = socket.getPort();
    }

    Packet(String msg, InetAddress IpAddr, int port) {
        this.msg = msg;
        this.IpAddr = IpAddr;
        this.port = port;
    }

    public String getMsg() {
        return msg;
    }

    public InetAddress getIpAddr() {
        return IpAddr;
    }

    public int getPort() {
        return port;
    }


}
