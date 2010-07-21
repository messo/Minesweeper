package hu.krivan.minesweeper.client;

import hu.krivan.minesweeper.common.Coords;
import hu.krivan.minesweeper.common.Table;
import java.io.IOException;

/**
 *
 * @author balint
 */
public class Game extends Thread {

    private Client client;
    private boolean should_run = true;
    private MainFrame frame;
    private final ClickObserver clickObserver = new ClickObserver();

    public Game(Client c) {
        super("Game");
        this.client = c;
    }

    @Override
    public void run() {
        String s;
        String cmd;

        frame = new MainFrame() {

            @Override
            protected void onFieldClicked(int x, int y) {
                synchronized (clickObserver) {
                    clickObserver.coords = new Coords(x + 1, y + 1);
                    clickObserver.notify();
                }
            }
        };
        frame.setVisible(true);
        // alapból ne engedélyezzük, csak ha mi jövünk!
        frame.setEnabled(false);

        while (should_run) {
            try {
                s = client.is.readLine();
                // beolvasunk egy üzenetet.
                cmd = s.split(" ")[0];
                System.out.println("< " + s);
                if (cmd.equals("TABLE")) {
                    // tábla érkezik.
                    readNewTable();
                } else if (cmd.equals("ASK")) {
                    frame.setStatus("It's your turn.");
                    frame.setEnabled(true);
                    synchronized (clickObserver) {
                        clickObserver.wait();
                        client.sendMessage("CLICK " + clickObserver.coords.getX() + " " + clickObserver.coords.getY());
                    }
                    frame.setEnabled(false);
                    frame.setStatus("Waiting for your opponent...");
                }

            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void readNewTable() throws IOException {
        Table table = new Table(client.is);
        frame.updateBoard(table);
    }
}
