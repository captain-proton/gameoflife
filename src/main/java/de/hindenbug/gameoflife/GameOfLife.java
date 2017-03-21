package de.hindenbug.gameoflife;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>An instance of this <code>GameOfLife</code> is one possible solution to conway's game of life.</p>
 * <p>The universe of the Game of Life is an infinite two-dimensional orthogonal grid of square cells, each of
 * which is in one of two possible states, alive or dead, or "populated" or "unpopulated". Every cell interacts
 * with its eight neighbours, which are the cells that are horizontally, vertically, or diagonally adjacent.
 * At each step in time, the following transitions occur:</p>
 * <ul>
 * <li>Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.</li>
 * <li>Any live cell with two or three live neighbours lives on to the next generation.</li>
 * <li>Any live cell with more than three live neighbours dies, as if by overpopulation.</li>
 * <li>Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.</li>
 * </ul>
 *
 * @author Nils Verheyen
 * @see <a href="https://en.wikipedia.org/wiki/Conway's_Game_of_Life">Conway's Game of Life</a>
 * @since 11.03.17 16:35
 */
public class GameOfLife
{
    private final Predicate<Being> isAliveInNextGeneration = being ->
    {
        int neighborCount = getExistingNeighbors(being).size();
        return neighborCount >= 2 && neighborCount <= 3;
    };

    private final Predicate<Being> isNewInNextGeneration = being ->
    {
        int neighborCount = getExistingNeighbors(being).size();
        return neighborCount == 3;
    };

    private Set<Being> beings;

    /**
     * Creates a new empty <code>GameOfLife</code>
     */
    public GameOfLife()
    {
        beings = new HashSet<>();
    }

    /**
     * Creates a new game of life with given beings as initial set of life forms
     *
     * @param beings initial set of beings, not null
     */
    public GameOfLife(Set<Being> beings)
    {
        this.beings = new HashSet<>(beings);
    }

    /**
     * Generates the next generation according to the game of life rule set.
     */
    public void generateNextGeneration()
    {
        // filter existing beings by those who survive in the next generation
        Set<Being> survivors = beings.stream()
                .filter(isAliveInNextGeneration)
                .collect(Collectors.toSet());
        Set<Being> newOnes = beings.stream()
                // create a being for each neighbor of each existing being
                .flatMap(this::getPossibleNeighbors)
                // filter those beings that contain the required amount of existing neighbors
                .filter(isNewInNextGeneration)
                .collect(Collectors.toSet());
        beings.clear();
        beings.addAll(survivors);
        beings.addAll(newOnes);
    }

    private Set<Being> getExistingNeighbors(Being being)
    {
        return beings.stream()
                .filter(b -> b.isNeighborOf(being))
                .collect(Collectors.toSet());
    }

    private Stream<Being> getPossibleNeighbors(Being being)
    {
        return Stream.of(being.createNeighbors());
    }

    /**
     * Add a new {@linkplain Being} to the set of this game of life.
     *
     * @param row    row of the being, may be less than zero
     * @param column column of the being, may be less than zero
     */
    public void addBeing(int row, int column)
    {
        beings.add(new Being(row, column));
    }

    /**
     * Adds a new {@linkplain Being} to this game of life if none exists on given row and column, otherwise it is
     * removed.
     *
     * @param row    row of the being, may be less than zero
     * @param column column of the being, may be less than zero
     * @return <code>true</code> if the being was added, <code>false</code> otherwise
     */
    public boolean toggleBeing(int row, int column)
    {
        Optional<Being> optionalBeing = beings.stream()
                .filter(b -> b.getRow() == row && b.getColumn() == column)
                .findAny();

        if (optionalBeing.isPresent())
        {
            beings.remove(optionalBeing.get());
            return false;
        } else
        {
            beings.add(new Being(row, column));
            return true;
        }
    }

    public Set<Being> getBeings()
    {
        return beings;
    }

    public void clear()
    {
        beings.clear();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof GameOfLife)) return false;
        GameOfLife that = (GameOfLife) o;
        return Objects.equals(beings, that.beings);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(beings);
    }
}
