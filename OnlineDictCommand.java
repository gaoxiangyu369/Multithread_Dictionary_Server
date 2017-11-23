/**
 * Created by Student Name: Xiangyu Gao.
 */

import org.kohsuke.args4j.Option;

public class OnlineDictCommand {
    @Option(required = true, name = "-h", usage = "Host name")
    private String host;

    @Option(required = false, name = "-p", usage = "Port number")
    private int port;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
