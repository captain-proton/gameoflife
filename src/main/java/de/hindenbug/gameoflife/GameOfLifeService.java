package de.hindenbug.gameoflife;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>GameOfLifeService</code> is a javafx service capable of generating a {@linkplain GameOfLife} object.
 * To listen to generation events add change listeners with {@linkplain #addListener(ChangeListener)}.
 *
 * @author Nils Verheyen
 * @since 14.03.17 21:31
 */
public class GameOfLifeService extends Service<GameOfLife> implements ObservableValue<GameOfLife>
{
    public static final int DEFAULT_GENERATION_TIME_MS = 200;

    private static final Logger LOG = LoggerFactory.getLogger(GameOfLifeService.class);

    private final GameOfLife gameOfLife;

    private final List<ChangeListener<GameOfLife>> changeListeners;
    private final List<InvalidationListener> invalidationListeners;

    private int interval;

    public GameOfLifeService(GameOfLife gameOfLife)
    {
        this(gameOfLife, DEFAULT_GENERATION_TIME_MS);
    }

    public GameOfLifeService(GameOfLife gameOfLife, int interval)
    {
        this.gameOfLife = gameOfLife;
        this.interval = interval;
        this.changeListeners = new ArrayList<>();
        this.invalidationListeners = new ArrayList<>();
    }

    @Override
    protected Task<GameOfLife> createTask()
    {
        return new Task<GameOfLife>()
        {
            @Override
            protected GameOfLife call() throws Exception
            {
                LOG.info("game of life task called");
                while (!this.isCancelled())
                {
                    GameOfLife oldValue = new GameOfLife(gameOfLife.getBeings());
                    gameOfLife.generateNextGeneration();
                    changeListeners.forEach(cl -> cl.changed(null, oldValue, gameOfLife));
                    try
                    {
                        Thread.sleep(interval);
                    } catch (InterruptedException e)
                    {
                        LOG.info("generated cancelled");
                    }
                }
                LOG.info("...finished");
                return gameOfLife;
            }
        };
    }

    /**
     * Adds a new listener that will be informed, after a new generation was created in this {@linkplain #gameOfLife}.
     * The thrown event will consist of the old game of life and the new one. No observable value is given!
     *
     * @param changeListener contains the listener that will be informed, not null
     */
    @Override
    public void addListener(ChangeListener changeListener)
    {
        this.changeListeners.add(changeListener);
    }

    @Override
    public void removeListener(ChangeListener changeListener)
    {
        this.changeListeners.remove(changeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener)
    {
        this.invalidationListeners.add(invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener)
    {
        this.invalidationListeners.remove(invalidationListener);
    }

    public synchronized void setGenerationTime(int millis)
    {
        if (millis <= 0)
            throw new IllegalArgumentException("millis must be greater than zero");

        this.interval = millis;
    }
}
