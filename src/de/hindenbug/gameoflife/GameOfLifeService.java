package de.hindenbug.gameoflife;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nils Verheyen
 * @since 14.03.17 21:31
 */
public class GameOfLifeService extends Service<GameOfLife> implements ObservableValue<GameOfLife>
{
    private final int interval;
    private final GameOfLife gameOfLife;

    private final List<ChangeListener<GameOfLife>> changeListeners;
    private final List<InvalidationListener> invalidationListeners;

    public GameOfLifeService(GameOfLife gameOfLife)
    {
        this(gameOfLife, 200);
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
                System.out.println("game of life task called");
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
                        System.out.println("generated cancelled");
                    }
                }
                System.out.println("...finished");
                return gameOfLife;
            }
        };
    }

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
}
