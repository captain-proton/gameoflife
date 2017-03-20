package de.hindenbug.gameoflife;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Nils Verheyen
 * @since 11.03.17 23:40
 */
public class TestGameOfLife
{
    @Test
    public void testBlinker()
    {
        GameOfLife gameOfLife = new GameOfLife();

        gameOfLife.addBeing(3, 3);
        gameOfLife.addBeing(3, 4);
        gameOfLife.addBeing(3, 5);

        Set<Being> initialBeings = new HashSet<>(gameOfLife.getBeings());

        gameOfLife.generateNextGeneration();

        Set<Being> beings = gameOfLife.getBeings();
        Assert.assertEquals(beings.size(), 3);

        Set<Being> expected = beings(new Being(2, 4),
                new Being(3, 4),
                new Being(4, 4));
        Assert.assertEquals(beings, expected);

        gameOfLife.generateNextGeneration();

        expected = beings(new Being(3, 3),
                new Being(3, 4),
                new Being(3, 5));
        Assert.assertEquals(initialBeings, expected);
    }

    private static Set<Being> beings(Being... beings)
    {
        return Stream.of(beings)
                .collect(Collectors.toSet());
    }
}
