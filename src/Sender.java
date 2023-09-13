import java.io.IOException;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;



public abstract class Sender extends Thread implements Subject {

    protected Subject receiver;

    protected Queue<Packet> messages;

    protected InetAddress srcIpAddr;

    protected Boolean stop;

    protected Observer observer;

    protected int timeStamp;

    protected int lastValue;

    public Sender() throws SocketException, UnknownHostException {
        this(null);
    }

    public Sender(InetAddress ipAddr) throws SocketException, UnknownHostException {

        messages = new LinkedList<>();
        stop = false;
        this.srcIpAddr = ipAddr;
        timeStamp = 0;
        observer = null;
    }

    public void run() {

//        int counter = 0;

        while (!stop) {

//            counter ++;
//            if (counter == 1000000000) {
//                System.out.println(messages);
//                counter = 0;
//            }

            try {
                this.sendMessage();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        }

        this.garbageCollect();

    }

    public synchronized void addMessage(Neighbor dest, String msg) {

        InetAddress dest_ip = dest.getIpAddr();
        int recv_port = dest.getPort();
//            System.out.println("messages : " + messages);
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        msg += "-" + String.valueOf(timeStamp);
//        timeStamp ++;

//        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), dest_ip, recv_port);

        Packet packet = new Packet(msg, dest_ip, recv_port);

        System.out.println("Message added to the queue");
        messages.add(packet);

        System.out.println(messages);
        System.out.println(messages.isEmpty());
    }

    public abstract void garbageCollect();


    public abstract void sendMessage() throws UnknownHostException;

    public void incrementTimeStamp() {
        timeStamp ++;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int ts) {
        timeStamp = ts;
    }

    public void setStop(Boolean stop) {
        this.stop = stop;
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
            observer.update(this);
        }
    }

    public int getLastValue() {
        return lastValue;
    }

}
