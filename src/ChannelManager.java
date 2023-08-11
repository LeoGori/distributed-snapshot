import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

public class ChannelManager {

    private HashSet<Neighbor> blockedChannels;

    private HashSet<Neighbor> channels;

    private HashSet<Neighbor> borderList;

    private InetAddress firstInitiator;

    private Neighbor firstTokenSender;

    private Neighbor tester;

    public ChannelManager() {
        this(new HashSet<>());
    }

    public ChannelManager(HashSet<Neighbor> channels) {
        blockedChannels = new HashSet<>();
        this.channels = channels;
        borderList = new HashSet<>();
        firstInitiator = null;
    }

    public void setChannels(HashSet<Neighbor> channels) {
        this.channels = channels;
    }

    public InetAddress getFirstInitiator() {
        return firstInitiator;
    }

    public void setFirstInitiator(InetAddress initiator) {
        firstInitiator = initiator;
    }

    public void setFirstTokenSender(InetAddress tokenSender) {
        for (Neighbor n : channels) {
            System.out.println(n.getIpAddr().getHostAddress() + " " + tokenSender.getHostAddress());
            if (n.getIpAddr().equals(tokenSender)) {
                System.out.println("Found");
                firstTokenSender = n;
                channels.remove(n);
                blockedChannels.remove(n);
            }
        }
    }

    public Neighbor getFirstTokenSender() {
        return firstTokenSender;
    }

    public synchronized void blockChannel(Neighbor neighbor) {
        blockedChannels.add(neighbor);
    }

    public synchronized void freeChannel(Neighbor neighbor) {
        blockedChannels.remove(neighbor);
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
        }
    }

    public synchronized void addBorder(InetAddress ipAddr) {

        Neighbor neighbor = null;
        for (Neighbor n : channels)
            if (n.getIpAddr() == ipAddr)
                neighbor = n;

        if (neighbor != null) {
            borderList.add(neighbor);
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
    }


    public Neighbor getNeighbor(InetAddress ipAddr) {
        for (Neighbor n: channels)
            if (n.getIpAddr() == ipAddr)
                return n;
        return null;
    }

    public void setTester(Neighbor t) throws UnknownHostException {
        tester = t;
    }

    public Neighbor getTester() {
        return tester;
    }


}
