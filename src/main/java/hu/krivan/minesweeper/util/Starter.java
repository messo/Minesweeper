package hu.krivan.minesweeper.util;

import hu.krivan.minesweeper.client.Client;
import hu.krivan.minesweeper.server.Server;
import java.util.Arrays;

/**
 *
 * @author balint
 */
public class Starter {

    private static final String USAGE = "USAGE: start with `client` or `server` parameter";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(USAGE);
            return;
        }

        if (args[0].equals("server")) {
            Server.main(Arrays.copyOfRange(args, 1, args.length));
        } else if (args[0].equals("client")) {
            Client.main(Arrays.copyOfRange(args, 1, args.length));
        } else {
            System.out.println(USAGE);
        }
    }
}
