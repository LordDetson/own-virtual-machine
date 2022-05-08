package by.babanin.vm.memory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import by.babanin.vm.exception.VirtualMachineException;

class VirtualMachineMemoryTest {

    private static final int CELLS_AMOUNT = (int) Math.pow(2, 14);
    private static final byte INSTRUCTION_SIZE = 16;
    private VirtualMachineMemory memory;

    @BeforeEach
    void setUp() {
        memory = new VirtualMachineMemory(CELLS_AMOUNT, (byte) 16);
    }

    @AfterEach
    void tearDown() {
        memory = null;
    }

    @Test
    void testConstructor() {
        Assertions.assertThrows(VirtualMachineException.class, () -> new VirtualMachineMemory(0, INSTRUCTION_SIZE));
        Assertions.assertThrows(VirtualMachineException.class, () -> new VirtualMachineMemory(-10, INSTRUCTION_SIZE));
        Assertions.assertThrows(VirtualMachineException.class, () -> new VirtualMachineMemory(CELLS_AMOUNT, (byte) 0));
        Assertions.assertThrows(VirtualMachineException.class, () -> new VirtualMachineMemory(CELLS_AMOUNT, (byte) 65));
    }

    @Test
    void testInCellBeginning() {
        long address = 0x3000;
        long instruction = 0b1001_1100_0100_1011;
        checkWriteReadInstruction(address, instruction);
    }

    @Test
    void testInCellAnd() {
        long address = 0x3030;
        long instruction = 0b1001_1100_0100_1011;
        checkWriteReadInstruction(address, instruction);
    }

    /**
     * 0x303A
     * ^
     * 100111|0001001011
     */
    @Test
    void testSplitInstruction() {
        long address = 0x303A;
        long instruction = 0b1001_1100_0100_1011;
        checkWriteReadInstruction(address, instruction);
    }

    /**
     * 0x3031   0x303A
     * ^        ^
     * 100111000100101|1
     *          100111|0001001011
     * 100111000100111|0001001011
     */
    @Test
    void testMemoryOverwrite() {
        long address1 = 0x3031;
        long address2 = 0x303A;
        long instruction = 0b1001_1100_0100_1011;
        memory.writeInstruction(address1, instruction);
        memory.writeInstruction(address2, instruction);
        long actual1 = memory.readInstruction(address1);
        long actual2 = memory.readInstruction(address2);
        Assertions.assertEquals(0b1001_1100_0100_1110, actual1);
        Assertions.assertEquals(instruction, actual2);
    }

    @Test
    void testOutOfRange() {
        long maxAddress = CELLS_AMOUNT * VirtualMachineMemory.CELL_SIZE - INSTRUCTION_SIZE;
        long instruction = 0b1001_1100_0100_1011;
        Assertions.assertThrows(VirtualMachineException.class, () -> memory.writeInstruction(maxAddress + 1, instruction));
        Assertions.assertThrows(VirtualMachineException.class, () -> memory.readInstruction(maxAddress + 1));
    }

    private void checkWriteReadInstruction(long address, long instruction) {
        memory.writeInstruction(address, instruction);
        long actual = memory.readInstruction(address);
        Assertions.assertEquals(instruction, actual);
    }
}