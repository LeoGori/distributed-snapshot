import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;

public class UdpReceiverThread extends ReceiverThread {

    private Packet packet;

    DatagramSocket socket;

    public UdpReceiverThread() {
        this(12000);
    }

    public UdpReceiverThread(int port) {
        super(port);
        packet = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPort() {
        return port;
    }

    public void run() {

        while (!socket.isClosed()) {
            byte[] buf = new byte[256];
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
//            datagramPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagramPacket);
            } catch (IOException e) {
//                throw new RuntimeException(e);
                break;
            }
//            String msg = new String(dp.getData(), 0, dp.getLength());
//          if msg is not null, print message
//            dp.getAddress().getHostAddress(), dp.getPort()

            packet = new Packet(datagramPacket);

            try {
                notifyObserver();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

//            System.out.println("Received: " + msg + " from " + dp.getAddress());
            // print status

//            System.out.println("Status: " + state);

        }

    }

    public void closeSocket() {
        socket.close();
    }


}

