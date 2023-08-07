import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Snapshot {

    private HashMap<InetAddress, ArrayList<Integer>> channelState;

    public Snapshot() {
        this(0);
    }

    public Snapshot(int state) {
        channelState = new HashMap<>();
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

    public HashMap<InetAddress, ArrayList<Integer>> getChannelState() {
        return channelState;
    }
}
