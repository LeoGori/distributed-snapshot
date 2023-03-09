import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.Queue;

public class Node extends Thread {
    private int status;

    private InetAddress ipAddr;

    private final Vector<Node> neighbors;

    Map<Node, Queue<Integer>> messageTable;

    private int port;

    public Node() throws UnknownHostException {
        this.status = 0;
        this.ipAddr = InetAddress.getLocalHost();
        this.port = 8080;
        this.neighbors = new Vector<>();
    }

    public Node(String ipAddr) throws UnknownHostException {
        this.status = 0;
        this.ipAddr = InetAddress.getByName(ipAddr);
        this.port = 8080;
        this.neighbors = new Vector<>();
    }

    public void addConnection(Node n) {
        neighbors.add(n);
        messageTable.put(n, new LinkedList<>());
    }

    public Node getNeighbor(int id) {
        return neighbors.get(id);
    }

    public void addMessage(Node dest, int value) {
        if (messageTable.containsKey(dest)) {
            messageTable.get(dest).add(value);
        } else {
            Queue <Integer> q = new LinkedList<>();
            q.add(value);
            messageTable.put(dest, q);
        }
    }

    @Override
    public void run(){
        for (Node key : messageTable.keySet()) {
            for (int i = 0; i < messageTable.get(key).size(); i++) {
                try {
                    sendMsg(messageTable.get(key).remove().toString(), key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            recvMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public InetAddress getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(InetAddress ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void sendMsg(String msg, Node neighbor) throws IOException {
        InetAddress recv_ip = neighbor.getIpAddr();
        int recv_port = neighbor.getPort();

        DatagramSocket socket = new DatagramSocket();

        DatagramPacket dp = new DatagramPacket(msg.getBytes(), msg.length(), recv_ip, recv_port);
        socket.send(dp);
        socket.close();

    }

    public void recvMsg() throws IOException {
        DatagramSocket socket = new DatagramSocket(port);
        byte[] buf = new byte[256];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        socket.receive(dp);
        String msg = new String(dp.getData(), 0, dp.getLength());
        System.out.println("Received: " + msg);
        socket.close();
    }

    @Override public String toString() {
        String string = "Node " + " at " + ipAddr + ":" + port + "\n";
        string += " has neighbors: \n";
        int index = 0;
        for (Node n : neighbors) {
            string += "Node " + index + " at " + n.getIpAddr() + ":" + n.getPort() + "\n";
            index++;
        }
        return string;
    }

}
