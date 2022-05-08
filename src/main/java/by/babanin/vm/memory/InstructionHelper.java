package by.babanin.vm.memory;

public class InstructionHelper {

    public final byte instructionSize;
    private final byte cellSize;
    public final CellPosition head;
    public final CellPosition tail;

    public InstructionHelper(long address, byte cellSize, byte instructionSize) {
        this.instructionSize = instructionSize;
        this.cellSize = cellSize;
        this.head = new CellPosition(address, cellSize);
        this.tail = new CellPosition(address + instructionSize, cellSize);
    }

    public boolean shouldSplit() {
        return head.cellIndex != tail.cellIndex;
    }

    public long transferFirstPart(final long instruction) {
        return instruction >>> head.offset - (cellSize - instructionSize);
    }

    public long transferLastPart(final long instruction) {
        return instruction << cellSize - (head.offset - (cellSize - instructionSize));
    }

    public long transferInstructionToBeginning(final long instruction) {
        return instruction << cellSize - instructionSize - head.offset;
    }

    public long normalizeFirstPart(final long firstPart) {
        return firstPart << head.offset - (cellSize - instructionSize);
    }

    public long normalizeLastPart(final long lastPart) {
        return lastPart >>> cellSize - (head.offset - (cellSize - instructionSize));
    }

    public long normalizeInstruction(final long instruction) {
        return instruction >>> cellSize - instructionSize - head.offset;
    }
}
