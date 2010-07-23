/**
 * Copyright (c) 2010 Bálint Kriván <balint@krivan.hu>. All rights reserved.
 * Use of this source code is governed by license that can be
 * found in the LICENSE file.
 */
package hu.krivan.minesweeper.server;

import hu.krivan.minesweeper.common.Coords;
import hu.krivan.minesweeper.common.Table;

/**
 * Represents a game on the server.
 *
 * @author balint
 */
public class GamePhase extends Thread {

    private final Client p1;
    private final Client p2;
    private final Table t;
    private boolean should_run = true;
    private int PID = 1;

    public GamePhase(Client c1, Client c2) {
        super("Game");
        p1 = c1;
        p2 = c2;
        t = new Table();

        System.out.println("A game has been created: " + c1.getNickname() + " vs. " + c2.getNickname());
    }

    @Override
    public void run() {
        Coords click;

        while (should_run) {
            sendTable();
            click = getPlayer().ask(); // kérünk a playertől CLICK-et
            //System.out.println(click);
            // ha gond van a click-el, akkor E4
            if (click == null) {
                broadcast("E4");
            }
            // ha nem volt jó a click akkor E3
            if (!t.clicked(click)) {
                broadcast("E3");
            }
            // csak akkor váltunk playert, ha nem aknára kapcsoltunk
            if (!t.playerFoundMine()) {
                changePlayer();
            } else {
                getPlayer().addPoint();
            }
        }

        // elköszönünk a playerektől.
        p1.disconnect();
        p2.disconnect();
    }

    private Client getPlayer() {
        switch (PID) {
            case 1:
                return p1;
            case 2:
                return p2;
            default:
                return null; // csak nem :)
        }
    }

    private void changePlayer() {
        PID = 3 - PID; // trükkös mi? :)
    }

    private void sendTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("TABLE ").append(p1.getPoints()).append(" ").append(p2.getPoints()).append("\r\n").append(t.zip());
        String s = sb.toString();
        System.out.println("BROADCASTING: " + s);

        broadcast(s);
    }

    private void broadcast(String msg) {
        p1.sendMessage(msg);
        p2.sendMessage(msg);
    }
}
