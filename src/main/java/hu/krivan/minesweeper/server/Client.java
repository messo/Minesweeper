package hu.krivan.minesweeper.server;

import hu.krivan.minesweeper.common.Coords;
import hu.krivan.minesweeper.util.SocketUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author balint
 */
public class Client {

    private Socket cso;
    private String nickname;
    private Client opponent = null;
    private int points = 0;
    private BufferedReader is;
    private PrintWriter os;
    public final ChallengeObserver challengeObserver = new ChallengeObserver();
    public boolean playRequest;

    public class ChallengeObserver {

        public volatile boolean accepted;
    }

    public class GameHandler extends Thread {

        @Override
        public void run() {
        }
    }

    /**
     * Létrehozzuk a klienst: ezen keresztül kommunikálunk a klienssel.
     * A klienshez tartozó Sockethez, létrehozzuk az I/O stream-eket.
     * 
     * @param cso
     */
    public Client(Socket cso, Server server) {
        try {
            this.cso = cso;

            is = SocketUtil.getBufferedReader(cso);
            os = SocketUtil.getPrintWriter(cso);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public String getHost() {
        return cso.getInetAddress().getHostName();
    }

    public String getIP() {
        return cso.getInetAddress().getHostAddress();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Client getOpponent() {
        return opponent;
    }

    public void addPoint() {
        points++;
    }

    public int getPoints() {
        return points;
    }

    /**
     * Üzenetküldés a kliensnek.
     * 
     * @param string
     */
    public synchronized void sendMessage(String string) {
        os.print(string + "\r\n");
        os.flush();
    }

    public String readMessage() throws IOException {
        return is.readLine();
    }

    /**
     * Megkérjük a klienst, hogy lépjen (kapcsoljon valahova a táblán)
     * 
     * @return koordináták
     */
    public Coords ask() {
        try {
            sendMessage("ASK");
            String s = is.readLine();
            if (s.startsWith("CLICK ")) {
                String[] coords = s.substring(6).split(" ");
                if (coords.length != 2) {
                    return null;
                }
                try {
                    return new Coords(Integer.parseInt(coords[0]) - 1, Integer.parseInt(coords[1]) - 1);
                } catch (NumberFormatException ex) {
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * Figyelmeztetjük a klienst, hogy egy játékos szeretne vele játszani.
     * 
     * @param player
     */
    public void sendPlayRequest(Client player) {
        sendMessage("WANNAPLAY " + player.getNickname());
        // beállítjuk, hogy most playRequest van, tehát csak erre figyelünk -> ACCEPT/REFUSE
        playRequest = true;
        opponent = player;
    }

    public void disconnect() {
        try {
            cso.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
