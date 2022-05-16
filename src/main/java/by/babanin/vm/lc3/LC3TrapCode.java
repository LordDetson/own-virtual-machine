package by.babanin.vm.lc3;

import java.util.Arrays;
import java.util.function.Consumer;

import by.babanin.vm.TrapCode;
import by.babanin.vm.exception.VirtualMachineException;

public enum LC3TrapCode implements TrapCode {
    GETC((byte) 0x20, LC3VirtualMachine::getc),  // get character from keyboard
    OUT((byte) 0x21, LC3VirtualMachine::out),   // output a character
    PUTS((byte) 0x22, LC3VirtualMachine::puts),  // output a word string
    IN((byte) 0x23, LC3VirtualMachine::in),    // input a string
    PUTSP((byte) 0x24, LC3VirtualMachine::putsp), // output a byte string
    HALT((byte) 0x25, LC3VirtualMachine::halt),  // halt the program
    ;

    private final byte trapCode;
    private final Consumer<LC3VirtualMachine> executor;

    LC3TrapCode(byte trapCode, Consumer<LC3VirtualMachine> executor) {
        this.trapCode = trapCode;
        this.executor = executor;
    }

    @Override
    public byte getValue() {
        return trapCode;
    }

    public static LC3TrapCode valueOf(byte value) {
        return Arrays.stream(LC3TrapCode.values())
                .filter(lc3TrapCode -> lc3TrapCode.getValue() == value)
                .findFirst()
                .orElseThrow(() -> new VirtualMachineException(Integer.toHexString(value) + " trap code isn't defined"));
    }

    public void execute(LC3VirtualMachine virtualMachine) {
        executor.accept(virtualMachine);
    }
}
