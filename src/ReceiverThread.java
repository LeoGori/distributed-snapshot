import java.io.IOException;
import java.net.*;
import java.io.*;
import java.util.*;

public class ReceiverThread extends Thread implements Subject {

    protected Observer observer;

    protected Packet packet;

    protected int port;
//    private final Queue<Integer> messages;

    protected boolean stop;

//    private DatagramPacket datagramPacket;

    public ReceiverThread() {
        this(12000);
    }

    public ReceiverThread(int port) {

        this.port = port;
        this.stop = false;
        byte[] buf = new byte[256];

    }

    public int getPort() {
        return port;
    }

    public Packet getPacket() {
        return packet;
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
    public void notifyObserver() throws UnknownHostException {
        if (observer != null) {
            observer.update();
        }
    }


    //    public Queue<Integer> getStatus() {
//        return messages;
//    }

}


