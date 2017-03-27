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
import javafx.event.EventType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

public class UI extends Application
{
    private static final Logger LOG = LoggerFactory.getLogger(UI.class);

    private static final int BEING_WIDTH = 15;
    private static final int BEING_HEIGHT = 15;
    private static final Paint CANVAS_BACKGROUND = Color.web("#eeeeee");
    private static final Paint CANVAS_GRID_LINE_COLOR = new Color(.7, .7, .7, 1);
    private static final Paint BEING_COLOR = new Color(.129, .586, .949, 1);

    private Semaphore gameOfLifeSync = new Semaphore(1);

    private final Predicate<Being> isInvisible = new Predicate<Being>()
    {
        @Override
        public boolean test(Being being)
        {
            /*
            the offset is used as additional space so no being is removed if it
            influences visible beings
             */
            int offset = 10;
            return (being.getRow() - offset) * beingHeight > canvas.getHeight()
                    || (being.getColumn() - offset) * beingWidth > canvas.getWidth()
                    || being.getRow() < 0
                    || being.getColumn() < 0;
        }
    };

    private final GameOfLife gameOfLife = GameOfLifeSample.GosperGliderGun;

    private Canvas canvas;
    private Scene scene;
    private HBox controls;

    private int beingHeight = BEING_HEIGHT;
    private int beingWidth = BEING_WIDTH;
    private int generationTimeMS = GameOfLifeService.DEFAULT_GENERATION_TIME_MS;

    private GameOfLifeService generator;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        primaryStage.setTitle("Game Of Life");
        primaryStage.setMinWidth(640);
        primaryStage.setMinHeight(480);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(800);

        Label lblRows = createLabel("Cell height");
        lblRows.getStyleClass().add("tf-label");
        TextField tfRows = new TextField();
        setTextFieldDefaults(tfRows,
                keyEvent -> beingHeight = !tfRows.getText().isEmpty()
                                          ? Integer.valueOf(tfRows.getText())
                                          : BEING_HEIGHT, Integer.toString(BEING_HEIGHT),
                KeyEvent.KEY_TYPED, createBeingSizeTFHandler(2));

        Label lblColumns = createLabel("Cell width");
        TextField tfColumns = new TextField();
        setTextFieldDefaults(tfColumns,
                keyEvent -> beingWidth = !tfColumns.getText().isEmpty()
                                         ? Integer.valueOf(tfColumns.getText())
                                         : BEING_WIDTH, Integer.toString(BEING_WIDTH),
                KeyEvent.KEY_TYPED, createBeingSizeTFHandler(2));

        Label lblTime = createLabel("Time to generate (ms)");
        TextField tfTime = new TextField();
        setTextFieldDefaults(tfTime,
                keyEvent ->
                {
                    generationTimeMS = !tfTime.getText().isEmpty()
                                       ? Integer.valueOf(tfTime.getText())
                                       : GameOfLifeService.DEFAULT_GENERATION_TIME_MS;
                    if (generator != null)
                        generator.setGenerationTime(generationTimeMS);
                },
                Integer.toString(GameOfLifeService.DEFAULT_GENERATION_TIME_MS),
                KeyEvent.KEY_TYPED, createBeingSizeTFHandler(4));

        Button btnStart = new Button("Start");
        btnStart.setOnAction(this::startGenerator);

        Button btnStop = new Button("Stop");
        btnStop.setOnAction(this::stopGenerator);

        Button btnNextGeneration = new Button("Next generation");
        btnNextGeneration.setOnAction(this::onNextGeneration);

        Button btnReset = new Button("Restart");
        btnReset.setOnAction(this::onReset);

        controls = new HBox(5, lblRows, tfRows,
                lblColumns, tfColumns,
                lblTime, tfTime,
                btnStart, btnStop, btnNextGeneration, btnReset);
        controls.setPadding(new Insets(5));

        canvas = new Canvas();
        canvas.getStyleClass().add("canvas");

        VBox container = new VBox(5, controls, canvas);

        scene = new Scene(container, primaryStage.getWidth(), primaryStage.getHeight());
        scene.getStylesheets().add("gameoflife.css");

        canvas.setOnMouseClicked(this::addBeing);

        resizeCanvas();
        drawField();
        drawLines();
        drawBeings();

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
            gameOfLifeSync = generator.getGameOfLifeSync();
            generator.setGenerationTime(generationTimeMS);
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
            try
            {
                LOG.debug("acquire");
                gameOfLifeSync.acquire();
                LOG.debug("cs");
                drawBeings();
                removeInvisibleBeings();
            } catch (InterruptedException e)
            {
                LOG.error("access to game of life interrupted");
            } finally
            {
                gameOfLifeSync.release();
                LOG.debug("released");
            }
        }
    }

    private void removeInvisibleBeings()
    {
        if (gameOfLife.getBeings().removeIf(isInvisible))
        {
            LOG.debug("invisible beings removed");
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
        drawField();
        drawLines();

        try
        {
            gameOfLifeSync.acquire();
            gameOfLife.generateNextGeneration();
            drawBeings();
            removeInvisibleBeings();
        } catch (InterruptedException e)
        {
            LOG.error("access to game of life interrupted");
        } finally
        {
            gameOfLifeSync.release();
        }
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

    private void drawBeings()
    {
        gameOfLife.getBeings().forEach(this::drawBeing);
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

    private static Label createLabel(String text)
    {
        Label label = new Label(text);
        label.getStyleClass().add("tf-label");
        return label;
    }

    private static <T extends Event> void setTextFieldDefaults(TextField textField, EventHandler<? super KeyEvent> keyReleasedHandler,
                                                               String text,
                                                               EventType<T> eventType,
                                                               EventHandler<? super T> eventHandler)
    {
        textField.setText(text);
        textField.addEventHandler(eventType, eventHandler);
        textField.setOnKeyReleased(keyReleasedHandler);
        textField.getStyleClass().add("fieldsize");
    }

    private static EventHandler<KeyEvent> createBeingSizeTFHandler(final Integer maxBeingSize)
    {
        return e ->
        {
            TextField textField = (TextField) e.getSource();
            boolean isTextGTMax = textField.getText().length() >= maxBeingSize;
            boolean isTextSelected = !textField.getSelectedText().isEmpty();
            boolean isDigit = Character.isDigit(e.getCharacter().charAt(0));

            /*
             consume event if:
             1. the text is greater than the allowed number of characters and
                no text is selected
             2. entered text is not a digit
              */
            if ((isTextGTMax && !isTextSelected)
                    || !isDigit)
            {
                e.consume();
            }
        };
    }
}
