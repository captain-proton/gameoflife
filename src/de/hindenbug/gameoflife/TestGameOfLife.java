package de.hindenbug.gameoflife;

import java.util.Collection;

/**
 * @author Nils Verheyen
 * @since 11.03.17 23:40
 */
public class TestGameOfLife
{
    public static final void main(String[] args)
    {
        GameOfLife gameOfLife = new GameOfLife();

        gameOfLife.addBeing(3, 3);
        gameOfLife.addBeing(3, 4);
        gameOfLife.addBeing(3, 5);

        gameOfLife.generateNextGeneration();
        Collection<Being> beings = gameOfLife.getBeings();
        printBeings(beings);

        gameOfLife = new GameOfLife();
        gameOfLife.addBeing(1, 1);
        gameOfLife.addBeing(1, 2);
        gameOfLife.addBeing(1, 3);

        gameOfLife.generateNextGeneration();
        beings = gameOfLife.getBeings();
        printBeings(beings);

        gameOfLife = new GameOfLife();
        gameOfLife.addBeing(1, 1);
        gameOfLife.addBeing(1, 2);
        gameOfLife.addBeing(2, 1);
        gameOfLife.addBeing(2, 2);

        gameOfLife.generateNextGeneration();
        beings = gameOfLife.getBeings();
        printBeings(beings);
    }
    
    private static void printBeings(Collection<Being> beings)
    {
        System.out.println("Beings:");
        beings.forEach(System.out::println);
    }
}
