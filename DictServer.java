/**
 * Created by Student Name: Xiangyu Gao.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This class is for setting a dictionary server anf waiting for clients requests.
 * It mainly contains a log that records each request information and the response.
 */
public class DictServer {
    private JPanel Main;
    public JTextArea recordArea;
    private JScrollPane record;
    private JButton closeButton;
    private DictionaryCommand argsBean;


    public DictServer() {

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = JOptionPane.showConfirmDialog(null,
                        "Do you really want to exit?", "Confirm",
                        JOptionPane.YES_NO_OPTION);
                if (n == 0) {
                    System.out.println("Server is offline.");
                    System.exit(0);
                }
            }
        });
    }

    public static void main(String[] args) {

        ServerSocket listeningSocket = null;
        Socket clientSocket = null;

        // Object that will store the parsed command line arguments.
        DictionaryCommand argsBean = new DictionaryCommand();

        // Parser provided by args4j.
        CmdLineParser parser = new CmdLineParser(argsBean);

        DictServer dic = new DictServer();

        JFrame frame = new JFrame("DictServer");
        frame.setContentPane(dic.Main);
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
                    System.out.println("Server is offline.");
                    System.exit(0);
                }
            }
        });

        try {
            parser.parseArgument(args);
            // Open a server socket listening on the specific port number.
            listeningSocket = new ServerSocket(argsBean.getPort());

            dic.recordArea.setText("Server is online. Waiting for a connection. (PortNum: "
                    + argsBean.getPort() + ", Time: " + setDate() + ")" + "\n");
            // Keep track of the number of clients.
            int i = 0;
            // Listen for incoming connections forever.
            while (true) {
                // Wait for an incoming client connection request.
                clientSocket = listeningSocket.accept();
                i++;
                showStartInfo(dic, clientSocket, i);

                // Process multiple clients at the same time by using threads.
                Thread worker = new Thread(new MultiThreadDictionary(clientSocket, i,
                        argsBean.getFile(), dic));
                worker.start();
            }
        }
        catch (CmdLineException e) {
            System.out.println("Error parsing parameters.");
            System.exit(0);
        }
        catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error setting up socket.", "Error",
                    JOptionPane.PLAIN_MESSAGE);
            System.out.println("Error setting up socket.");
        }
    }

    // Print each related client information when connection is successful.
    private static void showStartInfo(DictServer dic, Socket clientSocket, int counter) {
        dic.recordArea.append("********************" + "\n");
        dic.recordArea.append("Client connection number " + counter + " accepted at "
                + setDate() + "." + "\n");
        dic.recordArea.append("Remote Port: " + clientSocket.getPort() + "\n");
        dic.recordArea.append("Remote Hostname: "
                + clientSocket.getInetAddress().getHostName() + "\n");
        dic.recordArea.append("Local Port: " + clientSocket.getLocalPort() + "\n");
    }

    public static String setDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        return date;
    }
}