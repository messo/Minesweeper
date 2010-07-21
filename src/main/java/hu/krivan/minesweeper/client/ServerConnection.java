package hu.krivan.minesweeper.client;

import java.io.IOException;

/**
 *
 * @author balint
 */
public class ServerConnection extends Thread {

    private boolean should_run = true;
    private Client c;
    private PlayerList pl;

    public ServerConnection(Client c) {
        super("ServerConnection");
        this.c = c;
    }

    @Override
    public void run() {
        // itt az a feladatunk, hogy kiválasszuk, hogy kivel szeretnénk játszani.

        String s, cmd, arg;
        String[] tokens;
        pl = new PlayerList();
        pl.pack();
        pl.setLocationRelativeTo(null);
        pl.setVisible(true);
        pl.so = this;

        while (should_run) {
            try {
                s = c.is.readLine();
                tokens = s.split(" ");
                cmd = tokens[0];

                System.out.println("< " + s);

                if (cmd.equals("WANNAPLAY")) {
                    arg = tokens[1];
                    pl.challengeQuestion(arg);
                } else if (cmd.equals("+PLAYERLIST")) {
                    s = c.is.readLine();
                    int m = Integer.parseInt(s);
                    String[] players = new String[m];
                    for (int i = 0; i < m; i++) {
                        s = c.is.readLine();
                        players[i] = s;
                    }
                    pl.players = players;
                    pl.refreshList();
                } else if (cmd.equals("REFUSED")) {
                    pl.challengeRefused();
                } else if (cmd.equals("ACCEPTED")) {
                    startGame();
                }

            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    public void getPlayerList() {
        c.sendMessage("PLAYERLIST");
    }

    public void challengePlayer(int selectedIndex) {
        c.sendMessage("PLAY " + selectedIndex);
    }

    void acceptChallenge() {
        c.sendMessage("ACCEPT");
        startGame();
    }

    void refuseChallenge() {
        c.sendMessage("REFUSE");
    }

    void startGame() {
        should_run = false;
        pl.dispose();
        //c.sendMessage("CLICK 2 2");
        new Game(c).start();
    }
}
