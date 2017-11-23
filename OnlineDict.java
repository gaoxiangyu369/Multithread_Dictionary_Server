/**
 * Created by Student Name: Xiangyu Gao.
 */

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * This class is for providing a interactive interface for clients to send their requests and
 * get feedback.
 * The content mainly contains search field, button field and display/edit field.
 */
public class OnlineDict {
    private JPanel MainInterface;
    private JPanel Function;
    private JPanel ShowInfo;
    private JTextField searchTextField;
    private JButton Search;
    private JButton Add;
    private JButton Delete;
    private JTextArea meaningShow;
    private JLabel hint;
    private JScrollPane info;
    private boolean addConfirm = false;

    /**
     * Constructors
     */
    public OnlineDict() {
    }

    public OnlineDict(Socket socket) {
        // Get the input/output streams for reading/writing data from/to the socket.

        // Initialize the state of text area.
        editableStatus(false);

        // Execute search function by pressing enter.
        searchTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchPerform(socket);
                }
            }
        });

        // Execute search function by clicking Search button.
        Search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPerform(socket);
            }
        });

        // Execute delete function by clicking Delete button.
        Delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePerform(socket);
            }
        });

        // Execute add function by clicking Add button.
        Add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPerform(socket);
            }
        });
    }

    public static void main(String[] args) {
        Socket socket = null;
        //Object that will store the parsed command line arguments
        OnlineDictCommand argsBean = new OnlineDictCommand();

        //Parser provided by args4j
        CmdLineParser parser = new CmdLineParser(argsBean);

        try {
            // Create a stream socket bounded to a specific port and connect it to the
            // socket bound to localhost.
            parser.parseArgument(args);
            socket = new Socket(argsBean.getHost(), argsBean.getPort());
            JFrame frame = new JFrame("OnlineDict");
            frame.setContentPane(new OnlineDict(socket).MainInterface);
            frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int n = JOptionPane.showConfirmDialog(null,
                            "Do you want to exit?", "Confirm",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (n == 0) {
                        System.exit(0);
                    }
                }
            });
        }
        catch (CmdLineException e) {
            System.out.println("Error parsing parameters.");
            System.exit(0);
        }
        catch (UnknownHostException e) {
            System.out.println("Host Resolution Failed.");
            JOptionPane.showMessageDialog(null,
                    "Host Resolution Failed.", "Error", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
        catch (SocketException e) {
            JOptionPane.showMessageDialog(null, "Socket closed.",
                    "Error", JOptionPane.PLAIN_MESSAGE);
        }
        catch (IOException e) {
            System.out.println("Connection Failed.");
            JOptionPane.showMessageDialog(null,
                    "Connection Failed.", "Error", JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
    }

    /**
     * This method is for query function.
     */
    private void searchPerform(Socket socket) {
        editableStatus(false);
        addConfirm = false;
        if (!searchTextField.getText().equals("")) {
            String clientMsg = "Search " + searchTextField.getText() + "\n";
            if (outputStream(clientMsg, socket)) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                            "UTF-8"));
                    String received = in.readLine();
                    // Initialize the content area.
                    meaningShow.setText("");
                    if (!received.equals("No this word")) {
                        showMeaning(received);
                    }
                    else{
                        noThisWordError();
                    }
                }
                catch (IOException e) {
                    noConnectionError();
                }
            }
        }
    }

    /**
     * This method is for deletion function.
     */
    private void deletePerform(Socket socket) {
        // The text area is not editable when Deletion.
        editableStatus(false);
        addConfirm = false;
        if (!searchTextField.getText().equals("")) {
            // Communicate with the server by sending deletion request.
            // Search the word first before deletion.
            String clientMsg = "Delete " + searchTextField.getText() + " N" + "\n";
            if (outputStream(clientMsg, socket)) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                            "UTF-8"));
                    String received = in.readLine();
                    meaningShow.setText("");
                    if (!received.equals("No this word")) {
                        showMeaning(received);
                        // Confirm the deletion request.
                        int n = JOptionPane.showConfirmDialog(null,
                                "Do you really want to delete?", "Confirm",
                                JOptionPane.YES_NO_OPTION);
                        if (n == 0) {
                            String clientConfirm = "Delete " + searchTextField.getText() +
                                    " Y" + "\n";
                            if (outputStream(clientConfirm, socket)) {
                                BufferedReader input = null;
                                try {
                                    input = new BufferedReader(
                                            new InputStreamReader(
                                                    socket.getInputStream(), "UTF-8"));
                                    JOptionPane.showMessageDialog(null,
                                            input.readLine());
                                    meaningShow.setText("Error: No this word");
                                }
                                catch (IOException e) {
                                    noConnectionError();
                                }
                            }
                        }
                    }
                    else {
                        noThisWordError();
                    }
                }
                catch (IOException e) {
                    noConnectionError();
                }
            }
        }
    }

    /**
     * This method is for insertion function.
     */
    private void addPerform(Socket socket) {
        if (!searchTextField.getText().equals("")) {
            if (addConfirm) {
                // Activate the text area when add function is feasible.
                editableStatus(true);
                int n = JOptionPane.showConfirmDialog(null,
                        "Are you sure?", "Confirm",
                        JOptionPane.YES_NO_CANCEL_OPTION);
                if (n == 0) {
                    // The meaning can not be empty.
                    if (meaningShow.getText().equals("")) {
                        JOptionPane.showMessageDialog(null,
                                "No input. Please try again.", "Error",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                    // The input must be ended up with semicolon.
                    else if (!meaningShow.getText().contains(";") ||
                            (meaningShow.getText().lastIndexOf(";") !=
                                    meaningShow.getText().length() - 1)) {
                        JOptionPane.showMessageDialog(null,
                                "No semicolon(;). Please try again.",
                                "Error", JOptionPane.PLAIN_MESSAGE);
                    }
                    else {
                        String clientConfirm = "Add " + searchTextField.getText() + " Y "
                                + meaningShow.getText() + "\n";
                        if (outputStream(clientConfirm, socket)) {
                            BufferedReader in = null;
                            try {
                                in = new BufferedReader(
                                        new InputStreamReader(
                                                socket.getInputStream(), "UTF-8"));
                                JOptionPane.showMessageDialog(null, in.readLine());
                                // The text area become uneditable after addition.
                                meaningShow.setEditable(false);
                                addConfirm = false;
                            }
                            catch (IOException e) {
                                noConnectionError();
                            }
                        }
                    }
                }
                // The text area is uneditable if the word is already in dictionary.
                else {
                    addConfirm = false;
                    meaningShow.setText("");
                    editableStatus(false);
                }
            }
            // Do the search function first before addition.
            else {
                String clientMsg = "Add " + searchTextField.getText() + " N" + "\n";
                if (outputStream(clientMsg, socket)) {
                    BufferedReader in = null;
                    try {
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                                "UTF-8"));
                        String received = in.readLine();
                        meaningShow.setText("");
                        if (received.equals("No this word")) {
                            JOptionPane.showMessageDialog(null,
                                    "No this word. Please continue.", "Confirm",
                                    JOptionPane.OK_OPTION);
                            editableStatus(true);
                            // The addition confirmation becomes true when this word is not
                            // in dictionary.
                            addConfirm = true;
                            return;
                        }
                        else {
                            showMeaning(received);
                            JOptionPane.showMessageDialog(null,
                                    "The word is already in OnlineDict.","Confirm",
                                    JOptionPane.OK_OPTION);
                        }
                    }
                    catch (IOException e) {
                        noConnectionError();
                    }
                }
            }
        }
    }

    /**
     * This method is for controlling the text area's editable state.
     */
    private void editableStatus(boolean editable) {
        meaningShow.setEditable(editable);
        hint.setVisible(editable);
    }

    private void showMeaning(String input) {
        StringTokenizer meaning = new StringTokenizer(input, ";");
        int count = 1;
        while (meaning.hasMoreTokens()) {
            meaningShow.append(count + ". " + meaning.nextToken() + "\n");
            count ++;
        }
    }

    private void noThisWordError() {
        meaningShow.setText("Error: No this word");
        JOptionPane.showMessageDialog(null,
                "No this word.", "Error", JOptionPane.PLAIN_MESSAGE);
    }

    private void noConnectionError() {
        JOptionPane.showMessageDialog(null,
                "Error communicating with Server.", "Error",
                JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * This method is for creating output streams for communicating to the server.
     */
    private boolean outputStream(String message, Socket socket) {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
                    "UTF-8"));
            out.write(message);
            out.flush();
            return true;
        }
        catch (IOException e) {
            noConnectionError();
            return false;
        }
    }
}