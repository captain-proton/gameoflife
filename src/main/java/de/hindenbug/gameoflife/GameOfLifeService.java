package de.hindenbug.gameoflife;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * A <code>GameOfLifeService</code> is a javafx service capable of generating a {@linkplain GameOfLife} object.
 * To listen to generation events add change listeners with {@linkplain #addListener(ChangeListener)}.
 *
 * @author Nils Verheyen
 * @since 14.03.17 21:31
 */
public class GameOfLifeService extends Service<GameOfLife> implements ObservableValue<GameOfLife>
{
    static final int DEFAULT_GENERATION_TIME_MS = 150;

    private final GameOfLife gameOfLife;
    private final Semaphore gameOfLifeSync;
    private int generation;

    private final List<ChangeListener<GameOfLife>> changeListeners;
    private final List<InvalidationListener> invalidationListeners;

    private int interval;

    public GameOfLifeService(GameOfLife gameOfLife)
    {
        this(gameOfLife, DEFAULT_GENERATION_TIME_MS, new Semaphore(1));
    }

    public GameOfLifeService(GameOfLife gameOfLife, int interval, Semaphore gameOfLifeSync)
    {
        this.gameOfLife = gameOfLife;
        this.interval = interval;
        this.changeListeners = new ArrayList<>();
        this.invalidationListeners = new ArrayList<>();
        this.gameOfLifeSync = gameOfLifeSync;
    }

    @Override
    protected Task<GameOfLife> createTask()
    {
        return new Task<GameOfLife>()
        {
            private final Logger LOG = LoggerFactory.getLogger(GameOfLifeService.class);

            @Override
            protected GameOfLife call() throws Exception
            {
                LOG.info("game of life task called");
                while (!this.isCancelled())
                {
                    LOG.debug("not cancelled");
                    GameOfLife oldValue;
                    try
                    {
                        LOG.debug("acquire");
                        gameOfLifeSync.acquire();
                        LOG.debug("cs");
                        oldValue = new GameOfLife(gameOfLife.getBeings());
                        gameOfLife.generateNextGeneration();
                    } finally
                    {
                        gameOfLifeSync.release();
                        LOG.debug("released");
                    }
                    generation++;
                    LOG.debug("generation #" + generation + " generated");
                    Platform.runLater(() -> changeListeners.forEach(cl -> cl.changed(null, oldValue, gameOfLife)));
                    LOG.debug("listeners called");
                    try
                    {
                        LOG.debug("sleeping");
                        Thread.sleep(interval);
                        LOG.debug("woke up");
                    } catch (InterruptedException e)
                    {
                        LOG.info("generated cancelled");
                    }
                }
                LOG.info("...finished");
                return gameOfLife;
            }

            @Override
            protected void succeeded()
            {
                super.succeeded();
                LOG.debug("succeeded");
            }

            @Override
            protected void cancelled()
            {
                super.cancelled();
                LOG.debug("cancelled");
            }

            @Override
            protected void failed()
            {
                super.failed();
                LOG.debug("failed");
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

    synchronized void setGenerationTime(int millis)
    {
        if (millis <= 0)
            throw new IllegalArgumentException("millis must be greater than zero");

        this.interval = millis;
    }

    Semaphore getGameOfLifeSync()
    {
        return gameOfLifeSync;
    }
}
