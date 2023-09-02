import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

public class TcpSender extends Sender {

    private Socket socket;
    private PrintWriter out;

    public TcpSender(InetAddress ipAddr) throws IOException {
        super(ipAddr);
        socket = null;
        out = null;
    }

    public TcpSender() throws IOException {
        this(null);
    }

    public synchronized void sendMessage() {
        if (!messages.isEmpty()) {
            System.out.println("Sending packet");
            Packet p = messages.poll();

            if (p != null) {

                InetAddress ipAddr = p.getIpAddr();
                int port = p.getPort();

                try {
                    socket = new Socket(ipAddr, port);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Connected to " + ipAddr.toString() + ":" + port);

                try {
                    out = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                String msg = p.getMsg();

                System.out.println("sending " + msg + " to " + ipAddr.toString() + ":" + port + " through TCP");

                out.println(msg);

                out.close();

                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }

}
