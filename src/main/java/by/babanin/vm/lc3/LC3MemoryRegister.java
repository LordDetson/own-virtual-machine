package by.babanin.vm.lc3;

import by.babanin.vm.MemoryRegister;

public enum LC3MemoryRegister implements MemoryRegister {
    KBSR((short) 0xFE00),
    KBDR((short) 0xFE02),
    ;

    private final short address;

    LC3MemoryRegister(short address) {
        this.address = address;
    }

    @Override
    public long getAddress() {
        return address;
    }
}
