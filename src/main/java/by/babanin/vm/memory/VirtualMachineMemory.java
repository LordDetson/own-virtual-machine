package by.babanin.vm.memory;

import by.babanin.vm.exception.VirtualMachineException;
import by.babanin.vm.util.MaskGenerator;

/**
 * The maximum you can allocate is 16 gigabytes or 17,179,869,184 bytes
 */
public class VirtualMachineMemory {

    public static final byte CELL_SIZE = 64;

    private final long[] cells;
    private final byte instructionSize;
    private final long maxAddress;

    public VirtualMachineMemory(final int cellsAmount, final byte instructionSize) {
        if(cellsAmount <= 0) {
            throw new VirtualMachineException("Amount of memory cells cannot be less than or equal to 0");
        }
        if(instructionSize < 2 || instructionSize > CELL_SIZE) {
            throw new VirtualMachineException(
                    "Invalid instruction size: " + instructionSize + ". Instruction size range: [2," + instructionSize + "]");
        }
        this.cells = new long[cellsAmount];
        this.instructionSize = instructionSize;
        this.maxAddress = (long) cells.length * CELL_SIZE - instructionSize;
    }

    public void writeInstruction(final long address, final long instruction) {
        validateAddress(address);
        final InstructionHelper instructionHelper = getInstructionInfo(address);
        if(instructionHelper.shouldSplit()) {
            freeUp(instructionHelper.head.cellIndex, instructionHelper.head.offset, CELL_SIZE);
            write(instructionHelper.head.cellIndex, instructionHelper.transferFirstPart(instruction));
            freeUp(instructionHelper.head.cellIndex + 1, 0, instructionHelper.head.offset - (CELL_SIZE - instructionSize));
            write(instructionHelper.head.cellIndex + 1, instructionHelper.transferLastPart(instruction));
        }
        else {
            freeUp(instructionHelper.head.cellIndex, instructionHelper.head.offset, instructionHelper.head.offset + instructionSize);
            write(instructionHelper.head.cellIndex, instructionHelper.transferInstructionToBeginning(instruction));
        }
    }

    public long readInstruction(final long address) {
        validateAddress(address);
        final InstructionHelper instructionHelper = getInstructionInfo(address);
        if(instructionHelper.shouldSplit()) {
            long firstPart = getDataRange(instructionHelper.head.cellIndex, instructionHelper.head.offset, CELL_SIZE);
            long lastPart = getDataRange(instructionHelper.head.cellIndex + 1, 0, instructionHelper.head.offset - (CELL_SIZE - instructionSize));
            return instructionHelper.normalizeFirstPart(firstPart) + instructionHelper.normalizeLastPart(lastPart);
        }
        else {
            long instruction = getDataRange(instructionHelper.head.cellIndex, instructionHelper.head.offset,
                    instructionHelper.head.offset + instructionSize);
            return instructionHelper.normalizeInstruction(instruction);
        }
    }

    private void validateAddress(long address) {
        if(address < 0 || address > maxAddress) {
            throw new VirtualMachineException(
                    "Invalid address: " + Long.toHexString(address) + ". Memory range: [0," + Long.toHexString(maxAddress) + "]");
        }
    }

    private InstructionHelper getInstructionInfo(final long address) {
        return new InstructionHelper(address, CELL_SIZE, instructionSize);
    }

    private void freeUp(final int cellIndex, final int from, final int to) {
        cells[cellIndex] &= MaskGenerator.generateMask(CELL_SIZE, from, to);
    }

    private void write(final int cellIndex, final long value) {
        cells[cellIndex] += value;
    }

    private long getDataRange(final int cellIndex, final int from, final int to) {
        return cells[cellIndex] & ~MaskGenerator.generateMask(CELL_SIZE, from, to);
    }
}
