

package chatprogram;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

//make the main class also the login window so it can store the data about the user
public class Main extends JFrame implements ActionListener {

    private chatThread mainThread;

    //reference to chatting window
    private JFrame chatArea;
    private JComboBox hostOrJoin;
    private JPanel joinPanel, hostPanel;
    private JButton submitButton = new JButton("Join Chat");
    private JTextField joinIPAddress, hostPort, joinPort, hostUser, joinUser;
    private DefaultListModel userModel = new DefaultListModel();

    public chatThread getMainThread(){return mainThread;}
    public JFrame getChatArea(){return chatArea;}
    public DefaultListModel getUserListModel(){return userModel;}

    //adds message to this user's panel
    public void addMessage(String s){
        ((publicChatArea)chatArea).addMessage(s);
    }

    public void error(String errorMessage){
        JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Main(){

     String[] comboArray = {"Join a Conversation", "Host a Conversation"};
     hostOrJoin = new JComboBox(comboArray);
     this.setLayout(new BorderLayout(5, 5));
     this.add(hostOrJoin, BorderLayout.NORTH);


     //set up the seperate panels
     joinPanel = new JPanel(new GridLayout(3,2, 5, 5));
     joinPanel.add(new JLabel("Host:"));
     joinIPAddress = new JTextField();
     joinIPAddress.setText("localhost");
     joinPanel.add(joinIPAddress);
     joinPanel.add(new JLabel("Port:"));
     joinPort = new JTextField();
     joinPort.setText("1234");//default port
     joinPanel.add(joinPort);
     joinPanel.add(new JLabel("User Name:"));
     joinUser = new JTextField();
     joinPanel.add(joinUser);
     this.add(joinPanel, BorderLayout.CENTER);

     hostPanel = new JPanel(new GridLayout(3,2, 5, 5));
     hostPanel.add(new JLabel("Port:"));
     hostPort = new JTextField();
     hostPort.setText("1234");//default port
     hostPanel.add(hostPort);
     hostPanel.add(new JLabel("User Name:"));
     hostUser = new JTextField();
     hostPanel.add(hostUser);


     this.add(joinPanel, BorderLayout.CENTER);


     this.add(submitButton, BorderLayout.SOUTH);

     submitButton.addActionListener(this);
     //set event listener for dropdown box to change accordingly
     hostOrJoin.addItemListener(new ItemListener(){
         public void itemStateChanged(ItemEvent e)
         {
             if(hostOrJoin.getSelectedItem().equals("Join a Conversation"))
             {
              remove(hostPanel);
              add(joinPanel, BorderLayout.CENTER);
              hostPanel.revalidate();
              hostPanel.repaint();
              joinPanel.revalidate();
              joinPanel.repaint();
              submitButton.setText("Join Chat");
             }
             else
             {
              remove(joinPanel);
              add(hostPanel, BorderLayout.CENTER);
              hostPanel.revalidate();
              hostPanel.repaint();
              joinPanel.revalidate();
              joinPanel.repaint();
              submitButton.setText("Host Chat");
             }
         }
     });

    }
    public boolean validateUsername(String username)//does not check for duplicates. that is done in the joinThread class
    {
                        if(username.equals(""))
                        {
                        error("Please enter in a valid username");
                        return false;
                        }
                        else if(username.indexOf("//") != -1)
                        {
                        error("Please do not include any // characters in your name");
                        return false;
                        }
                        return true;
    }

 public void actionPerformed(ActionEvent e)
        {
        boolean isError = false;

            if(hostOrJoin.getSelectedItem().equals("Host a Conversation"))
            {
                int port = -1;
                String username;
                try
                {
                //get the port
                port = Integer.parseInt(hostPort.getText());
                //get the username
                username = hostUser.getText();
                    if(validateUsername(username))
                    {
                    //by creating the new thread, it will check if port is in use and check for username already taken
                    mainThread = new hostThread(port, username, this, 500);
                    }
                }

                catch(NumberFormatException nfe)
                {
                error("Not a valid port number");
                isError = true;
                }
                catch(IOException ioe)
                {
                error("Exception Thrown, port may already be in use.");
                isError = true;
                }

            }
            else if(hostOrJoin.getSelectedItem().equals("Join a Conversation"))
            {
                int port = -1;
                String host, username;
                try
                {
                //get the port
                port = Integer.parseInt(joinPort.getText());
                //get the host
                host = joinIPAddress.getText();
                //get the username
                username = joinUser.getText();

                  if(validateUsername(username))
                    {
                    //by creating the new thread, it will check if port is in use and check for username already taken
                     mainThread = new joinThread(port, host, username, 500, this);
                    }
               
                }
                catch(usernameTaken ut)
                {
                error(ut.getMessage());
                isError = true;
                }
                catch(NumberFormatException nfe)
                {
                error("Not a valid port number");
                isError = true;
                }
                catch(IOException ioe)
                {
                error("Exception Thrown, port/host may not be hosting.");
                isError = true;
                }
            }

            if(!isError)
            {
            chatArea = new publicChatArea(mainThread, this);
            chatArea.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//don't exit program until user closes the
            chatArea.setSize(500,300);
            chatArea.setLocationRelativeTo(null);
            chatArea.setVisible(true);
            dispose();
            }
        }
    public static void main(String[] args) {
        //ask user for login info
        JFrame splashScreen = new Main();
        splashScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//don't exit program until user closes the
        splashScreen.setSize(300,200);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setVisible(true);


    }

}
