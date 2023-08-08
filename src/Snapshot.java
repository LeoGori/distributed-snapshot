import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Snapshot {

    private HashMap<InetAddress, ArrayList<Integer>> channelState;

    private int state;

    public Snapshot() {
        this(0);
    }

    public Snapshot(int state) {

        channelState = new HashMap<>();
        this.state = state;
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

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("state: " + String.valueOf(state) + "\n");
        string.append("channel state: \n");
        for (InetAddress addr : channelState.keySet()) {
            string.append("Node ").append(addr).append(":").append(channelState.get(addr)).append("\n");
        }
        return string.toString();
    }

    public HashMap<InetAddress, ArrayList<Integer>> getChannelState() {
        return channelState;
    }
}
