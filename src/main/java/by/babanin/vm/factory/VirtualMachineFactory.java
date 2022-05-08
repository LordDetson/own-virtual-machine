package by.babanin.vm.factory;

import by.babanin.vm.VirtualMachine;
import by.babanin.vm.lc3.LC3VirtualMachine;

public class VirtualMachineFactory {

    public VirtualMachine lc3VirtualMachine() {
        return new LC3VirtualMachine();
    }
}
