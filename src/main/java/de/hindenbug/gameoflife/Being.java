package de.hindenbug.gameoflife;

import java.util.Objects;

/**
 * A <code>Being</code> represents a life form in conways game of life. It exists in one specific row and column and
 * has up to eight neighbors, from top left to bottom right.
 *
 * @author Nils Verheyen
 * @since 11.03.17 16:33
 */
public class Being
{
    private int row;
    private int column;

    public Being(int row, int column)
    {
        this.row = row;
        this.column = column;
    }

    /**
     * Returns <code>true</code> if this being is the neighbor of the other one. A being is the neighbor of another
     * being if it exists "around" it.
     * <pre>
     *     - - - - -
     *     - X X X -
     *     - X O X -
     *     - X X X -
     *     - - - - -
     * </pre>
     * The <code>O</code> represents this being, all <code>X</code> are neighbors.
     *
     * @param other Possible neighbor of this being.
     * @return <code>true</code> is the other one is a being
     */
    public boolean isNeighborOf(Being other)
    {
        return other.row >= row - 1
                && other.row <= row + 1
                && other.column >= column - 1
                && other.column <= column + 1
                && !(other.row == row && other.column == column);
    }

    public Being[] createNeighbors()
    {
        Being[] result = new Being[8];
        int index = 0;
        for (int i = row - 1; i <= row + 1; i++)
        {
            for (int j = column - 1; j <= column + 1; j++)
            {
                if (!(i == row && j == column))
                {
                    result[index] = new Being(i, j);
                    index++;
                }
            }
        }
        return result;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Being)) return false;
        Being being = (Being) o;
        return row == being.row &&
                column == being.column;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(row, column);
    }

    @Override
    public String toString()
    {
        return "Being{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
