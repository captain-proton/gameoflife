package de.hindenbug.gameoflife;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by nils on 20.03.17.
 */
public class GameOfLifeSample
{
    public static final GameOfLife GosperGliderGun = new GameOfLife(
            Stream.of(
                    new Being(5, 1),
                    new Being(6, 1),
                    new Being(5, 2),
                    new Being(6, 2),

                    new Being(5, 11),
                    new Being(6, 11),
                    new Being(7, 11),
                    new Being(4, 12),
                    new Being(8, 12),
                    new Being(3, 13),
                    new Being(9, 13),
                    new Being(3, 14),
                    new Being(9, 14),
                    new Being(6, 15),
                    new Being(4, 16),
                    new Being(8, 16),
                    new Being(5, 17),
                    new Being(6, 17),
                    new Being(7, 17),
                    new Being(6, 18),

                    new Being(5, 21),
                    new Being(5, 22),
                    new Being(4, 21),
                    new Being(4, 22),
                    new Being(3, 21),
                    new Being(3, 22),
                    new Being(2, 23),
                    new Being(6, 23),
                    new Being(6, 25),
                    new Being(7, 25),
                    new Being(1, 25),
                    new Being(2, 25),

                    new Being(3, 35),
                    new Being(4, 35),
                    new Being(3, 36),
                    new Being(4, 36)

            ).collect(Collectors.toSet())
    );
}
