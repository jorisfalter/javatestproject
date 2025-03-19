package game;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FlightGame extends Application {
    private double x = 50; // Starting X position on runway
    private double y = 500; // Starting Y position on runway
    private double velocityX = 0;
    private double velocityY = 0;
    private final double acceleration = 0.3;
    private final double friction = 0.99;
    private final double gravity = 0.2;
    private boolean isAirborne = false;
    private double groundLevel = 500; // Y position of the runway
    private double takeoffSpeed = 4.0; // Minimum speed needed for takeoff
    private GraphicsContext gc;

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("Simple Flight Game");

        // Keyboard controls with smooth movement
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case RIGHT:
                    velocityX += acceleration;
                    break;
                case LEFT:
                    if (isAirborne) {
                        velocityX -= acceleration;
                    } else {
                        velocityX = Math.max(0, velocityX - acceleration);
                    }
                    break;
                case UP:
                    if (isAirborne || Math.abs(velocityX) >= takeoffSpeed) {
                        velocityY -= acceleration;
                        isAirborne = true;
                    }
                    break;
                case DOWN:
                    if (isAirborne) {
                        velocityY += acceleration;
                    }
                    break;
                case ESCAPE:
                    stage.close();
                    break;
            }
        });

        // Animation loop
        new AnimationTimer() {
            public void handle(long now) {
                updateGame();
                drawScene();
            }
        }.start();

        stage.show();
    }

    private void updateGame() {
        // Apply velocity
        x += velocityX;
        y += velocityY;

        // Apply friction and gravity
        if (!isAirborne) {
            velocityX *= friction; // More friction on ground
            y = groundLevel; // Stay on ground
            velocityY = 0;
        } else {
            velocityX *= 0.995; // Less friction in air
            velocityY += gravity; // Apply gravity
        }

        // Check for landing/crashing
        if (y >= groundLevel && isAirborne) {
            if (velocityY < 2.0) { // Safe landing speed
                y = groundLevel;
                isAirborne = false;
            } else {
                // Crash! Reset position
                x = 50;
                y = groundLevel;
                velocityX = 0;
                velocityY = 0;
                isAirborne = false;
            }
        }

        // Keep plane within bounds
        x = Math.max(0, Math.min(x, 770));
        y = Math.max(0, Math.min(y, groundLevel));
    }

    private void drawScene() {
        // Draw sky
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, 800, 600);

        // Draw clouds
        gc.setFill(Color.WHITE);
        drawCloud(100, 100);
        drawCloud(300, 200);
        drawCloud(600, 150);

        // Draw ground
        gc.setFill(Color.GREEN);
        gc.fillRect(0, groundLevel + 30, 800, 70);

        // Draw runway
        gc.setFill(Color.GRAY);
        gc.fillRect(0, groundLevel, 800, 30);

        // Draw runway markings
        gc.setFill(Color.WHITE);
        for (int i = 0; i < 20; i++) {
            gc.fillRect(i * 40, groundLevel + 13, 20, 4);
        }

        // Draw plane
        gc.setFill(Color.RED);
        gc.fillOval(x, y, 30, 30);

        // Draw HUD
        gc.setFill(Color.BLACK);
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        gc.fillText(String.format("Speed: %.1f", speed), 10, 20);
        gc.fillText(String.format("Altitude: %.0f", groundLevel - y), 10, 40);
        gc.fillText(isAirborne ? "AIRBORNE" : "ON GROUND", 10, 60);

        // Draw takeoff speed indicator
        if (!isAirborne) {
            gc.setFill(speed >= takeoffSpeed ? Color.GREEN : Color.RED);
            gc.fillText("TAKEOFF SPEED: " + (speed >= takeoffSpeed ? "REACHED" : "NOT REACHED"), 10, 80);
        }
    }

    private void drawCloud(double x, double y) {
        gc.fillOval(x, y, 40, 30);
        gc.fillOval(x + 20, y - 10, 40, 30);
        gc.fillOval(x + 40, y, 40, 30);
    }

    public static void main(String[] args) {
        launch();
    }
}