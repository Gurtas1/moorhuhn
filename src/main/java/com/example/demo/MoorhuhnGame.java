package com.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Insets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * The {@code MoorhuhnGame} class represents the main application for the Moorhuhn Game.
 *
 * <p>This class extends the {@code Application} class and serves as the entry point
 * for the JavaFX application. It initializes the game state, sets up the primary stage,
 * and manages the game logic including the menu layout, game start, reloading, shooting,
 * and the end game screen.</p>
 *
 * <p>The game involves shooting chickens that appear on the screen within a limited time frame.
 * The player has a limited amount of ammunition and needs to reload when it runs out.
 * The score is calculated based on the size and type of chicken hit (flying or static).</p>
 *
 * <p>The main components of the game include:</p>
 * <ul>
 * <li>Score label to display the player's score</li>
 * <li>Timer label to display the remaining game time</li>
 * <li>Ammo label to display the remaining ammunition</li>
 * <li>Crosshair to aim and shoot</li>
 * <li>Static and flying chickens appearing randomly</li>
 * <li>End game screen displaying the final score and options to play again or quit</li>
 * </ul>
 */
public class MoorhuhnGame extends Application {

    private int score;
    private int timeLeft;
    private int ammo;
    private boolean isReloading = false;
    private boolean endScreen;
    private Random random;

    private Label scoreLabel;
    private Label timerLabel;
    private Label ammoLabel;
    private Timeline gameTimeline;
    private ImageView crosshairView;
    private Pane gameLayout;
    private List<Chicken> chickens;

    /**
     * Default constructor for the {@code MoorhuhnGame} class.
     */
    public MoorhuhnGame() {
        // Default constructor
    }

