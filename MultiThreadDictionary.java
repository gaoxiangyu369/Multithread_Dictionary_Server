/**
 * Created by Student Name: Xiangyu Gao.
 */

import java.io.*;
import java.net.Socket;
import org.json.*;
import java.net.SocketException;
import java.util.StringTokenizer;

/**
 * This class is for multiple threads performing concurrent operations in a dictionary.
 * The requests mainly contain querying the meaning(s) of a given word, adding a new word and
 * deleting an existing word.
 */
public class MultiThreadDictionary extends Thread {

    private Socket clientSocket;
    private int clientNum;
    private File filename;
    private DictServer server;

    /**
     * Constructors
     */
    public MultiThreadDictionary(Socket clientSocket, int clientNum, File filename,
                                 DictServer server) {
        this.clientSocket = clientSocket;
        this.clientNum = clientNum;
        this.filename = filename;
        this.server = server;
    }

    @Override
    public void run(){
        try {
            // Get the input/output streams for communicating to the client.
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream(), "UTF-8"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    clientSocket.getOutputStream(), "UTF-8"));

            String clientMsg, command, word, meaning, confirm;

            while ((clientMsg = in.readLine())!= null) {

                StringTokenizer input = new StringTokenizer(clientMsg, " ");
                // Keep track of each client request.
                server.recordArea.append("Message from client " + clientNum + ": " +
                        clientMsg + "\n");
                command = input.nextToken();

                switch (command) {
                    // Search an existed word's meanings.
                    case "Search":
                        JSONObject dataSet1 = readFile();
                        word = input.nextToken();
                        query(word, dataSet1, out);
                        break;
                    // Add a new word to the dictionary.
                    case "Add":
                        JSONObject dataSet2 = readFile();
                        word = input.nextToken();
                        confirm = input.nextToken();
                        // Check the word in dictionary before adding it.
                        if (confirm.equals("N"))
                            query(word, dataSet2, out);
                        else {
                            meaning = input.nextToken("\n").trim();
                            insert(word, meaning, dataSet2, out);
                        }
                        break;
                    // Delete an exited word.
                    case "Delete":
                        JSONObject dataSet3 = readFile();
                        word = input.nextToken();
                        confirm = input.nextToken();
                        // Check the word in dictionary before removing it.
                        if (confirm.equals("N"))
                            query(word, dataSet3, out);
                        else
                            remove(word, dataSet3, out);
                        break;
                }
                server.recordArea.append("Response sent" + "\n");
            }
        }
        catch(SocketException e) {
            server.recordArea.append("Socket closed." + "\n");
        }
        catch(FileNotFoundException e) {
            server.recordArea.append("Error opening the dictionary.");
        }
        catch(JSONException e) {
            server.recordArea.append("Error parsing the dictionary." + "\n");
        }
        catch(IOException e) {
            server.recordArea.append("Error sending/getting information." + "\n");
        }
        finally {
            // Close the socket when done.
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                    server.recordArea.append("********************" + "\n");
                    server.recordArea.append("The Client connection number " + clientNum +
                            " is over. " + server.setDate() + "\n");
                }
                catch (IOException e) {
                    server.recordArea.append("Error closing Client Socket." + "\n");
                }
            }
        }
    }

    /**
     * This method is the process of query in dictionary.
     */
    private void query(String word, JSONObject dictData, BufferedWriter output)
            throws IOException {
        // If the word is in the dictionary, just display the meaning(s) of that word.
        if (dictData.has(word.toLowerCase())) {
            JSONArray w = dictData.getJSONArray(word.toLowerCase());
            String m = w.getString(0);
            output.write(m + "\n");
            output.flush();
        }
        // If the word is not found, display error message.
        else {
            output.write("No this word" + "\n");
            output.flush();
        }
    }

    /**
     * This method is the process of addition in dictionary. The synchronized keyword is for
     * keeping the dictionary in a consistent state.
     */
    private synchronized void insert(String word, String meaning, JSONObject dictData,
                                     BufferedWriter output)
            throws IOException {
        JSONArray newWordMeaning = new JSONArray();
        newWordMeaning.put(meaning);
        // Add the word into dictionary with its associated meaning(s).
        dictData.put(word.toLowerCase(), newWordMeaning);
        // Return the action result.
        output.write("Dictionary Updated" + "\n");
        output.flush();
        // Updating the dictionary.
        updateFile(dictData);
    }

    /**
     * This method is the process of deletion in dictionary. The synchronized keyword is for
     * keeping the dictionary in a consistent state.
     */
    private synchronized void remove(String word, JSONObject dictData, BufferedWriter output)
            throws IOException {
        dictData.remove(word.toLowerCase());
        // Return the action result.
        output.write("Dictionary Updated" + "\n");
        output.flush();
        // Updating the dictionary.
        updateFile(dictData);
    }

    /**
     * This method is for loading the dictionary data format.
     */
    private JSONObject readFile() throws FileNotFoundException, JSONException {
        JSONTokener dicRead = new JSONTokener(new FileReader(filename));
        JSONObject dataSet = new JSONObject(dicRead);
        return dataSet;
    }

    /**
     * This method is for updating the dictionary data format.
     */
    private void updateFile(JSONObject dictData) {
        FileWriter outputStream = null;
        try {
            outputStream = new FileWriter(filename, false);
            outputStream.write(dictData.toString());
            outputStream.flush();
            outputStream.close();
            server.recordArea.append("Dictionary Updated" + "\n");
        }
        catch (IOException e) {
            server.recordArea.append("Error writing to the dictionary." + "\n");
        }
    }
}