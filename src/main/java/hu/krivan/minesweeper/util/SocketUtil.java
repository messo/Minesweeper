/**
 * Copyright (c) 2010 Bálint Kriván <balint@krivan.hu>. All rights reserved.
 * Use of this source code is governed by license that can be
 * found in the LICENSE file.
 */
package hu.krivan.minesweeper.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author balint
 */
public class SocketUtil {

    public static BufferedReader getBufferedReader(Socket s) throws IOException {
        return new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    public static PrintWriter getPrintWriter(Socket s) throws IOException {
        return new PrintWriter(s.getOutputStream());
    }
}
