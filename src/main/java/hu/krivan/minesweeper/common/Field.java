/**
 * Copyright (c) 2010 Bálint Kriván <balint@krivan.hu>. All rights reserved.
 * Use of this source code is governed by license that can be
 * found in the LICENSE file.
 */
package hu.krivan.minesweeper.common;

/**
 *
 * @author balint
 */
public class Field {

    private int value; // -1 -> 8 (-1 = akna)
    private boolean revealed = false; // felderített-e?
    private Coords coords;

    public Field(int x, int y, int value, boolean revealed) {
        this(x, y);
        this.value = value;
        this.revealed = revealed;
    }

    public Field(int x, int y) {
        coords = new Coords(x, y);
    }

    public int getX() {
        return coords.getX();
    }

    public int getY() {
        return coords.getY();
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void incValue() {
        value++;
    }

    public void mine() {
        value = -1;
    }

    public boolean isMine() {
        return value == -1;
    }
}
