
package chatprogram;
import java.util.*;

public abstract class chatThread {
    protected Main mainReference;
    protected String username;
    protected ArrayList privateChats;
    protected String host;
    protected int port;
    public abstract void sendMessage(String s);
    public String getUsername(){return username;}
    public abstract void executeCommand(String command);
    public abstract void sendPrivateRequest(String fromUser, String toUser);
}
