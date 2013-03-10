package chatprogram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class privateChatArea extends JFrame {

    private ArrayList files;
    private DefaultListModel fileModel = new DefaultListModel();
    private privateChatPanel mainPanel;
    private Main mainReference;
    private fileRecieverPanel fileReciever;
    private fileSenderPanel fileSender;
    private JButton acceptButton, declineButton;
    private JPanel acceptDecline;
    private Runnable privateThread;
    private int port;
    private String host;
    private String username, fromUser;
    private JFileChooser chooser = new JFileChooser();//chooser for both sender and recieer
    private DataOutputStream outStream;//writer to other client
    private DataInputStream inStream;//reader from other client
    private File saveDirectory;
    //streams for file server
    private DataOutputStream fileOut;
    private DataInputStream fileIn;

    public privateChatArea(boolean isHost, String host, int port, String username, String fromUser, Main mainRef) throws IOException {

        this.setSize(800, 300);
        this.setTitle("Private Chat");
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.host = host;
        this.port = port;
        this.username = username;
        this.fromUser = fromUser;
        this.mainReference = mainRef;
        mainPanel = new privateChatPanel();
        fileReciever = new fileRecieverPanel();
        fileSender = new fileSenderPanel();
        files = new ArrayList();

        this.add(mainPanel, BorderLayout.CENTER);
        this.add(fileReciever, BorderLayout.WEST);
        this.add(fileSender, BorderLayout.SOUTH);

        if (isHost) {
            privateThread = new privateHost();
            new Thread(privateThread).start();
        } else {
            //ask user if he/she wants to accept
            acceptButton = new JButton("Accept Private Chat with '" + fromUser + "'?");
            declineButton = new JButton("Decline");

            acceptButton.addActionListener(mainPanel);
            declineButton.addActionListener(mainPanel);

            acceptDecline = new JPanel(new FlowLayout());

            acceptDecline.add(acceptButton);
            acceptDecline.add(declineButton);
            mainPanel.add(acceptDecline, BorderLayout.NORTH);
            repaint();

            mainPanel.repaint();
            mainPanel.revalidate();

        }

        this.setVisible(true);
    }

    public void enableAllControls() {

        mainPanel.enablePanel();
        fileReciever.enablePanel();
        fileSender.enablePanel();

    }

    public String getFileName(String fileName) {
        int dirSplitIndex = fileName.lastIndexOf("/");
        if (dirSplitIndex == -1)//other slash for windows
        {
            dirSplitIndex = fileName.lastIndexOf("\\");
        }
        fileName = fileName.substring(dirSplitIndex + 1);

        return fileName;
    }

    private class privateHost implements Runnable {

        private ServerSocket server;
        private Socket client;
        private ServerSocket fileServer;
        private Socket fileClient;
        private int filePort;

        public privateHost() throws IOException {
            server = new ServerSocket(port);

            filePort = port + 1;
            //find a port for the fileserver
            while (fileServer == null) {
                try {
                    fileServer = new ServerSocket(filePort);
                } catch (IOException ioe) {
                    filePort++;
                    fileServer = null;
                }
            }
        }

        public void run() {
            //wait for client to accept invitation
            while (client == null) {
                try {
                    mainPanel.addMessage("Waiting for user to accept invite...\n");
                    client = server.accept();

                    outStream = new DataOutputStream(client.getOutputStream());
                    inStream = new DataInputStream(client.getInputStream());

                    //get response from invitation
                    String response = inStream.readUTF();
                    
                    if (response.equals("declined")) {
                        mainPanel.addMessage("User has declined invitation.\n");
                        return;
                    }
                    //otherwise, the user accepted the invitation

                    //tell client which port file server is on
                    outStream.writeInt(filePort);

                    //get the file client to accept
                    fileClient = fileServer.accept();
                    fileOut = new DataOutputStream(fileClient.getOutputStream());
                    fileIn = new DataInputStream(fileClient.getInputStream());

                    //create thread to detect and store incoming files
                    new Thread(new fileReaderThread()).start();

                    //enable all gui user controls
                    enableAllControls();

                    mainPanel.addMessage("User has accepted invite\n");
                }
                catch (IOException ioe)
                {
                    mainReference.error("Cannot connect to other user");
                }
            }
            //this class is also used as reader for regular messages
            while (true) {
                try {
                    String message = inStream.readUTF();
                    mainPanel.addMessage(message);
                } catch (IOException ioe) {
                    mainPanel.addMessage(fromUser + " has disconnected");
                    break;
                }
            }
        }
    }

    private class privateClient implements Runnable {

        private Socket client;
        private Socket fileClient;
        private int filePort;

        public privateClient(String host, int port, Boolean wasAccepted) throws IOException {
            //create the client
            client = new Socket(host, port);
            outStream = new DataOutputStream(client.getOutputStream());
            inStream = new DataInputStream(client.getInputStream());

            if (wasAccepted)//tell the other user that the invitation was accepted
            {
                outStream.writeUTF("accepted");
            } else {
                outStream.writeUTF("declined");
                return;
            }
            //get the file port
            filePort = inStream.readInt();
            //create the connection with the file server
            fileClient = new Socket(host, filePort);
            //get the file streams
            fileOut = new DataOutputStream(fileClient.getOutputStream());
            fileIn = new DataInputStream(fileClient.getInputStream());
            //create thread to detect and store incoming files
            new Thread(new fileReaderThread()).start();
        }

        public void run() {
            while (true) {
                try {
                    String message = inStream.readUTF();
                    mainPanel.addMessage(message);
                } catch (IOException ioe) {
                    mainPanel.addMessage(fromUser + " has disconnected");
                    break;
                }
            }
        }
    }

    private class privateChatPanel extends chatPanel implements ActionListener {

        public privateChatPanel() {
            super();
            send.addActionListener(this);
            inputBox.addActionListener(this);
            //disable controls until users are actually ready to chat.
            inputBox.setEnabled(false);
            send.setEnabled(false);
        }

        public void enablePanel() {
            inputBox.setEnabled(true);
            send.setEnabled(true);
            revalidate();
            repaint();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == inputBox || e.getSource() == send) {
                //current string
                String message = inputBox.getText();
                inputBox.setText("");
                message = username + " > " + message + "\n";
                addMessage(message);

                try {
                    outStream.writeUTF(message);
                } catch (IOException ioe) {
                }

            } else if (e.getSource() == acceptButton) {
                try {
                    mainPanel.remove(acceptDecline);
                    acceptDecline.repaint();
                    acceptDecline.revalidate();
                    this.repaint();
                    this.revalidate();
                    enableAllControls();
                    privateThread = new privateClient(host, port, true);
                    new Thread(privateThread).start();
                } catch (IOException ioe) {
                   
                    ioe.printStackTrace();
                }
            } else if (e.getSource() == declineButton) {
                try {
                    //tell other user that invitation has been decline
                    privateThread = new privateClient(host, port, false);
                } catch (IOException ioe) {
                    //ignore
                }
                dispose();
            }
        }
    }

    private class fileRecieverPanel extends JPanel implements ActionListener {

        private JList fileList;
        private JButton saveFile;

        public fileRecieverPanel() {
            saveFile = new JButton("Save File");
            saveFile.addActionListener(this);
            //disable until chat starts
            saveFile.setEnabled(false);
            this.setLayout(new BorderLayout());
            this.add(new JLabel("Files Recieved"), BorderLayout.NORTH);
            fileList = new JList(fileModel);
            JScrollPane scrollPane = new JScrollPane(fileList);
            scrollPane.setSize(100, 500);
            this.add(scrollPane, BorderLayout.CENTER);
            this.add(saveFile, BorderLayout.SOUTH);
        }

        public void enablePanel() {
            saveFile.setEnabled(true);
            revalidate();
            repaint();
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveFile)//user wants to request a private chat with another user
            {
                //check if there is a user selected
                if (fileList.getSelectedValue() == null) {
                    mainReference.error("No file selected");
                } else {
                    //prompt to save

                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                        saveDirectory = chooser.getSelectedFile();
                        //save the current selected file
                        new Thread(new Runnable() {

                            private String fileNameSelected = (String) fileList.getSelectedValue();

                            public void run() {
                                try {                                  
                                    FileOutputStream out = new FileOutputStream(new File(saveDirectory.getAbsolutePath() + "/" + fileNameSelected));
                                    //get the file
                                    file fileSelected = null;
                                    for (int i = 0; i < files.size(); i++) {
                                        if (((file) files.get(i)).getName().equals(fileNameSelected)) {
                                            fileSelected = (file) files.get(i);
                                        }
                                    }
                                    if (fileSelected != null) {
                                        out.write(fileSelected.getData());
                                    }
                                    out.close();
                                } catch (IOException ioe) {
                                }
                            }
                        }).start();

                    } else {
                        //Ignore
                    }




                }

            }
        }
    }

    private class fileReaderThread implements Runnable {
        //create new thread to recieve commands and file data

        public void run() {
            try {
                //get the original file command from the user
                String command = fileIn.readUTF();
                
                //get the file size
                int fileSize = Integer.parseInt(command.substring(command.lastIndexOf("-") + 1, command.length() - 1));
                int dashIndex = command.indexOf("-");
                String fileName = command.substring(dashIndex + 1, command.lastIndexOf("-"));

               
                //create new file object to store data

                //make the file object and add to list
                file tmp = new file(fileSize, fileName);
                files.add(tmp);
                fileModel.add(fileModel.getSize(), fileName);
                
                fileIn.read(tmp.getData());


                //get the next command



            } catch (FileNotFoundException fnfe) {
                mainReference.error("Could not read file.");
            } catch (EOFException eofe) {
                mainReference.error(eofe.getMessage());
            } catch (IOException ioe) {
            }
        }
    }

    private class file {

        private byte[] data;
        private String name;

        public byte[] getData() {
            return data;
        }

        public String getName() {
            return name;
        }

        public file(int length, String fileName) {
            data = new byte[length];
            name = fileName;
        }
    }

    private class fileSenderPanel extends JPanel implements ActionListener {

        private File fileToSend;
        private JButton sendFile = new JButton("Send File"), browse = new JButton("Browse");
        private JTextField source = new JTextField(20);
        private DataInputStream fileIn;

        public fileSenderPanel() {

            sendFile.addActionListener(this);

            //disable controls until chat starts
            sendFile.setEnabled(false);
            browse.setEnabled(false);
            source.setEnabled(false);

            this.setLayout(new FlowLayout());
            this.add(sendFile);
            this.add(source);
            this.add(browse);

            browse.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    if (chooser.showOpenDialog(browse) == JFileChooser.APPROVE_OPTION) {
                        fileToSend = chooser.getSelectedFile();
                        source.setText(fileToSend.getAbsolutePath());
                        source.setBackground(Color.WHITE);
                    }

                }
            });
        }

        public void enablePanel() {
            sendFile.setEnabled(true);
            browse.setEnabled(true);
            source.setEnabled(true);
            revalidate();
            repaint();
        }

        public void actionPerformed(ActionEvent e)//let user choose a file to send to other user
        {
            if (e.getSource() == sendFile) {
                if (source.getText().equals("")) {
                    mainReference.error("No file specified");
                } else {


                    //create a temporary thread get the file and send the file
                    new Thread(new Runnable() {

                        public void run() {
                            try {


                                byte[] data;

                                long fileLength = fileToSend.length();
                                if (fileLength < 0) {
                                    throw new IOException("Invalid file size");
                                }

                                //send information about file name and length of file
                                String command = "\\file-" + getFileName(fileToSend.getAbsolutePath()) + "-" + fileLength + "\\";

                                fileOut.writeUTF(command);

                                DataInputStream fileReader = new DataInputStream(new FileInputStream(new File(fileToSend.getAbsolutePath())));//reads the file from users computer

                                data = new byte[(int) (fileLength)];

                                int read = fileReader.read(data);

                                fileOut.write(data, 0, read);//send to other user
                                
                                //reset the source textbox
                                source.setText("");

                            } catch (FileNotFoundException fnfe) {
                                mainReference.error("Could not send file.");
                            } catch (EOFException eofe) {
                                mainReference.error(eofe.getMessage());
                            } catch (IOException ioe) {
                            }
                        }
                    }).start();
                }
            }
        }
    }
}
