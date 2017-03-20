package de.hindenbug.gameoflife;

/**
 * @version
 * @author Nils Verheyen
 * @since 11.03.17 22:50
 */

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import java.io.IOException;

public class UI extends Application
{

    private static final int BEING_WIDTH = 20;
    private static final int BEING_HEIGHT = 20;
    private static final Paint CANVAS_BACKGROUND = Color.web("#eeeeee");
    private static final Paint CANVAS_GRID_LINE_COLOR = new Color(.7, .7, .7, 1);
    private static final Paint BEING_COLOR = new Color(.129, .586, .949, 1);

    private final GameOfLife gameOfLife = new GameOfLife();

    private Canvas canvas;
    private Scene scene;
    private HBox controls;

    private int beingHeight = BEING_HEIGHT;
    private int beingWidth = BEING_WIDTH;

    private GameOfLifeService generator;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        primaryStage.setTitle("Game Of Life");
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);

        Label lblRows = new Label("Zellenhöhe");
        lblRows.getStyleClass().add("tf-label");
        TextField tfRows = new TextField();
        tfRows.addEventHandler(KeyEvent.KEY_TYPED, createBeingSizeTFHandler(2));
        tfRows.setOnKeyReleased(e -> beingHeight = !tfRows.getText().isEmpty()
                                                   ? Integer.valueOf(tfRows.getText())
                                                   : BEING_HEIGHT);
        tfRows.getStyleClass().add("fieldsize");
        Label lblColumns = new Label("Zellenbreite");
        lblColumns.getStyleClass().add("tf-label");
        TextField tfColumns = new TextField();
        tfColumns.addEventHandler(KeyEvent.KEY_TYPED, createBeingSizeTFHandler(2));
        tfColumns.setOnKeyReleased(e -> beingWidth = !tfColumns.getText().isEmpty()
                                                     ? Integer.valueOf(tfColumns.getText())
                                                     : BEING_WIDTH);
        tfColumns.getStyleClass().add("fieldsize");

        Button btnStart = new Button("Start");
        btnStart.setOnAction(this::startGenerator);

        Button btnStop = new Button("Stop");
        btnStop.setOnAction(this::stopGenerator);

        Button btnNextGeneration = new Button("Nächste Generation");
        btnNextGeneration.setOnAction(this::onNextGeneration);

        Button btnReset = new Button("Neustart");
        btnReset.setOnAction(this::onReset);

        controls = new HBox(5, lblRows, tfRows, lblColumns, tfColumns, btnStart, btnStop, btnNextGeneration, btnReset);
        controls.setPadding(new Insets(5));

        canvas = new Canvas();
        canvas.getStyleClass().add("canvas");

        VBox container = new VBox(5, controls, canvas);

        scene = new Scene(container, 800, 600);
        scene.getStylesheets().add("de/hindenbug/gameoflife/gameoflife.css");

        canvas.setOnMouseClicked(this::addBeing);

        resizeCanvas();
        drawField();
        drawLines();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void stopGenerator(ActionEvent event)
    {
        if (generator != null)
            generator.cancel();
    }

    private <T extends Event> void startGenerator(ActionEvent event)
    {
        if (generator == null)
        {
            generator = new GameOfLifeService(gameOfLife);
            generator.addListener(this::onGenerationGenerated);
            generator.start();
        } else
        {
            generator.restart();
        }
    }

    private <T extends Event> void onGenerationGenerated(ObservableValue observableValue,
                                                         Object oldValue,
                                                         Object newValue)
    {
        if (newValue != null)
        {
            drawField();
            drawLines();
            GameOfLife gameOfLife = (GameOfLife) newValue;
            gameOfLife.getBeings().forEach(this::drawBeing);
        }
    }

    private void onReset(ActionEvent event)
    {
        stopGenerator(event);
        gameOfLife.clear();
        resizeCanvas();
        drawField();
        drawLines();
    }

    private void onNextGeneration(ActionEvent event)
    {
        gameOfLife.generateNextGeneration();
        drawField();
        drawLines();
        gameOfLife.getBeings().forEach(this::drawBeing);
    }

    private void resizeCanvas()
    {

        double height = scene.getHeight() - controls.getHeight();
        if (canvas.getHeight() != height)
            canvas.setHeight(height);

        double width = scene.getWidth();
        if (canvas.getWidth() != width)
            canvas.setWidth(width);
    }

    private void drawLines()
    {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(CANVAS_GRID_LINE_COLOR);
        gc.setLineWidth(1);

        // see http://stackoverflow.com/questions/27846659/how-to-draw-an-1-pixel-line-using-javafx-canvas
        
        // columns
        for (double x = .5; x < canvas.getWidth(); x += beingWidth)
        {
            gc.strokeLine(x, 0, x, canvas.getHeight());
        }

        // rows
        for (double y = .5; y < canvas.getHeight(); y += beingHeight)
        {
            gc.strokeLine(0, y, canvas.getWidth(), y);
        }
    }

    private void drawField()
    {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(CANVAS_BACKGROUND);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

    }

    private void drawBeing(double x, double y, Paint color)
    {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);

        x = x - x % beingWidth + 1;
        y = y - y % beingHeight + 1;

        gc.fillRect(x, y, beingWidth - 1, beingHeight - 1);
    }

    private void drawBeing(Being being)
    {
        double y = being.getRow() * beingHeight;
        double x = being.getColumn() * beingWidth;
        drawBeing(x, y, BEING_COLOR);
    }

    private void addBeing(MouseEvent mouseEvent)
    {
        double x = mouseEvent.getX();
        double y = mouseEvent.getY();
        boolean isPresent = gameOfLife.toggleBeing((int) (y / beingHeight), (int) (x / beingWidth));
        Paint color = isPresent
                      ? BEING_COLOR
                      : CANVAS_BACKGROUND;
        drawBeing(x, y, color);
    }


    private EventHandler<KeyEvent> createBeingSizeTFHandler(final Integer maxLength)
    {
        return e ->
        {
            TextField textField = (TextField) e.getSource();
            if (textField.getText().length() >= maxLength
                    || !Character.isDigit(e.getCharacter().charAt(0)))
            {
                e.consume();
            }
        };
    }
}