    /**
     * Starts the primary stage for the Moorhuhn Game application.
     *
     * <p>This method sets up the initial menu layout for the game, including
     * the "Play" and "Quit" buttons. The "Play" button starts the game,
     * and the "Quit" button closes the application. The layout is styled using
     * a CSS stylesheet and includes a crosshair cursor for the scene.</p>
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        Button playButton = new Button("Play");
        Button quitButton = new Button("Quit");

        // Add CSS classes to buttons
        playButton.getStyleClass().add("button-play");
        quitButton.getStyleClass().add("button-quit");

        playButton.setOnAction(e -> startGame(primaryStage));
        quitButton.setOnAction(e -> primaryStage.close());

        BorderPane menuLayout = new BorderPane();

        VBox centerBox = new VBox(playButton);
        centerBox.setAlignment(Pos.CENTER);

        HBox bottomBox = new HBox(quitButton);
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.setPadding(new Insets(10));

        menuLayout.setCenter(centerBox);
        menuLayout.setBottom(bottomBox);
        menuLayout.setCursor(Cursor.CROSSHAIR);

        Scene menuScene = new Scene(menuLayout, 700, 600);
        menuScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(menuScene);
        primaryStage.setTitle("Moorhuhn Game");
        primaryStage.show();
    }

    private void startGame(Stage primaryStage) {
        random = new Random();
        chickens = new ArrayList<>();
        gameLayout = new Pane();
        gameLayout.setPrefSize(1300, 720);
        ammo = 10;
        score = 0;
        timeLeft = 30;
        endScreen = false;

        // Background
        Image backgroundImage = new Image("background.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(1351);
        backgroundView.setFitHeight(768);
        backgroundView.setPreserveRatio(true);
        backgroundView.setSmooth(true);
        backgroundView.setCache(true);
        gameLayout.getChildren().add(backgroundView);

        // Crosshair
        Image crosshairImage = new Image("crosshair.png");
        crosshairView = new ImageView(crosshairImage);
        crosshairView.setMouseTransparent(true);
        setCrosshairSize(50);
        gameLayout.getChildren().add(crosshairView);
        crosshairView.toFront();

        // Score label
        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(new Font(35));
        scoreLabel.getStyleClass().add("labels");
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(10);
        gameLayout.getChildren().add(scoreLabel);

        // Timer label
        timerLabel = new Label("Time: 60");
        timerLabel.setFont(new Font(35));
        timerLabel.getStyleClass().add("labels");
        timerLabel.setLayoutX(300);
        timerLabel.setLayoutY(10);
        gameLayout.getChildren().add(timerLabel);

        // Ammo label
        ammoLabel = new Label("Ammo: 10");
        ammoLabel.setFont(new Font(35));
        ammoLabel.getStyleClass().add("labels");
        ammoLabel.setLayoutX(1100);
        ammoLabel.setLayoutY(660);
        gameLayout.getChildren().add(ammoLabel);

        // Set up mouse movement to update crosshair position
        gameLayout.setOnMouseMoved(event -> {
            crosshairView.setX(event.getX() - crosshairView.getFitWidth() / 2);
            crosshairView.setY(event.getY() - crosshairView.getFitHeight() / 2);
        });

        // Set up mouse click to handle shooting
        gameLayout.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && ammo > 0 && !isReloading && !endScreen) {
                handleShooting(event.getX(), event.getY());
            }
        });

        // Set up key press to handle reloading
        gameLayout.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.R && !isReloading  && !endScreen) {
                handleReloading();
            }
        });

        // Set up game timer
        gameTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int numberOfChickensToAdd = random.nextInt(3);
            addStaticChickens(numberOfChickensToAdd);
            if (timeLeft % 2 == 0) {
                addFlyingChicken();
            }
            timeLeft--;
            timerLabel.setText("Time: " + timeLeft);
            if (timeLeft <= 0) {
                endGame(primaryStage);
            }
        }));
        gameTimeline.setCycleCount(Timeline.INDEFINITE);
        gameTimeline.play();

        Scene gameScene = new Scene(gameLayout, 1300, 720);
        gameScene.setCursor(Cursor.NONE);
        primaryStage.setScene(gameScene);
        gameScene.setOnKeyPressed(this::handleKeyPress);
        gameScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    }


    private void handleReloading() {
        isReloading = true;
        ammoLabel.setText("Ammo: Reloading...");
        ammoLabel.setLayoutX(950);
        Timeline reloadTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            ammo = 10;
            ammoLabel.setText("Ammo: " + ammo);
            ammoLabel.setLayoutX(1100);
            isReloading = false;
        }));
        reloadTimeline.setCycleCount(1);
        reloadTimeline.play();
    }

    private void setCrosshairSize(double size) {
        crosshairView.setFitWidth(size);
        crosshairView.setFitHeight(size);
    }

    private void handleShooting(double x, double y) {
        ammo--;
        ammoLabel.setText("Ammo: " + ammo);

        // Check for hit detection
        Iterator<Chicken> iterator = chickens.iterator();
        while (iterator.hasNext()) {
            Chicken chicken = iterator.next();
            if (chicken.imageView.getBoundsInParent().contains(x, y)) {
                score += calculateScore(chicken);
                scoreLabel.setText("Score: " + score);
                gameLayout.getChildren().remove(chicken.imageView);
                iterator.remove();
                break;
            }
        }
    }

    private int calculateScore(Chicken chicken) {
        double size = chicken.imageView.getFitWidth();
        int baseScore = (int) (2000 / size);
        if (chicken.isFlying) {
            baseScore *= 2;
        }
        return baseScore;
    }

    private void endGame(Stage primaryStage) {
        gameTimeline.stop();
        endScreen = true;

        if (crosshairView != null) {
            gameLayout.getChildren().remove(crosshairView);
        }

        Pane overlay = new Pane();
        overlay.getStyleClass().add("end-screen-layout");
        overlay.setPrefSize(gameLayout.getWidth(), gameLayout.getHeight());

        VBox endGameLayout = new VBox(20);
        endGameLayout.setAlignment(Pos.CENTER);
        endGameLayout.getStyleClass().add("end-screen");

        Label endGameScoreLabel = new Label("Final Score: " + score);
        endGameScoreLabel.getStyleClass().add("label-final");

        Button playAgainButton = new Button("Play Again");
        playAgainButton.getStyleClass().add("end-screen-button");
        playAgainButton.setOnAction(e -> startGame(primaryStage));

        Button quitButton = new Button("Quit");
        quitButton.getStyleClass().add("end-screen-button");
        quitButton.setOnAction(e -> primaryStage.close());

        endGameLayout.getChildren().addAll(endGameScoreLabel, playAgainButton, quitButton);

        overlay.getChildren().add(endGameLayout);
        endGameLayout.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            endGameLayout.setLayoutX((overlay.getWidth() - newValue.getWidth()) / 2);
            endGameLayout.setLayoutY((overlay.getHeight() - newValue.getHeight()) / 2);
        });

        gameLayout.getChildren().add(overlay);
        gameLayout.setCursor(Cursor.CROSSHAIR);

        Scene scene = primaryStage.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
    }


    private void addStaticChickens(int count) {
        Image chickenImage1 = new Image("chicken1.png");
        Image chickenImage2 = new Image("chicken2.png");
        Image chickenImage3 = new Image("chicken3.png");
        for (int i = 0; i < count; i++) {
            ImageView chickenView = new ImageView(randomImage(chickenImage1, chickenImage2, chickenImage3));
            double size = 50 + random.nextDouble() * 50; // Random size between 50 and 100
            chickenView.setFitWidth(size);
            chickenView.setFitHeight(size);
            double x = random.nextDouble() * (gameLayout.getPrefWidth() - chickenView.getFitWidth());
            double y = random.nextDouble() * (gameLayout.getPrefHeight() - 3*chickenView.getFitHeight() - 150) + 400; // Keep chickens on the ground
            chickenView.setX(x);
            chickenView.setY(y);
            chickens.add(new Chicken(chickenView, false));
            gameLayout.getChildren().add(chickenView);
        }
        crosshairView.toFront();
    }


    private void addFlyingChicken() {
        Image chickenImageLeft = new Image("chicken_flying_left.png");
        Image chickenImageRight = new Image("chicken_flying_right.png");
        boolean fromLeft = random.nextBoolean();

        ImageView chickenView = fromLeft ? new ImageView(chickenImageRight) : new ImageView(chickenImageLeft);
        double size = 50 + random.nextDouble() * 50; // Random size between 50 and 100
        chickenView.setFitWidth(size);
        chickenView.setFitHeight(size);

        double startY = random.nextDouble() * (gameLayout.getPrefHeight() - 200);
        double startX = fromLeft ? -chickenView.getFitWidth() : gameLayout.getPrefWidth();
        double endX = fromLeft ? gameLayout.getPrefWidth() + chickenView.getFitWidth() : -chickenView.getFitWidth();

        chickenView.setLayoutX(startX);
        chickenView.setLayoutY(startY);

        chickens.add(new Chicken(chickenView, true));
        gameLayout.getChildren().add(chickenView);

        TranslateTransition transition = new TranslateTransition(Duration.seconds(7), chickenView);
        transition.setFromX(0);
        transition.setToX(endX - startX);
        transition.setOnFinished(e -> {
            gameLayout.getChildren().remove(chickenView);
            chickens.remove(chickenView);
        });
        transition.play();

        crosshairView.toFront();
    }


    private Image randomImage(Image... images) {
        return images[random.nextInt(images.length)];
    }


    /**
     * The main method serves as the entry point for the Java application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private static class Chicken {
        ImageView imageView;
        boolean isFlying;
        Chicken(ImageView imageView, boolean isFlying) {
            this.imageView = imageView;
            this.isFlying = isFlying;
        }
    }

    private void handleKeyPress(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == KeyCode.R && !isReloading) {
            handleReloading();
        }
    }
}
