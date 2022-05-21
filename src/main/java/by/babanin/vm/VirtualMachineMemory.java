package by.babanin.vm;

public interface VirtualMachineMemory {

    void writeInstruction(final long address, final long instruction);

    long readInstruction(final long address);
}
