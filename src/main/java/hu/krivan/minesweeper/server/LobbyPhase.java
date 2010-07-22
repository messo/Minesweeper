package hu.krivan.minesweeper.server;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Ezzel kezelünk egy kliens-server kommunikációt, amikor már el van nevezve
 * a felhasználónk, tehát játékra kész, de még nem kezdett játszani
 *
 * @author balint
 */
public class LobbyPhase extends Thread {

    private Client client;
    private Server server;
    private boolean shouldRun = true;

    public LobbyPhase(Client client, Server server) {
        super("LobbyPhase-" + client.getNickname());
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            String s, cmd, arg;
            String[] tokens;

            // Most kezeljük a GAME előtti parancsokat.
            while (shouldRun) {
                s = client.readMessage();
                if (s == null) {
                    //megszakadt a kapcsolat
                    server.kick(client);
                    break;
                }
                tokens = s.split(" ");
                if (tokens.length == 0) {
                    continue;
                }
                cmd = tokens[0];
                if (client.playRequest) { // ha játékra kértük fel a klienst, akkor csak ACCEPT, vagy REFUSE
                    if (cmd.equals("ACCEPT")) {
                        client.playRequest = false;
                        onAcceptedToPlay();
                        break;
                    } else if (cmd.equals("REFUSE")) {
                        client.playRequest = false;
                        onRefusedToPlay();
                    }
                } else {
                    if (cmd.equals("PLAYERLIST")) {
                        sendPlayerList();
                    } else if (cmd.equals("PLAY")) {
                        arg = tokens[1];
                        server.onClientWantsToPlayWith(client, Integer.parseInt(arg));
                        synchronized (client.challengeObserver) {
                            // megállítjuk a szálat, ameddig a felkért user el nem fogadja/utasítja a felkérést.
                            client.challengeObserver.wait();
                        }
                        // ha meghozta a döntést, és elfogadta, akkor kilépünk a szálból, hiszen kezdődik a játék
                        if (client.challengeObserver.accepted) {
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

    private void sendPlayerList() {
        StringBuilder sb = new StringBuilder();
        ArrayList<Client> room = server.getLobby();
        sb.append("+PLAYERLIST\r\n");
        sb.append(room.size());
        sb.append("\r\n");
        for (Client c : room) {
            sb.append(c.getNickname());
            sb.append("\r\n");
        }
        // FIXME kell ez ide?
        sb.delete(sb.length() - 2, sb.length());
        client.sendMessage(sb.toString());
    }

    private void onRefusedToPlay() {
        Client opponent = client.getOpponent();
        opponent.sendMessage("REFUSED");
        opponent.challengeObserver.accepted = false;
        // értesítjük a ClientConnection szálat, hogy kész vagyunk a challenge-el
        synchronized (opponent.challengeObserver) {
            opponent.challengeObserver.notify();
        }
    }

    private void onAcceptedToPlay() {
        Client opponent = client.getOpponent();
        opponent.sendMessage("ACCEPTED");
        opponent.challengeObserver.accepted = true;
        // értesítjük a ClientConnection szálat, hogy kész vagyunk a challenge-el
        synchronized (opponent.challengeObserver) {
            opponent.challengeObserver.notify();
        }

        server.startGame(client, opponent);
    }
}
