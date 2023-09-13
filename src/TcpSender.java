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

    public synchronized void sendMessage() throws UnknownHostException {
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

                System.out.println("Connected to " + ipAddr + ":" + port);

                String msg = p.getMsg();

                if (!Token.isToken(msg)) {
                    lastValue = Integer.parseInt(msg);
                    notifyObserver();
                }

                try {
                    out = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("sending " + msg + " to " + ipAddr + ":" + port + " through TCP");

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

    public void garbageCollect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
