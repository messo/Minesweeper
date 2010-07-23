package hu.krivan.minesweeper.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author balint
 */
public class Table {

    private Field[][] table = new Field[21][21];
    private boolean lastIsMine = false;

    public Field[][] getFields() {
        return table;
    }

    public Table() {
        this(true);
    }

    public Table(boolean shouldInit) {
        for (int b = 0; b < 21; b++) {
            for (int a = 0; a < 21; a++) {
                table[a][b] = new Field(a, b);
            }
        }

        if (shouldInit) {
            init();
        }
    }

    public Table(BufferedReader reader) throws IOException {
        String s;
        int value;
        boolean revealed;
        for (int i = 0; i < 21; i++) {
            s = reader.readLine();
            for (int m = 0; m < s.length(); m++) {
                if (s.charAt(m) == '?') {
                    value = -2;
                    revealed = false;
                } else if (s.charAt(m) == '*') {
                    value = -1;
                    revealed = true;
                } else {
                    value = s.charAt(m) - '0';
                    revealed = true;
                }
                table[m][i] = new Field(m, i, value, revealed);
            }
        }
    }

    public final void init() {
        int[] t = new int[21 * 21];
        for (int i = 0; i < t.length; i++) {
            t[i] = i;
        }

        int aknak = 0;
        int N = 21 * 21;
        int idx;
        Random r = new Random();
        while (aknak < 50) {
            idx = r.nextInt(N);
            if (placeMine(idx % 21, (idx - idx % 21) / 21)) {
                aknak++;
            }
        }
    }

    public String zip() {
        StringBuilder sb = new StringBuilder();
        for (int b = 0; b < 21; b++) {
            for (int a = 0; a < 21; a++) {
                if (table[a][b].isRevealed()) {
                    if (table[a][b].getValue() == -1) {
                        sb.append("*");
                    } else {
                        sb.append(table[a][b].getValue());
                    }
                } else {
                    sb.append("?");
                }
            }
            if (b != 20) {
                sb.append("\r\n");
            }
        }

        return sb.toString();
    }

    public boolean clicked(Coords click) {
        int x = click.getX();
        int y = click.getY();

        if (x >= 0 && y >= 0 && x < 21 && y < 21) {
            if (table[x][y].isRevealed()) {
                return false; // már felfedezett
            }
            if (table[x][y].getValue() == -1) {
                lastIsMine = true;
            } else {
                lastIsMine = false;
            }

            reveal(x, y);
        } else {
            return false;
        }

        return true;
    }

    private void mineNeighbourhood(int x, int y) {
        for (int x0 = x - 1; x0 <= x + 1; x0++) {
            for (int y0 = y - 1; y0 <= y + 1; y0++) {
                if (x0 >= 0 && y0 >= 0 && x0 < 21 && y0 < 21) {
                    if (!table[x0][y0].isMine()) {
                        table[x0][y0].incValue();
                    }
                }
            }
        }
    }

    private boolean placeMine(int x, int y) {
        if (!table[x][y].isMine()) {
            table[x][y].mine();
            mineNeighbourhood(x, y);
            return true;
        } else {
            return false;
        }
    }

    private void reveal(int x, int y) {
        // floodfill:
        ArrayList<Field> sor = new ArrayList<Field>();
        sor.add(table[x][y]);
        table[x][y].setRevealed(true);
        Field m;
        while (!sor.isEmpty()) {
            // kivesszük az elsőt.
            m = sor.remove(0);
            if (m.getValue() == 0) {
                // berakjuk a szomszédokat, de csak azokat amik még nem felfedezettek
                for (int a = m.getX() - 1; a <= m.getX() + 1; a++) {
                    for (int b = m.getY() - 1; b <= m.getY() + 1; b++) {
                        if (a >= 0 && b >= 0 && a < 21 && b < 21) {
                            if (!table[a][b].isRevealed()) {
                                sor.add(table[a][b]);
                                table[a][b].setRevealed(true);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean playerFoundMine() {
        return lastIsMine;
    }
}
