import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;

public class TcpReceiverThread extends ReceiverThread {


    private ServerSocket serverSocket;

    public TcpReceiverThread() throws IOException {
        this(12000);
    }

    public TcpReceiverThread(int port) throws IOException {
        super(port);
        serverSocket = new ServerSocket(port);
    }

    public int getPort() {
        return port;
    }

    public void run() {

        while (!stop) {

//            datagramPacket = new DatagramPacket(buf, buf.length);
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Accepted connection from " + clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort());


            try {
                packet = new Packet(clientSocket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Received: " + packet.getMsg() + " from " + packet.getIpAddr());


            try {
                notifyObserver();
                System.out.println("Notified observer");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
