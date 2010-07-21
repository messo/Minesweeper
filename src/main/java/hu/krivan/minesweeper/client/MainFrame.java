package hu.krivan.minesweeper.client;

import hu.krivan.minesweeper.common.Table;
import javax.swing.JFrame;

/**
 *
 * @author  balint
 */
public abstract class MainFrame extends JFrame {
    private final Board board;

    /** Creates new form MainFrame */
    public MainFrame() {
        initComponents();
        jPanel1.add(board = createBoard());
        pack();
    }

    public void updateBoard(Table table) {
        board.onUpdatedTable(table);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        statusBar = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setVerifyInputWhenFocusTarget(false);
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        statusBar.setText("jLabel1");
        getContentPane().add(statusBar, java.awt.BorderLayout.PAGE_END);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * @param args the command line arguments
     */
    /*public static void main(String args[]) {
    java.awt.EventQueue.invokeLater(new Runnable() {
    public void run() {
    new MainFrame().setVisible(true);
    }
    });
    }*/
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel statusBar;
    // End of variables declaration//GEN-END:variables

    private Board createBoard() {
        return new Board() {

            @Override
            protected void onFieldClicked(int x, int y) {
                MainFrame.this.onFieldClicked(x, y);
            }
        };
    }

    public void setStatus(String text) {
        statusBar.setText(text);
    }

    protected abstract void onFieldClicked(int x, int y);
}