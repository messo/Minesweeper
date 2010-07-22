package hu.krivan.minesweeper.client;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.*;
import hu.krivan.minesweeper.util.SocketUtil;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;

/**
 *
 * @author balint
 */
public class Client {

    public PrintWriter os;
    public BufferedReader is;
    private Socket so;

    public static void main(String[] args) {
        try {
            final Client c = new Client();
            if( args.length != 2 ) {
                System.out.println("USAGE: host port");
                return;
            }
            c.connect(args[0], Integer.parseInt(args[1]));
            UserNameProvider unp = new UserNameProvider(null, true);
            unp.addListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        JTextField tf = (JTextField) e.getSource();
                        c.login(tf.getText());
                        ServerConnection so = new ServerConnection(c);
                        so.start();
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            unp.pack();
            unp.setLocationRelativeTo(null);
            unp.setVisible(true);
        } catch (UnknownHostException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    private void connect(String host, int port) throws UnknownHostException, IOException {
        so = new Socket(host, port);
        is = SocketUtil.getBufferedReader(so);
        os = SocketUtil.getPrintWriter(so);
    }

    private void login(String name) throws IOException {
        readMessage();
        sendMessage("MYNAME " + name);
        readMessage();
    }

    public void disconnect() {
        try {
            so.close();
        } catch(IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void sendMessage(String string) {
        if (os.checkError()) {
            System.out.println("# checkError");
        }
        os.print(string + "\r\n");
        System.out.println("> " + string);
        os.flush();
        if (os.checkError()) {
            System.out.println("# checkError");
        }
    }

    public String readMessage() throws IOException {
        String s;
        s = is.readLine();
        System.out.println("< " + s);
        return s;
    }
}
