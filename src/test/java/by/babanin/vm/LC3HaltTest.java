package by.babanin.vm;

import by.babanin.vm.factory.VirtualMachineFactory;
import by.babanin.vm.lc3.LC3VirtualMachine;

public class LC3HaltTest {

    private static LC3VirtualMachine virtualMachine = (LC3VirtualMachine) new VirtualMachineFactory().lc3VirtualMachine();

    public static void main(String[] args) {
        short instruction = (short) 0b1111_0000_00100101;
        virtualMachine.trap(instruction);
    }
}
