
package chatprogram;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class chatPanel extends JPanel implements ActionListener{
    protected JTextField inputBox = new JTextField();
    protected JScrollPane scrollBox;
    protected JTextArea mainDisplay = new JTextArea();
    protected JButton send = new JButton("Send");
    private chatThread mainThread;

    public void addMessage(String s)
    {
        mainDisplay.append(s);
        //auto scroll to bottom
        scrollBox.getVerticalScrollBar().setValue(scrollBox.getVerticalScrollBar().getMaximum());
    }
    public chatPanel()
    {
    this.setLayout(new BorderLayout(5,5));
    mainDisplay.setEditable(false);
    scrollBox = new JScrollPane(mainDisplay);
    this.add(scrollBox, BorderLayout.CENTER);
    JPanel inputArea = new JPanel();
    inputArea.setLayout(new BorderLayout(5,5));
    inputArea.add(inputBox, BorderLayout.CENTER);
    inputArea.add(send, BorderLayout.EAST);
    this.add(inputArea, BorderLayout.SOUTH);
    }
    
    public chatPanel(chatThread mainThread)
    {
    this.mainThread = mainThread;
    //set the layout and add the gui parts
    this.setLayout(new BorderLayout(5,5));
    mainDisplay.setEditable(false);
    scrollBox = new JScrollPane(mainDisplay);
    this.add(scrollBox, BorderLayout.CENTER);
    JPanel inputArea = new JPanel();
    inputArea.setLayout(new BorderLayout(5,5));
    inputArea.add(inputBox, BorderLayout.CENTER);
    inputArea.add(send, BorderLayout.EAST);
    this.add(inputArea, BorderLayout.SOUTH);

    //add actionlisteners
    inputBox.addActionListener(this);
    send.addActionListener(this);
   
    }

      public void actionPerformed(ActionEvent e)
       {
          if(e.getSource() == inputBox || e.getSource() == send)
          {
           //current string
           String message = inputBox.getText();
           inputBox.setText("");
           mainThread.sendMessage(mainThread.getUsername() + " > " + message + "\n");
          }
       }
}
