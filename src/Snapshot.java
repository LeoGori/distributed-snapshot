import java.util.HashMap;

public class Snapshot {

    private HashMap<Neighbor, Integer> channelState;

    public Snapshot() {
        this.channelState = new HashMap<>();
    }

    public void addChannelState(Neighbor neighbor, int state) {
        channelState.put(neighbor, state);
    }

    public HashMap<Neighbor, Integer> getChannelState() {
        return channelState;
    }
}
