import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // Up Down Left Right
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private Image powerFoodImage;
    private Image scaredGhostImage;

    private Image cherryImage1;
    private Image cherryImage2;

    boolean paused = false;
    boolean ghostsScared = false;
    long scaredEndTime = 0;
    int scaredDuration = 8000; // milliseconds


    //X = wall, O = skip, P = pac man, ' ' = food, * = power food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X *      X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X   *   X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X     *  X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X    *            X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> powerFoods;
    HashSet<Block> ghosts;
    Block pacman;
    
    HashSet<Block> cherries;
    long lastCherrySpawnTime = 0;
    int cherrySpawnInterval = 10000; // 10 seconds
    boolean useAltCherryImage = false;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./icons/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./icons/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./icons/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./icons/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./icons/redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./icons/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./icons/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./icons/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./icons/pacmanRight.png")).getImage();

        powerFoodImage = new ImageIcon(getClass().getResource("./icons/powerFood.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("./icons/scaredGhost.png")).getImage();

        cherryImage1 = new ImageIcon(getClass().getResource("./icons/cherry.png")).getImage();
        cherryImage2 = new ImageIcon(getClass().getResource("./icons/cherry2.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();

    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        powerFoods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        cherries = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == '*') {
                    Block powerFood = new Block(powerFoodImage, x + 8, y + 8, 16, 16);
                    powerFoods.add(powerFood);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
                // nothing to do for 'O' (skip)
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        for (Block powerFood : powerFoods) {
            g.drawImage(powerFood.image, powerFood.x, powerFood.y, powerFood.width, powerFood.height, null);
        }
        for (Block cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }

        //score
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 0, 150)); // semi-transparent overlay
            g2d.fillRect(0, 0, boardWidth, boardHeight);

            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g2d.getFontMetrics();
            String gameOverText = "GAME OVER";
            int textWidth = fm.stringWidth(gameOverText);
            g2d.drawString(gameOverText, (boardWidth - textWidth) / 2, boardHeight / 2 - 20);

            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            String scoreText = "Final Score: " + score;
            int scoreWidth = g2d.getFontMetrics().stringWidth(scoreText);
            g2d.drawString(scoreText, (boardWidth - scoreWidth) / 2, boardHeight / 2 + 20);
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }

        if (paused && !gameOver) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 0, 150)); // semi-transparent black
            g2d.fillRect(0, 0, boardWidth, boardHeight);

            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            FontMetrics fm = g2d.getFontMetrics();
            String pausedText = "PAUSED";
            int textWidth = fm.stringWidth(pausedText);
            int textHeight = fm.getHeight();
            g2d.drawString(pausedText, (boardWidth - textWidth) / 2, (boardHeight + textHeight) / 2);
        }
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.setColor(Color.YELLOW);

            if (gameOver) {
                g.drawString("Press any key to restart", tileSize, boardHeight - tileSize / 2);
            } else if (paused) {
                g.drawString("Press P to resume playing", tileSize, boardHeight - tileSize / 2);
            } else {
                g.drawString("Press P to pause", tileSize, boardHeight - tileSize / 2);
            }
    }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        if (pacman.x < -pacman.width) {
            pacman.x = boardWidth;
        } else if (pacman.x > boardWidth) {
            pacman.x = -pacman.width;
        }

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        //check ghost collisions
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (ghostsScared) {
                    ghost.reset(); // Send back to starting position
                    score += 200;
                }
                else {
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
                        return;
                    }
                    resetPositions();
                    break;
                }
            }

            if (ghost.y == tileSize*9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        //check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }

        Block powerFoodEaten = null;
        for (Block powerFood : powerFoods) {
            if (collision(pacman, powerFood)) {
                powerFoodEaten = powerFood;
                score += 50;
                ghostsScared = true;
                scaredEndTime = System.currentTimeMillis() + scaredDuration;

                // Change all ghost images to scared
                for (Block ghost : ghosts) {
                    ghost.image = scaredGhostImage;
                }
            }
        }
        powerFoods.remove(powerFoodEaten);
        
        if (ghostsScared && System.currentTimeMillis() > scaredEndTime) {
            ghostsScared = false;
            // Reset ghost images to original
            for (Block ghost : ghosts) {
                if (ghost.startX == tileSize * 8) ghost.image = blueGhostImage;
                else if (ghost.startX == tileSize * 10) ghost.image = orangeGhostImage;
                else if (ghost.startX == tileSize * 9) ghost.image = pinkGhostImage;
                else if (ghost.startX == tileSize * 11) ghost.image = redGhostImage;
            }
        }

        // Animate cherry and spawn if needed
        if (System.currentTimeMillis() - lastCherrySpawnTime > cherrySpawnInterval) {
            spawnCherry();
            lastCherrySpawnTime = System.currentTimeMillis();
        }

        for (Block cherry : cherries) {
            cherry.image = useAltCherryImage ? cherryImage2 : cherryImage1;
        }
        useAltCherryImage = !useAltCherryImage; // toggle frame

        Block cherryEaten = null;
        for (Block cherry : cherries) {
            if (collision(pacman, cherry)) {
                cherryEaten = cherry;
                score += 100;
            }
        }
        cherries.remove(cherryEaten);
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!paused && !gameOver) {
            move();
            repaint();
        }

        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }
        else if (e.getKeyCode() == KeyEvent.VK_P) {
            paused = !paused;
            repaint();
            return; // Optional: prevents movement when pressing P
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }

    public void spawnCherry() {
        // Don't spawn if one already exists
        if (!cherries.isEmpty()) return;

        while (true) {
            int r = random.nextInt(rowCount);
            int c = random.nextInt(columnCount);
            char tile = tileMap[r].charAt(c);

            if (tile == ' ') { // only on empty food tiles
                int x = c * tileSize + 8;
                int y = r * tileSize + 8;
                Block cherry = new Block(cherryImage1, x, y, 16, 16);
                cherries.add(cherry);
                break;
            }
        }
    }

}