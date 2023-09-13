import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;


public class Snapshot {

    private HashMap<InetAddress, ArrayList<Integer>> channelState;

    private int state;

    private InetAddress initiator;

    private HashSet<Neighbor> borderList;

    public Snapshot(int state) {
        channelState = new HashMap<>();
        this.state = state;
        borderList = new HashSet<>();
    }

    public Snapshot(Packet packet) throws UnknownHostException {
        String msg = packet.getMsg();
        String[] parts = msg.split("\\|\\|");

        channelState = new HashMap<>();
        borderList = new HashSet<>();
        initiator = InetAddress.getByName(parts[0].split(":")[1]);
        state = Integer.parseInt(parts[1].split(":")[1]);

        if (parts.length - 2 > 0) {
            String[] subParts;
            for (int i = 2; i < parts.length; i++) {
                subParts = parts[i].split(":");
                if (!subParts[0].contains("be")) {
                    InetAddress channelIp = InetAddress.getByName(subParts[0]);
                    String[] items = subParts[1].replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                    ArrayList<Integer> values = new ArrayList<Integer>();
                    for (String item : items)
                        values.add(Integer.parseInt(item));
                    channelState.put(channelIp, values);
                }
                else {
                    borderList.add(new Neighbor(subParts[1]));
                }
            }
        }
    }

    public int getState() {
        return state;
    }
    public void addChannelState(InetAddress ipAddr, int state) {

        if (channelState.containsKey(ipAddr)) {
            channelState.get(ipAddr).add(state);
        } else {
            ArrayList<Integer> states = new ArrayList<>();
            states.add(state);
            channelState.put(ipAddr, states);
        }
    }

    public String getSerialized() {


        StringBuilder s = new StringBuilder("init:" + initiator.getHostAddress());
        s.append("||s:" + state);
        if(!channelState.isEmpty()) {
            for (InetAddress addr : channelState.keySet()) {
                s.append("||").append(addr).append(":").append(channelState.get(addr));
            }
        }
        if (!borderList.isEmpty()) {
            for (Neighbor n : borderList) {
                s.append("||be:" + n.getIpAddr().getHostAddress());
            }
        }
        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("initiator: " + initiator.getHostAddress() + "\n");
        string.append(new StringBuilder("state: " + state + "\n"));
        if (!channelState.keySet().isEmpty()) {
            string.append("channel state: \n");
            for (InetAddress addr : channelState.keySet()) {
                string.append("Node ").append(addr).append(":").append(channelState.get(addr)).append("\n");
            }
        }
        if (!borderList.isEmpty()) {
            string.append("Border list: \n");
            for (Neighbor n : borderList) {
                string.append("border element: " + n.getIpAddr().getHostAddress() + "\n");
            }
        }
        return string.toString();
    }

    public void setBorderList(HashSet<Neighbor> bl) {
        borderList = bl;
    }

    public InetAddress getInitiator() {
        return initiator;
    }

    public HashMap<InetAddress, ArrayList<Integer>> getChannelState() {
        return channelState;
    }

    public void setInitiator(InetAddress i) {
        initiator = i;
    }

    public int getBalance() {
        int balance = 0;
        balance += state;
        for (InetAddress addr : channelState.keySet()) {
            for (int i : channelState.get(addr)) {
                balance += i;
            }
        }
        return balance;
    }

    public HashSet<Neighbor> getBorderList() {
        return borderList;
    }
}
