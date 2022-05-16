package by.babanin.vm;

import by.babanin.vm.factory.VirtualMachineFactory;
import by.babanin.vm.lc3.LC3Register;
import by.babanin.vm.lc3.LC3VirtualMachine;

public class LC3OutTest {

    private static LC3VirtualMachine virtualMachine = (LC3VirtualMachine) new VirtualMachineFactory().lc3VirtualMachine();

    public static void main(String[] args) {
        short instruction = (short) 0b1111_0000_00100001;
        char value = 'D';
        virtualMachine.setRegisterValue(LC3Register.R0, (short) value);
        virtualMachine.trap(instruction);
    }
}
