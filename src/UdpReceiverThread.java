import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;

public class UdpReceiverThread extends ReceiverThread {

    private Packet packet;

    public UdpReceiverThread() {
        this(12000);
    }

    public UdpReceiverThread(int port) {

        this.port = port;
        this.stop = false;
        packet = null;
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
            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
//            datagramPacket = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(datagramPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
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

        socket.close();

    }


}

