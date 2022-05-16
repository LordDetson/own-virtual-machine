package by.babanin.vm;

import by.babanin.vm.factory.VirtualMachineFactory;
import by.babanin.vm.lc3.LC3Register;
import by.babanin.vm.lc3.LC3VirtualMachine;

public class LC3PutspTest {

    private static LC3VirtualMachine virtualMachine = (LC3VirtualMachine) new VirtualMachineFactory().lc3VirtualMachine();

    public static void main(String[] args) {
        short instruction = (short) 0b1111_0000_00100100;
        short charAddress = 0x3000;
        virtualMachine.setRegisterValue(LC3Register.R0, charAddress);
        virtualMachine.writeProgram(charAddress, toBinaryString("Hello, World!"));
        virtualMachine.trap(instruction);
    }

    private static String toBinaryString(String str) {
        StringBuilder builder = new StringBuilder();
        for (byte b : str.getBytes()) {
            builder.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        builder.append("0".repeat(16));
        int spaceSize = builder.length() % 16;
        if(spaceSize != 0) {
            builder.append("0".repeat(spaceSize));
        }
        return builder.toString();
    }
}
