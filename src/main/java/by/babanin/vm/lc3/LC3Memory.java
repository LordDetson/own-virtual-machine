package by.babanin.vm.lc3;

import java.util.Scanner;

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
        int kbsrAddress = (int) (LC3MemoryRegister.KBSR.getAddress() & 0xFFFF);
        int kbdrAddress = (int) (LC3MemoryRegister.KBDR.getAddress() & 0xFFFF);
        if(kbsrAddress == address) {
            short character = readKey();
            memory[kbsrAddress] = (short) (1 << 15);
            memory[kbdrAddress] = character;
        }
        return memory[(int) address];
    }

    private short readKey() {
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        if(str.isEmpty()) {
            return 0;
        }
        return (short) str.charAt(0);
    }

    private void validateAddress(long address) {
        if(address < 0 || address > MAX_ADDRESS) {
            throw new VirtualMachineException(
                    "Invalid address: " + Long.toHexString(address) + ". Memory range: [0," + Long.toHexString(MAX_ADDRESS) + "]");
        }
    }
}
