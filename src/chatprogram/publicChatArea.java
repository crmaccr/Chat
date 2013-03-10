
package chatprogram;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class publicChatArea extends JFrame{
    private usersPanel users;
    private chatPanel chatArea;
    private chatThread mainThread;
    private Main mainReference;

    public void addMessage(String s)
    {
        chatArea.addMessage(s);
    }


    public publicChatArea(chatThread mainThread, Main mainReference){
    this.setSize(600,500);
    this.setLocationRelativeTo(null);
    this.setTitle("Main Chat");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new BorderLayout(5,5));
    this.mainReference = mainReference;
    this.mainThread = mainThread;
    //add users area
    users = new usersPanel();
    this.add(users, BorderLayout.WEST);
    //add main chat area and user input area
    chatArea =new chatPanel(mainThread);
    this.add(chatArea, BorderLayout.CENTER);
    this.setVisible(true);
    }

    private class usersPanel extends JPanel implements ActionListener
    {

        private JList userList;

        private JButton startPrivate = new JButton("Request Private Chat");

        public usersPanel(){
        startPrivate.addActionListener(this);
        this.setLayout(new BorderLayout());
        this.add(new JLabel("Users"), BorderLayout.NORTH);
        userList = new JList(mainReference.getUserListModel());
        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setSize(100,500);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(startPrivate, BorderLayout.SOUTH);
        }

        public void actionPerformed(ActionEvent e)
        {
            if(e.getSource() == startPrivate)//user wants to request a private chat with another user
            {
                //check if there is a user selected
                if(userList.getSelectedValue() == null)
                {
                    mainReference.error("No user selected");
                }
                else if(userList.getSelectedValue().equals(mainReference.getMainThread().getUsername()))
                {
                    mainReference.error("You cannot select yourself");
                }
                else
                {
                //to user
                String toUser = (String)userList.getSelectedValue();
                //send request
                mainThread.sendPrivateRequest(mainThread.getUsername(), toUser);
                }

            }
        }
        
    }
}



