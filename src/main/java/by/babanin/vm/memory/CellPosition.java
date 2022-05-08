package by.babanin.vm.memory;

public class CellPosition {

    public final int cellIndex;
    public final int offset;

    public CellPosition(long address, byte cellSize) {
        this((int) address / cellSize, (int) (address % cellSize));
    }

    public CellPosition(int cellIndex, int offset) {
        this.cellIndex = cellIndex;
        this.offset = offset;
    }
}
