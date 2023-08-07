import java.net.InetAddress;
import java.util.HashSet;

public class ChannelManager {

    private HashSet<Neighbor> blockedChannels;

    private HashSet<Neighbor> channels;

    private HashSet<Neighbor> borderList;

    private InetAddress firstInitiator;

    private Neighbor firstTokenSender;

    public ChannelManager() {
        this(new HashSet<>());
    }

    public ChannelManager(HashSet channels) {
        blockedChannels = new HashSet<>();
        this.channels = channels;
        borderList = new HashSet<>();
        firstInitiator = null;
    }

    public InetAddress getFirstInitiator() {
        return firstInitiator;
    }

    public void setFirstInitiator(InetAddress initiator) {
        setFirstInitiator(initiator);
    }

    public void setFirstTokenSender(InetAddress tokenSender) {
        for (Neighbor n : channels)
            if (n.getIpAddr() == tokenSender)
                firstTokenSender = n;
    }

    public Neighbor getFirstTokenSender() {
        return firstTokenSender;
    }

    public synchronized void blockChannel(Neighbor neighbor) {
        blockedChannels.add(neighbor);
        channels.remove(neighbor);
    }

    public synchronized void freeChannel(Neighbor neighbor) {
        blockedChannels.remove(neighbor);
        channels.add(neighbor);
    }

    public synchronized void addBorder(Neighbor neighbor) {
        borderList.add(neighbor);
        channels.remove(neighbor);
    }

    public synchronized void freeChannel(InetAddress ipAddr) {

        Neighbor neighbor = null;
        for (Neighbor n : channels)
            if (n.getIpAddr() == ipAddr)
                neighbor = n;

        if (neighbor != null) {
            blockedChannels.remove(neighbor);
            channels.add(neighbor);
        }
    }

    public synchronized void addBorder(InetAddress ipAddr) {

        Neighbor neighbor = null;
        for (Neighbor n : channels)
            if (n.getIpAddr() == ipAddr)
                neighbor = n;

        if (neighbor != null) {
            borderList.add(neighbor);
            channels.remove(neighbor);
        }

    }

    public HashSet<Neighbor> getBorderList() {
        return borderList;
    }

    public HashSet<Neighbor> getChannels() {
        return channels;
    }

    public HashSet<Neighbor> getBlockedChannels() {
        return blockedChannels;
    }

    public HashSet<Neighbor> getFreeChannels() {
        HashSet<Neighbor> freeChannels = new HashSet<>(channels);
        freeChannels.removeAll(blockedChannels);
        return freeChannels;
    }

    public synchronized void blockAllChannels() {
        blockedChannels.addAll(channels);
        channels.clear();
    }


    public Neighbor getNeighbor(InetAddress ipAddr) {
        for (Neighbor n: channels)
            if (n.getIpAddr() == ipAddr)
                return n;
        return null;
    }


}