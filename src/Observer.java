import java.net.UnknownHostException;

public interface Observer {

    //method to update the observer, used by subject
    public void update(Subject subject) throws UnknownHostException;

//    //attach with subject to observe
//    public void setSubject(Subject sub);
}
