import java.net.UnknownHostException;

public interface Subject {

    //methods to register and unregister observers
    public void register(Observer obj);
    public void unregister(Observer obj);

    //method to notify observers of change
    public void notifyObserver() throws UnknownHostException;

//    //method to get updates from subject
//    public Object getUpdate();

}