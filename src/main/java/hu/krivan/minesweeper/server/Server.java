package hu.krivan.minesweeper.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author balint
 */
public class Server extends Thread {

    private static final int DEFAULT_PORT = 12345;
    private int port;
    public boolean should_run = true;
    private ServerSocket sso;
    private ArrayList<Client> lobby;
    private ArrayList<Client> newcomers;
    private ArrayList<Game> playingRooms;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else {
            System.out.println("No port was given, fallback to the default: " + DEFAULT_PORT);
        }

        new Server(port);
    }

    public Server(int port) {
        this.port = port;
        lobby = new ArrayList<Client>();
        newcomers = new ArrayList<Client>();
        playingRooms = new ArrayList<Game>();

        System.out.println("The server has been started. It's listening on port " + port);
        try {
            sso = new ServerSocket(this.port, 10);
            start();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    @Override
    public void run() {
        // gyűjtjük az embereket.
        while (should_run) {
            try {
                Socket cso = sso.accept();
                Client c = new Client(cso, this);
                newcomers.add(c); // hozzáadjuk a klienst a kliens listához.
                c.handleLobby();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void sendPlayerListTo(Client c) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Client> room = (ArrayList<Client>) lobby.clone();
        sb.append("+PLAYERLIST\r\n");
        sb.append(room.size());
        sb.append("\r\n");
        for (Client client : room) {
            sb.append(client.getNickname());
            sb.append("\r\n");
        }
        sb.delete(sb.length()-2, sb.length());
        c.sendMessage(sb.toString());
    }

    public void onClientWantsToPlayWith(Client player, int target) {
        // szeretnénk játszani a #target ID-jű játékossal. Lehet, hogy már kilépett,
        // vagy más van helyette nem probléma.

        Client opponent = lobby.get(target);

        // player vs. opponent
        opponent.sendPlayRequest(player);
    }

    public void kick(Client c) {
        lobby.remove(c);
        c.disconnect();
    }

    public void onClientRefusedToPlay(Client c) {
        Client opponent = c.getOpponent();
        opponent.sendMessage("REFUSED");
        opponent.challengeObserver.accepted = false;
        // értesítjük a ClientConnection szálat, hogy kész vagyunk a challenge-el
        synchronized (opponent.challengeObserver) {
            opponent.challengeObserver.notify();
        }
    }

    public void onClientAcceptedToPlay(Client c) {
        Client opponent = c.getOpponent();
        opponent.sendMessage("ACCEPTED");
        opponent.challengeObserver.accepted = true;
        // értesítjük a ClientConnection szálat, hogy kész vagyunk a challenge-el
        synchronized (opponent.challengeObserver) {
            opponent.challengeObserver.notify();
        }

        // remove them from the lobby.
        lobby.remove(c);
        lobby.remove(opponent);

        // let's start the game:
        Game g = new Game(c, opponent);
        playingRooms.add(g);
        g.start();
    }

    public void onClientLogined(Client c) {
        newcomers.remove(c);
        lobby.add(c);
        System.out.println(c.getNickname() + " has arrive to the lobby.");
    }

    public boolean isNicknameReserved(Client c) {
        c.sendMessage("OK");
        return false;
        // c.sendMessage("RESERVED");
        // return true;
    }
}
