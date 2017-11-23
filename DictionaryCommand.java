/**
 * Created by Student Name: Xiangyu Gao.
 */

import org.kohsuke.args4j.Option;
import java.io.File;

public class DictionaryCommand {
    @Option(required = true, name = "-f", usage = "File name")
    private File file;

    @Option(required = false, name = "-p", usage = "Port number")
    private int port;

    public File getFile() {
        return file;
    }

    public int getPort() {
        return port;
    }
}
