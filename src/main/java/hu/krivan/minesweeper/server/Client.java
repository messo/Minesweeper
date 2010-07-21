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
    private Server server;
    private Client opponent = null;
    private int points = 0;
    public BufferedReader is;
    public PrintWriter os;
    public boolean playRequest = false;
    public final ChallengeObserver challengeObserver = new ChallengeObserver();

    public class ChallengeObserver {

        public volatile boolean accepted;
    }

    public class LobbyHandler extends Thread {

        private boolean shouldRun = true;

        @Override
        public void run() {
            try {
                String s, cmd, arg;
                String[] tokens;

                // beloginoltatjuk a klienst!
                login();

                this.setName("LobbyHandler-" + getNickname());

                // Most kezeljük a GAME előtti parancsokat.
                while (shouldRun) {
                    s = is.readLine();
                    if (s == null) {
                        //megszakadt a kapcsolat
                        server.kick(Client.this);
                        break;
                    }
                    tokens = s.split(" ");
                    if (tokens.length == 0) {
                        continue;
                    }
                    cmd = tokens[0];
                    if (playRequest) { // ha játékra kértük fel a klienst, akkor csak ACCEPT, vagy REFUSE
                        if (cmd.equals("ACCEPT")) {
                            playRequest = false;
                            server.onClientAcceptedToPlay(Client.this);
                            break;
                        } else if (cmd.equals("REFUSE")) {
                            playRequest = false;
                            server.onClientRefusedToPlay(Client.this);
                        }
                    } else {
                        if (cmd.equals("PLAYERLIST")) {
                            server.sendPlayerListTo(Client.this);
                        } else if (cmd.equals("PLAY")) {
                            arg = tokens[1];
                            server.onClientWantsToPlayWith(Client.this, Integer.parseInt(arg));
                            synchronized (Client.this.challengeObserver) {
                                // megállítjuk a szálat, ameddig a felkért user el nem fogadja/utasítja a felkérést.
                                Client.this.challengeObserver.wait();
                            }
                            // ha meghozta a döntést, és elfogadta, akkor kilépünk a szálból, hiszen kezdődik a játék
                            if (Client.this.challengeObserver.accepted) {
                                break;
                            }
                        }
                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public class GameHandler extends Thread {

        @Override
        public void run() {
        }
    }

    public void handleLobby() {
        new LobbyHandler().start();
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
            this.server = server;

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

    /**
     * Beloginoltatjuk a klienst.
     * 
     * @throws java.io.IOException
     */
    public void login() throws IOException {
        sendMessage("HI");
        do {
            String s = is.readLine();
            if (!s.startsWith("MYNAME ")) {
                continue;
            }
            nickname = s.substring(7);
        } while (server.isNicknameReserved(this));
        server.onClientLogined(this);
    }

    public void disconnect() {
        try {
            cso.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
