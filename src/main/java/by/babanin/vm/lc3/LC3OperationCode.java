package by.babanin.vm.lc3;

import java.util.Arrays;
import java.util.function.BiConsumer;

import by.babanin.vm.ByteOperationCode;
import by.babanin.vm.exception.VirtualMachineException;

public enum LC3OperationCode implements ByteOperationCode {
    OP_BR((byte) 0x00, LC3VirtualMachine::br),     /* branch */
    OP_ADD((byte) 0x01, LC3VirtualMachine::add),    /* add  */
    OP_LD((byte) 0x02, LC3VirtualMachine::ld),     /* load */
    OP_ST((byte) 0x03, LC3VirtualMachine::st),     /* store */
    OP_JSR((byte) 0x04, LC3VirtualMachine::jsr),    /* jump register */
    OP_AND((byte) 0x05, LC3VirtualMachine::and),    /* bitwise and */
    OP_LDR((byte) 0x06, LC3VirtualMachine::ldr),    /* load register */
    OP_STR((byte) 0x07, LC3VirtualMachine::str),    /* store register */
    OP_RTI((byte) 0x08, LC3VirtualMachine::rti),    /* unused */
    OP_NOT((byte) 0x09, LC3VirtualMachine::not),    /* bitwise not */
    OP_LDI((byte) 0x0A, LC3VirtualMachine::ldi),    /* load indirect */
    OP_STI((byte) 0x0B, LC3VirtualMachine::sti),    /* store indirect */
    OP_JMP((byte) 0x0C, LC3VirtualMachine::jmp),    /* jump */
    OP_RES((byte) 0x0D, LC3VirtualMachine::res),    /* reserved (unused) */
    OP_LEA((byte) 0x0E, LC3VirtualMachine::lea),    /* load effective address */
    OP_TRAP((byte) 0x0F, LC3VirtualMachine::trap),   /* execute trap */;

    private final byte operationCode;
    private final BiConsumer<LC3VirtualMachine, Short> executor;

    LC3OperationCode(byte operationCode, BiConsumer<LC3VirtualMachine, Short> executor) {
        this.operationCode = operationCode;
        this.executor = executor;
    }

    @Override
    public byte getCode() {
        return operationCode;
    }

    public static LC3OperationCode valueOf(short instruction, byte instructionSize) {
        byte opCode = (byte) (instruction >> instructionSize - 4);
        return Arrays.stream(LC3OperationCode.values())
                .filter(lc3OperationCode -> lc3OperationCode.getCode() == opCode)
                .findFirst()
                .orElseThrow(() -> new VirtualMachineException(Integer.toHexString(opCode) + " operation code is not defined"));
    }

    public void execute(LC3VirtualMachine virtualMachine, short instruction) {
        executor.accept(virtualMachine, instruction);
    }
}
