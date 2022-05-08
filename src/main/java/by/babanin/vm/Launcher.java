package by.babanin.vm;

import by.babanin.vm.factory.VirtualMachineFactory;

public class Launcher {

    public static VirtualMachineFactory virtualMachineFactory;
    public static VirtualMachine virtualMachine;

    public static void main(String[] args) {
        virtualMachineFactory = new VirtualMachineFactory();
        virtualMachine = virtualMachineFactory.lc3VirtualMachine();
    }
}
