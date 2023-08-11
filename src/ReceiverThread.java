import java.io.IOException;
import java.net.*;
import java.util.*;

public class ReceiverThread extends Thread implements Subject {

    private Observer observer;

    private final int port;
//    private final Queue<Integer> messages;

    private final boolean stop;

    private DatagramPacket datagramPacket;

    public ReceiverThread() {
        this(12000);
    }

    public ReceiverThread(int port) {

        this.port = port;
        this.stop = false;
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
//            String msg = new String(dp.getData(), 0, dp.getLength());
//          if msg is not null, print message
//            dp.getAddress().getHostAddress(), dp.getPort()

            notifyObserver();

//            System.out.println("Received: " + msg + " from " + dp.getAddress());
            // print status

//            System.out.println("Status: " + state);

        }

        socket.close();

    }

    @Override
    public void register(Observer o) {
        if (observer == null) {
            observer = o;
        }
    }

    @Override
    public void unregister(Observer o) {
        if (observer != null) {
            observer = null;
        }
    }

    @Override
    public void notifyObserver() {
        if (observer != null) {
            observer.update();
        }
    }

    public DatagramPacket getDatagramPacket() {
        return datagramPacket;
    }

    //    public Queue<Integer> getStatus() {
//        return messages;
//    }

}


