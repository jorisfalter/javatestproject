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
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

public class FlightGame extends Application {
    private double x = 50; // X position
    private double y = 500; // Y position
    private double z = 0; // Z position (altitude)
    private double velocityX = 0;
    private double velocityY = 0;
    private double velocityZ = 0;
    private double rotation = 0; // Plane rotation in degrees
    private double bankAngle = 0; // Banking angle for turns
    private final double acceleration = 0.3;
    private final double friction = 0.99;
    private final double gravity = 0.2;
    private final double turnRate = 2.0;
    private boolean isAirborne = false;
    private double groundLevel = 500;
    private double takeoffSpeed = 4.0;
    private GraphicsContext gc;
    private Set<KeyCode> activeKeys = new HashSet<>();

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new StackPane(canvas));
        stage.setScene(scene);
        stage.setTitle("3D Flight Game");

        // Track both key presses and releases
        scene.setOnKeyPressed(event -> activeKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> activeKeys.remove(event.getCode()));

        new AnimationTimer() {
            public void handle(long now) {
                handleInput();
                updateGame();
                drawScene();
            }
        }.start();

        stage.show();
    }

    private void handleInput() {
        // Handle multiple simultaneous key presses
        if (activeKeys.contains(KeyCode.RIGHT)) {
            if (isAirborne) {
                bankAngle = Math.min(bankAngle + 2, 45);
                rotation += turnRate;
            }
            double thrust = acceleration * Math.cos(Math.toRadians(rotation));
            velocityX += thrust;
            velocityY += thrust * Math.sin(Math.toRadians(rotation));
        }

        if (activeKeys.contains(KeyCode.LEFT)) {
            if (isAirborne) {
                bankAngle = Math.max(bankAngle - 2, -45);
                rotation -= turnRate;
            } else {
                velocityX = Math.max(0, velocityX - acceleration);
            }
        }

        if (activeKeys.contains(KeyCode.UP)) {
            if (isAirborne || Math.abs(velocityX) >= takeoffSpeed) {
                velocityZ -= acceleration * 0.8; // Reduced vertical acceleration
                isAirborne = true;
            }
        }

        if (activeKeys.contains(KeyCode.DOWN)) {
            if (isAirborne) {
                velocityZ += acceleration * 0.8;
            }
        }

        // Auto-level when not turning
        if (!activeKeys.contains(KeyCode.LEFT) && !activeKeys.contains(KeyCode.RIGHT)) {
            bankAngle *= 0.95; // Smoother auto-leveling
        }
    }

    private void updateGame() {
        // Calculate total velocity for air resistance
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ);
        double airResistance = Math.pow(0.995, speed); // More resistance at higher speeds

        // Update position
        x += velocityX;
        y += velocityY;
        z += velocityZ;

        if (!isAirborne) {
            velocityX *= friction;
            z = 0;
            velocityZ = 0;
            bankAngle = 0;
        } else {
            // Apply air resistance
            velocityX *= airResistance;
            velocityY *= airResistance;
            velocityZ = velocityZ * airResistance + gravity;

            // Apply banking effect to movement
            double turnForce = Math.sin(Math.toRadians(bankAngle)) * 0.2;
            velocityX += turnForce * Math.sin(Math.toRadians(rotation));
            velocityY -= turnForce * Math.cos(Math.toRadians(rotation));
        }

        // Landing/Crashing
        if (z >= groundLevel && isAirborne) {
            if (velocityZ < 2.0 && Math.abs(bankAngle) < 10) {
                z = 0;
                isAirborne = false;
            } else {
                resetPlane();
            }
        }

        // Keep in bounds
        x = Math.max(-400, Math.min(x, 400));
        y = Math.max(-400, Math.min(y, 400));
        z = Math.max(0, Math.min(z, groundLevel));
    }

    private void resetPlane() {
        x = 50;
        y = 500;
        z = 0;
        velocityX = 0;
        velocityY = 0;
        velocityZ = 0;
        rotation = 0;
        bankAngle = 0;
        isAirborne = false;
    }

    private void drawScene() {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, 800, 600);

        // Draw ground grid with perspective
        drawGrid();

        // Draw plane
        drawPlane();

        // HUD
        drawHUD();
    }

    private void drawGrid() {
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(1);

        // Draw perspective grid
        int gridSize = 800;
        int spacing = 50;
        double horizon = 300;

        for (int i = -gridSize; i <= gridSize; i += spacing) {
            // Vertical lines
            double x1 = 400 + i - x;
            double x2 = 400 + (i - x) * 2;
            gc.strokeLine(x1, horizon + z, x2, 600);

            // Horizontal lines
            double y1 = horizon + z + i;
            if (y1 < 600 && y1 > horizon + z) {
                gc.strokeLine(0, y1, 800, y1);
            }
        }
    }

    private void drawPlane() {
        gc.save();

        // Transform to plane position
        gc.translate(400, 300);
        gc.rotate(bankAngle);

        // Draw plane shape
        gc.setFill(Color.RED);
        gc.fillPolygon(
                new double[] { -15, 0, 15 },
                new double[] { 15, -15, 15 },
                3);

        gc.restore();
    }

    private void drawHUD() {
        gc.setFill(Color.BLACK);
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        gc.fillText(String.format("Speed: %.1f", speed), 10, 20);
        gc.fillText(String.format("Altitude: %.0f", z), 10, 40);
        gc.fillText(String.format("Bank Angle: %.0f°", bankAngle), 10, 60);
        gc.fillText(isAirborne ? "AIRBORNE" : "ON GROUND", 10, 80);

        if (!isAirborne) {
            gc.setFill(speed >= takeoffSpeed ? Color.GREEN : Color.RED);
            gc.fillText("TAKEOFF SPEED: " + (speed >= takeoffSpeed ? "REACHED" : "NOT REACHED"), 10, 100);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}