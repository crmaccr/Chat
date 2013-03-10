

package chatprogram;

import java.net.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;

public class hostThread extends chatThread
{
    private ServerSocket server;
    private ArrayList clients;
    private DefaultListModel userListModel;

    public hostThread(int port, String username, Main mainReference, int maxClients) throws IOException
    {
        this.username = username;
        //add host username to list
        userListModel = mainReference.getUserListModel();


        this.port = port;
        this.server = new ServerSocket(port);
        this.host = this.server.getInetAddress().getHostName();
        clients = new ArrayList();
        privateChats = new ArrayList();
        this.mainReference = mainReference;

        new Thread(new acceptClients()).start();
        int pos = userListModel.getSize();
        userListModel.add(pos, username);
    }
    public void executeCommand(String command)
    {
        //disect command
        char commandChar = command.charAt(9);
        switch(commandChar)
        {
            //  commands are:
            //  \command-a-username\    = adds a username
            //  \command-r-username\    = removes a username
            //  \command-p-fromusername-touser-fromport\    = private request from a user


            case 'a'://add a username
            int pos = userListModel.getSize();
            userListModel.add(pos, command.substring(11, command.length() - 1));
            break;
            case 'r'://remove a username
            userListModel.remove(userListModel.indexOf(command.substring(11, command.length() - 1)));
            break;
            case 'p'://private request
                //get the from username
                int dashIndex = command.indexOf('-', 11);
                String fromUser = command.substring(11, dashIndex);
                String toUser = command.substring(dashIndex + 1, command.indexOf('-', dashIndex + 1));
                dashIndex = command.indexOf('-', dashIndex + 1);
                int privatePort = Integer.parseInt(command.substring(dashIndex + 1, command.length() - 1));


                if(toUser.equals(getUsername()))//someone is requesting a private chat with the host
                {

                            //find the client requesting
                            client fromClient = null;
                           for(int i = 0; i < clients.size(); i++)
                           {
                            if(clients.get(i) != null)
                            {
                            if(((client)clients.get(i)).getClientUsername().equals(fromUser))
                            {
                                fromClient = ((client)clients.get(i));
                            }
                            }
                           }
                            //begin a private chat
                                    try{
                                    privateChats.add(new privateChatArea(false, fromClient.getHostIP(), privatePort, username, fromUser, mainReference));
                                    return;
                                    }
                                    catch(IOException ioe){}

                }

                else//reroute the message to the proper person
                {
                    if(!fromUser.equals(toUser))
                    {
                        client fromClient = null, toClient = null;
                        for(int i = 0; i < clients.size(); i++)
                        {
                            if(((client)clients.get(i)) != null)
                            {
                            if(((client)clients.get(i)).getClientUsername().equals(toUser))
                            {
                             toClient = ((client)clients.get(i));
                            }
                            else if(((client)clients.get(i)).getClientUsername().equals(fromUser))
                            {
                                fromClient = ((client)clients.get(i));
                            }
                            }
                        }
                        try
                        {
                            toClient.sendClientMessage("\\command-p-"+fromUser+"-"+privatePort + "-" + fromClient.getHostIP() +"\\");
                        }
                        catch(IOException ioe)
                        {

                        }
                    }
                }

            break;
        }

    }
    public synchronized void sendMessage(String s)
    {
        //check and add to the host itself
         if(s.indexOf("\\command") == 0)
             {
                 executeCommand(s);
             }
             else
             {
             mainReference.addMessage(s);
             }
        //if message is a command for private message DON'T SEND IT TO ALL OTHER CLIENTS
        if(s.indexOf("\\command-p") != 0)
        {
            //send message to clients
            for(int i = 0; i < clients.size(); i++)
            {
                if(((client)clients.get(i)) != null)
                {
                    try{
                    ((client)clients.get(i)).sendClientMessage(s);
                    }
                    catch(IOException ioe)
                    {

                    }
                }
            }
        }
    }
    public void sendPrivateRequest(String fromUser, String toUser)//send private request from host
    {
        //if the from user is the host, create the private server for the host
        if(fromUser.equals(getUsername()))
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

              //find the client and send that client the private invite
              for(int i = 0; i < clients.size(); i++)
              {
                  if(((client)clients.get(i)) != null)
                  {
                      try{
                      ((client)clients.get(i)).sendClientMessage("\\command-p-"+getUsername()+"-"+privatePort+"-"+ host +"\\");
                      }
                      catch(IOException ioe)
                      {}
                      break;
                  }
              }

        }
       
    }

    private class acceptClients implements Runnable
    {
        public void run(){
             //accept the clients

            int i = 0;
            while(true){
                try
                {
                Socket s = server.accept();
                clients.add(new client(s));
                i++;
                }
                catch(IOException ioe)
                {

                }
            }

        }
    }


    private class client
    {
        private Socket clientSocket;
        private InetAddress clientInfo;
        private DataInputStream inStream;
        private DataOutputStream outStream;
        private String username;

        public String getHostIP()
        {
            return clientInfo.getHostAddress();
        }
        public String getClientUsername(){return this.username;}

        public client(Socket s)
        {
            this.clientSocket = s;

            try
            {
            clientInfo = clientSocket.getInetAddress();
            this.inStream =  new DataInputStream(clientSocket.getInputStream());
            this.outStream = new DataOutputStream(clientSocket.getOutputStream());

            //get the username

            this.username = this.inStream.readUTF();


            //check if username is already taken
            boolean taken = false;
            for(int i = 0; i < clients.size(); i++)
            {
                if(((client)clients.get(i)).getClientUsername().equals(this.username) && clients.get(i) != this)
                {
                    //taken already
                    taken = true;
                    break;
                }
            }

            this.outStream.writeUTF(taken ? "taken" : "not_taken");
                if(!taken)
                {
                    DefaultListModel userListModel = mainReference.getUserListModel();

                    //send new client all of the users on the list not including himself
                    Object[] userObjects = userListModel.toArray();
                    for(int i = 0; i < userObjects.length; i++)
                    {
                         sendClientMessage("\\command-a-"+((String)userObjects[i])+"\\");
                    }
                    //send clients user name to everybody not including himself
                    sendMessage("\\command-a-"+username+"\\");
                    //client isn't on the client array yet, so send him his username
                    sendClientMessage("\\command-a-"+username+"\\");

                    new Thread(new clientHandler()).start();
                }
                else//remove self from list
                {
                clients.remove(this);
                }
            }
            catch(IOException ioe)
            {
            mainReference.addMessage(clientInfo.getHostAddress() + " has had an IOException\n");
            }
        }
         public void sendClientMessage(String s) throws IOException
        {
            outStream.writeUTF(s);
        }

            private class clientHandler implements Runnable
            {

                public void run(){
                    try{

                    while(true){

                    
                    String messageFromClient = inStream.readUTF();
                    
                    sendMessage(messageFromClient);
                    //add it to itself as well
                    
                    }
                    }catch(EOFException eof){
                    mainReference.addMessage(clientInfo.getHostAddress() + " has disconnected");
                    }
                    catch(IOException ioe){
                     //For me, this exception occurs when someone disconnects, not the eofexception
                    sendMessage(getClientUsername() + " has disconnected\n");
                    //remove this client from the list
                    for(int i =0; i < clients.size(); i++)
                    {
                        if(((client)clients.get(i)).getClientUsername().equals(getClientUsername()))
                        {
                            clients.remove(i);
                        }
                    }
                    sendMessage("\\command-r-"+getClientUsername()+"\\");
                    
                    }
                }
            }
    }
}

