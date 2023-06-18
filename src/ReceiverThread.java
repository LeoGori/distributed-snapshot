import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Vector;

public class ReceiverThread extends Thread {

    private int status;

    private final int port;
    private final Queue<Integer> messages;

    private final boolean stop;

    public ReceiverThread(int port) {
        this.port = port;
        this.messages = new LinkedList<>();
        this.stop = false;
    }

    public int popMessage() {
        return messages.poll().intValue();
    }

    public int getPort() {
        return port;
    }

    public void run() {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (!stop) {

            byte[] buf = new byte[256];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String msg = new String(dp.getData(), 0, dp.getLength());
//          if msg is not null, print message
//            dp.getAddress().getHostAddress(), dp.getPort()
            System.out.println("Received: " + msg + " from " + dp.getAddress().toString());

//            if (msg.equals("token")) {
//            } else {
//                status = status + Integer.parseInt(msg);
//            }
        }
        socket.close();
    }

//    public Queue<Integer> getStatus() {
//        return messages;
//    }

}
