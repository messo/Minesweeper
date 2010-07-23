/**
 * Copyright (c) 2010 Bálint Kriván <balint@krivan.hu>. All rights reserved.
 * Use of this source code is governed by license that can be
 * found in the LICENSE file.
 */
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
    private boolean shouldRun = true;
    private ServerSocket sso;
    private ArrayList<Client> lobby;
    private ArrayList<Client> unknowns;
    private ArrayList<GamePhase> playingRooms;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else {
            System.out.println("No port was given, fallback to the default: " + DEFAULT_PORT);
        }

        new Server(port).start();
    }

    public Server(int port) {
        this.port = port;
        lobby = new ArrayList<Client>();
        unknowns = new ArrayList<Client>();
        playingRooms = new ArrayList<GamePhase>();
    }

    @Override
    public void run() {
        try {
            sso = new ServerSocket(port, 10);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            return;
        }
        System.out.println("The server has been started. It's listening on port " + port);

        // gyűjtjük az embereket.
        while (shouldRun) {
            try {
                Socket cso = sso.accept();
                Client c = new Client(cso, this);
                unknowns.add(c); // hozzáadjuk a klienst a kliens listához.
                new LoginPhase(c, this).start();
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void onClientWantsToPlayWith(Client player, int target) {
        // szeretnénk játszani a #target ID-jű játékossal. Lehet, hogy már kilépett,
        // vagy más van helyette nem probléma.

        Client opponent = lobby.get(target);

        // player vs. opponent
        opponent.sendPlayRequest(player);
    }

    /**
     * Amikor a kliens bejelentkezett, akkor áttesszük a lobbyba.
     *
     * @param c
     */
    public synchronized void onClientLogined(Client c) {
        unknowns.remove(c);
        lobby.add(c);
        System.out.println(c.getNickname() + " has arrived to the lobby.");
        new LobbyPhase(c, this).start();
    }

    public ArrayList<Client> getLobby() {
        return (ArrayList<Client>) lobby.clone();
    }

    public boolean isNicknameReserved(String nickname) {
        // TODO implement this
        return false;
    }

    public void startGame(Client player1, Client player2) {
        // remove them from the lobby.
        lobby.remove(player1);
        lobby.remove(player2);

        // let's start the game:
        GamePhase g = new GamePhase(player1, player2);
        playingRooms.add(g);
        g.start();
    }

    public void kick(Client c) {
        // mindkét helyen lehet, töröljük mindenhonnan
        unknowns.remove(c);
        lobby.remove(c);
        c.disconnect();
    }
}
