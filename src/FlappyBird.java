import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird settings
    int birdX = boardWidth / 8;
    int birdY = boardWidth / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe settings
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;  // scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Game logic
    Bird bird;
    int velocityX = -4; // Move pipes to the left speed (simulates bird moving right)
    int velocityY = 0;  // Move bird up/down speed
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();
    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    boolean moveStart = false;  // Added moveStart here
    double score = 0;

    // Constructor
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Bird setup
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        // Pipe placement timer
        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.stop();

        // Game loop timer
        gameLoop = new Timer(1000 / 60, this); // Timer running at ~60 FPS
        gameLoop.stop();
    }

    void placePipes() {
        // Pipe placement logic
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        // Bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));

        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
            g.drawString("Press 'SPACE' to Restart", 10, 100);
            g.drawString("Press 'E' to Exit", 10, 180);
        } else if (!moveStart) {
            g.drawString(String.valueOf((int) score), 10, 35);
            g.drawString("Press 'SPACE' to Start", 10, 100);
            g.drawString("Press 'UP' to Jump", 10, 180);
            g.drawString("Press 'E' to Exit", 10, 260);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // Bird movement
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // Apply gravity, limit bird.y to the top of the canvas

        // Pipe movement
        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // Increment score for each set of pipes passed
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        // Check if the bird falls below the screen
        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&   // a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&   // a's top right corner passes b's top left corner
               a.y < b.y + b.height &&  // a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;    // a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            velocityY = -9;  // Jump
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                // Restart the game
                bird.y = birdY;
                velocityY = 0;
                pipes.clear();
                gameOver = false;
                score = 0;
                gameLoop.start();
                placePipeTimer.start();
            } else if (!moveStart) {
                // Start the game
                placePipeTimer.start();
                gameLoop.start();
                moveStart = true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            System.exit(0);
        }
    }

    // Unused methods for KeyListener
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
