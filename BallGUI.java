import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BallGUI extends JPanel implements ActionListener {
    private class Ball {
        double x, y;
        double velocityX = 0;
        double velocityY = 0;
        boolean hasStopped = false;
        int bounceCount = 0;
        Color color;

        Ball(double startX, double startY, Color color) {
            this.x = startX;
            this.y = startY;
            this.color = color;
        }
    }

    private final int ballSize = 40;
    private final double gravity = 0.5;
    private final double minVelocityThreshold = 3.0;
    private final double pixelsToMeters = 50.0;

    private Timer timer;
    private ArrayList<Ball> balls;
    private int level = 1;

    private final Random random = new Random();

    public BallGUI() {
        setupBalls(level);
        timer = new Timer(10, this);
        timer.start();
    }

    private void setupBalls(int level) {
        balls = new ArrayList<>();
        int redBallsCount = 1 + (level / 5);  // Bola merah yang dihitung pantulannya bertambah tiap kelipatan 10 level
        int totalBalls = level;                 // Total bola sama dengan level

        for (int i = 0; i < totalBalls; i++) {
            double startX = 50 + i * (ballSize + 10);
            Color c;
            if (i < redBallsCount) {
                c = Color.RED;  // Bola merah yang dihitung pantulannya
            } else {
                c = Color.BLUE; // Bola tambahan yang tidak dihitung pantulannya
            }
            balls.add(new Ball(startX, 0, c));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Gambar tanah
        g.setColor(Color.GREEN.darker());
        g.fillRect(0, getHeight() - 10, getWidth(), 10);

        for (Ball b : balls) {
            g.setColor(b.color);
            g.fillOval((int) b.x, (int) b.y, ballSize, ballSize);
        }

        // Info level
        g.setColor(Color.BLACK);
        g.drawString("Level: " + level, 10, 20);

        // Total pantulan bola merah saja
        int totalBounces = balls.stream()
            .filter(b -> b.color.equals(Color.RED))
            .mapToInt(b -> b.bounceCount).sum();

        //g.drawString("Jumlah Pantulan Bola Merah: " + totalBounces, 10, 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean allStopped = true;
        int groundY = getHeight() - 10 - ballSize;

        for (Ball b : balls) {
            if (!b.hasStopped) {
                allStopped = false;

                b.velocityY += gravity;
                b.y += b.velocityY;
                b.x += b.velocityX;

                if (b.y >= groundY) {
                    b.y = groundY;
                    b.bounceCount++;

                    // Acak kecepatan horizontal
                    b.velocityX = random.nextDouble() * 6 - 3;

                    // Acak kecepatan vertikal pantulan antara 90% sampai 110% dari kecepatan negatif sebelumnya, dan tambah sedikit random supaya variatif
                    double baseVelocityY = -b.velocityY * (0.5 + random.nextDouble()*0.6);
                    b.velocityY = baseVelocityY;

                    // Tambahkan variasi acak ke velocityY agar lebih dinamis
                    b.velocityY += random.nextDouble() * 2 - 1; // variasi -1 sampai +1

                    if (Math.abs(b.velocityY) < minVelocityThreshold) {
                        b.velocityY = 0;
                        b.velocityX = 0;
                        b.hasStopped = true;
                    }
                }

                // Batas kiri dan kanan
                if (b.x < 0) {
                    b.x = 0;
                    b.velocityX = -b.velocityX;
                } else if (b.x > getWidth() - ballSize) {
                    b.x = getWidth() - ballSize;
                    b.velocityX = -b.velocityX;
                }
            }
        }

        if (allStopped) {
            timer.stop();

            // Hitung pantulan bola merah saja
            int totalBounces = balls.stream()
                .filter(b -> b.color.equals(Color.RED))
                .mapToInt(b -> b.bounceCount).sum();

            SwingUtilities.invokeLater(() -> {
                String input = JOptionPane.showInputDialog(null,
                        "Tebak berapa kali total pantulan bola merah (level " + level + "):");
                if (input != null) {
                    try {
                        int guess = Integer.parseInt(input);
                        if (guess == totalBounces) {
                            JOptionPane.showMessageDialog(null, "Kamu menang level " + level + "! 🎉");

                            level++;
                            setupBalls(level);
                            timer.start();
                        } else {
                            int opsi = JOptionPane.showConfirmDialog(null,
                                    "Salah. Jawabannya: " + totalBounces + ". Coba lagi?",
                                    "Coba lagi?", JOptionPane.YES_NO_OPTION);

                            if (opsi == JOptionPane.YES_OPTION) {
                                setupBalls(level);
                                timer.start();
                            } else {
                                System.exit(0);
                            }
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Masukkan angka yang valid.");
                        timer.start();
                    }
                } else {
                    System.exit(0);
                }
            });
        }

        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tebak Jumlah Pantulan Bola Berlevel");
        BallGUI panel = new BallGUI();
        frame.add(panel);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);  // <-- Tambahkan baris ini supaya frame muncul di tengah layar
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
}
