package hu.krivan.minesweeper.server;

/**
 * Ez a szál felelős azért, hogy a felhasználó bejelentkezzen
 *
 * @author balint
 */
public class LoginPhase extends Thread {

    private Client client;
    private Server server;

    public LoginPhase(Client client, Server server) {
        super("LoginPhase");
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        client.sendMessage("HI");
        String nickname = null;
        do {
            try {
                String s = client.readMessage();
                if (!s.startsWith("MYNAME ")) {
                    continue;
                }
                nickname = s.substring("MYNAME ".length());
            } catch (Exception ex) {
                server.kick(client);
                nickname = null;
                break;
            }
        } while (nameReserved(nickname));

        // nickname might be null, if we kicked the client.
        if (nickname != null) {
            client.setNickname(nickname);
            server.onClientLogined(client);
        }
    }

    private boolean nameReserved(String nickname) {
        // nickname can't be null
        if (nickname == null || nickname.equals("")) {
            return true;
        }

        if (server.isNicknameReserved(nickname)) {
            client.sendMessage("RESERVED");
            return true;
        } else {
            client.sendMessage("OK");
            return false;
        }
    }
}
