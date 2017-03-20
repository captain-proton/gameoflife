package de.hindenbug.gameoflife;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Nils Verheyen
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

    public GameOfLife()
    {
        beings = new HashSet<>();
    }

    public GameOfLife(Set<Being> beings)
    {
        this.beings = new HashSet<>(beings);
    }

    public void generateNextGeneration()
    {
        Set<Being> survivors = beings.stream()
                .filter(isAliveInNextGeneration)
                .collect(Collectors.toSet());
        Set<Being> newOnes = beings.stream()
                .flatMap(this::getPossibleNeighbors)
                .filter(isNewInNextGeneration)
                .collect(Collectors.toSet());
        beings.clear();
        beings.addAll(survivors);
        beings.addAll(newOnes);
    }

    public Set<Being> getExistingNeighbors(Being being)
    {
        return beings.stream()
                .filter(b -> b.isNeighborOf(being))
                .collect(Collectors.toSet());
    }

    public Stream<Being> getPossibleNeighbors(Being being)
    {
        return Stream.of(being.createNeighbors());
    }

    public void addBeing(int row, int column)
    {
        beings.add(new Being(row, column));
    }

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
