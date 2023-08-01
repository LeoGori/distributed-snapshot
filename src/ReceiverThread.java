import java.io.IOException;
import java.net.*;
import java.util.*;
import java.net.MulticastSocket;


public class ReceiverThread extends Thread {

    protected static final String TOKEN = "-1";

    protected int status;

    protected final int port;
    protected final Queue<Integer> messages;

    protected final boolean stop;

    MultiCastReceiver multiReceiver = new MultiCastReceiver();


    public ReceiverThread() {
        this(12000);
    }

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

        byte[] buf = new byte[256];

        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        while (!stop) {

            DatagramPacket dp = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String msg = new String(dp.getData(), 0, dp.getLength());
//          if msg is not null, print message
//            dp.getAddress().getHostAddress(), dp.getPort()

            if (msg.equals(TOKEN)) {
                System.out.println("stop algo");
            }
            else {
                status = status + Integer.parseInt(msg);
            }

            System.out.println("Received: " + msg + " from " + dp.getAddress().getHostAddress());
            // print status
            System.out.println("Status: " + status);

        }
        socket.close();
    }

//    public Queue<Integer> getStatus() {
//        return messages;
//    }

}
