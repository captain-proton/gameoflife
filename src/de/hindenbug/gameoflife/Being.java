package de.hindenbug.gameoflife;

import java.util.Objects;

/**
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

    public void setRow(int row)
    {
        this.row = row;
    }

    public int getColumn()
    {
        return column;
    }

    public void setColumn(int column)
    {
        this.column = column;
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
