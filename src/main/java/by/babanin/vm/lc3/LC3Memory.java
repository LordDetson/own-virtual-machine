package by.babanin.vm.lc3;

import by.babanin.vm.VirtualMachineMemory;
import by.babanin.vm.exception.VirtualMachineException;

/**
 * Memory has 65536 cells. Each cell is 2 bytes or 16 bits.
 * Memory size is 131072 bytes or 128 KB.
 */
public class LC3Memory implements VirtualMachineMemory {

    private static final int MAX_ADDRESS = 1 << 16;
    private final short[] memory = new short[MAX_ADDRESS];

    @Override
    public void writeInstruction(long address, long instruction) {
        validateAddress(address);
        memory[(int) address] = (short) (instruction & 0xFFFF);
    }

    @Override
    public long readInstruction(long address) {
        validateAddress(address);
        return memory[(int) address];
    }

    private void validateAddress(long address) {
        if(address < 0 || address > MAX_ADDRESS) {
            throw new VirtualMachineException(
                    "Invalid address: " + Long.toHexString(address) + ". Memory range: [0," + Long.toHexString(MAX_ADDRESS) + "]");
        }
    }
}
