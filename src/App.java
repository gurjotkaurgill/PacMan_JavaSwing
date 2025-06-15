import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pac Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null); //center the window on the screen
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close the application when the window is closed

        PacMan pacmanGame = new PacMan();
        frame.add(pacmanGame);
        frame.pack(); //fit - large enough to display all components
        pacmanGame.requestFocus(); //receive keyboard inputs
        frame.setVisible(true);
    }
}