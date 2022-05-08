package by.babanin.vm.lc3;

import java.util.Arrays;

import by.babanin.vm.exception.VirtualMachineException;

public enum LC3Register {
    R0((byte) 0),
    R1((byte) 1),
    R2((byte) 2),
    R3((byte) 3),
    R4((byte) 4),
    R5((byte) 5),
    R6((byte) 6),
    R7((byte) 7),
    R_PC((byte) 8),   // Program counter is the memory address of the next instruction to be executed
    R_COND((byte) 9), // Condition flag tells us information about the previous calculation
    R_COUNT((byte) 10),
    ;

    private final byte value;

    LC3Register(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static LC3Register valueOf(byte value) {
        return Arrays.stream(LC3Register.values())
                .filter(register -> register.getValue() == value)
                .findFirst()
                .orElseThrow(() -> new VirtualMachineException(Integer.toHexString(value) + " register is defined"));
    }
}
