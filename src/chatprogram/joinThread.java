
package chatprogram;

import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.*;

public class joinThread extends  chatThread{
    private Socket socket;
    private DataOutputStream outStream;
    private DataInputStream inStream;


    public joinThread(int port, String host, String username, int maxClients, Main mainReference) throws IOException, usernameTaken
    {
    //attempt to create connection with host
        this.username = username;
        this.mainReference = mainReference;
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        outStream = new DataOutputStream(socket.getOutputStream());
        inStream = new DataInputStream(socket.getInputStream());
        //print out the username
        outStream.writeUTF(username);
        //get response on whether the username has been taken
        String response = inStream.readUTF();
        if(response.equals("taken"))
        {
            throw new usernameTaken();
        }
        privateChats = new ArrayList();
        new Thread(new recieveMessages()).start();
    }

    public void sendMessage(String s){
        try
        {
        outStream.writeUTF(s);
        }
        catch(IOException ioe)
        {
        }
    }
    public void executeCommand(String command)
    {
        //disect command
        char commandChar = command.charAt(9);
        
        DefaultListModel userListModel = mainReference.getUserListModel();
        switch(commandChar)
        {
            //  commands are:
            //  \command-a-username\    = adds a username
            //  \command-r-username\    = removes a username
            //  \command-p-fromuser-fromport-fromhost\  = recieved a private request from another user

            case 'a'://add a username
            int pos = userListModel.getSize();
            userListModel.add(pos, command.substring(11, command.length() - 1));
            break;
            case 'r'://remove a username
            userListModel.remove(userListModel.indexOf(command.substring(11, command.length() - 1)));
            break;
            case 'p':
            //get the from username
            int dashIndex = command.indexOf('-', 11);
            String fromUser = command.substring(11, dashIndex);
            int fromPort = Integer.parseInt(command.substring(dashIndex + 1, command.indexOf('-', dashIndex + 1)));
            dashIndex = command.indexOf('-', dashIndex + 1);
            String fromHost = command.substring(dashIndex + 1, command.indexOf('\\', dashIndex));

                                    try{
                                    privateChats.add(new privateChatArea(false, fromHost, fromPort, username, fromUser, mainReference));
                                    return;
                                    }
                                    catch(IOException ioe){
                                    ioe.printStackTrace();
                                    }                                                                        
            break;

        }

    }
     public void sendPrivateRequest(String fromUser, String toUser)
    {
    
        int privatePort = port + 1;
            while(true)
            {
                privateChatArea tmp = null;
                        try{
                        tmp = new privateChatArea(true, host, privatePort, username, fromUser, mainReference);//attempt to create a new private chat host
                        privateChats.add(tmp);
                        break;
                        }
                        catch(IOException ioe)//port is already being used, so try again
                        {
                         privatePort++;
                         if(tmp != null)
                             tmp.dispose();//remove previous try
                        }


             }
            
            sendMessage("\\command-p-"+getUsername()+"-"+toUser+"-"+privatePort+"\\");
    }

    //wait for messages from host
    private class recieveMessages implements Runnable
    {
        public void run(){
            while(true)
            {
                try
                {
                     String message = inStream.readUTF();                    
                     //check for command

                     if(message.indexOf("\\command") == 0)//it is a command from the host, execute it
                     {
                         executeCommand(message);
                     }
                     else
                     {
                     mainReference.addMessage(message);
                     }
                }
                catch(IOException ioe)
                {               
                //host has disconnected
                mainReference.addMessage("Host has disconnected");
                break;
                }
            }
        }
    }
}
