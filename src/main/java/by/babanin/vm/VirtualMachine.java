package by.babanin.vm;

public interface VirtualMachine {

    void run();

    void writeProgram(long address, String program);
}
