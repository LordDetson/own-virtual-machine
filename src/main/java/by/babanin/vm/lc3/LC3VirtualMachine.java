package by.babanin.vm.lc3;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import by.babanin.vm.ConditionFlag;
import by.babanin.vm.VirtualMachine;
import by.babanin.vm.memory.VirtualMachineMemory;
import by.babanin.vm.util.Utils;

/**
 * Memory has 16384 cells. Each cell is 8 bytes or 64 bits.
 * Memory size is 131072 bytes or 128 KB.
 */
public class LC3VirtualMachine implements VirtualMachine {

    private static final byte MEMORY_CELLS_DEGREE = 14;
    private static final byte MEMORY_CELLS_BASIS = 2;
    private static final byte INSTRUCTION_SIZE = 16;
    private static final short PC_START = 0x3000;
    private final Map<LC3Register, Short> registers = new HashMap<>(LC3Register.values().length);
    private final VirtualMachineMemory memory;

    public LC3VirtualMachine() {
        int cellsAmount = (int) Math.pow(MEMORY_CELLS_BASIS, MEMORY_CELLS_DEGREE);
        this.memory = new VirtualMachineMemory(cellsAmount, INSTRUCTION_SIZE);

    }

    @Override
    public void run() {
        setRegisterValue(LC3Register.R_PC, PC_START);

        boolean running = true;
        while(running) {
            short instruction = (short) memory.readInstruction(getAndIncProgramCounter());
            LC3OperationCode operationCode = LC3OperationCode.valueOf(instruction, INSTRUCTION_SIZE);
            operationCode.execute(this, instruction);
        }
    }

    @Override
    public void writeProgram(long address, String program) {
        for(int i = 0; i < program.length(); i += INSTRUCTION_SIZE) {
            long instruction = Utils.parseLong(program.substring(i, i + INSTRUCTION_SIZE), 2);
            memory.writeInstruction(address, instruction);
            address += INSTRUCTION_SIZE;
        }
    }

    public void setRegisterValue(LC3Register register, short value) {
        registers.put(register, value);
    }

    public short getRegisterValue(LC3Register register) {
        Short value = registers.get(register);
        if(value == null) {
            value = 0;
            setRegisterValue(register, value);
        }
        return value;
    }

    public void setConditionFlag(ConditionFlag flag) {
        setRegisterValue(LC3Register.R_COND, flag.getFlagCode());
    }

    private void updateFlag(LC3Register register) {
        short value = getRegisterValue(register);
        setConditionFlag(LC3ConditionFlag.valueOf(value, INSTRUCTION_SIZE));
    }

    public ConditionFlag getConditionFlag() {
        short value = getRegisterValue(LC3Register.R_COND);
        return LC3ConditionFlag.valueOfFlag(value);
    }

    public void setProgramCounter(short value) {
        setRegisterValue(LC3Register.R_PC, value);
    }

    public short getProgramCounter() {
        return getRegisterValue(LC3Register.R_PC);
    }

    public short getAndIncProgramCounter() {
        short pc = getProgramCounter();
        setProgramCounter((short) (pc + INSTRUCTION_SIZE));
        return pc;
    }

    public void br(short instruction) {
        byte n = (byte) ((instruction >>> 11) & 0x1);
        byte z = (byte) ((instruction >>> 10) & 0x1);
        byte p = (byte) ((instruction >>> 9) & 0x1);

        ConditionFlag conditionFlag = getConditionFlag();
        if((n == 0 && z == 0 && p == 0) ||
                (n == 1 && conditionFlag == LC3ConditionFlag.FL_NEG) ||
                (z == 1 && conditionFlag == LC3ConditionFlag.FL_ZRO) ||
                (p == 1 && conditionFlag == LC3ConditionFlag.FL_POS)) {
            short pcOffset = signExtend((short) (instruction & 0x01FF), (byte) 9);
            setProgramCounter((short) (getProgramCounter() + pcOffset));
        }
    }

    public void add(short instruction) {
        /* destination register (DR) */
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        /* first operand (SR1) */
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        /* whether we are in immediate mode */
        short imm_flag = (short) ((instruction >>> 5) & 0x1);
        if(imm_flag == 1) {
            short imm5 = signExtend((short) (instruction & 0x1F), (byte) 5);
            setRegisterValue(r0, (short) (getRegisterValue(r1) + imm5));
        }
        else {
            LC3Register r2 = LC3Register.valueOf((byte) (instruction & 0x7));
            setRegisterValue(r0, (short) (getRegisterValue(r1) + getRegisterValue(r2)));
        }

        updateFlag(r0);
    }

    public short signExtend(short value, byte size) {
        if((value >> (size - 1)) == 1) {
            value |= (0xFFFF << size);
        }
        return value;
    }

    public void ld(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x1FF), (byte) 9);
        short value = (short) memory.readInstruction(getProgramCounter() + pcOffset);
        setRegisterValue(r0, value);
        updateFlag(r0);
    }

    public void st(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x1FF), (byte) 9);
        memory.writeInstruction(getProgramCounter() + pcOffset, getRegisterValue(r0));
    }

    public void jsr(short instruction) {
        setRegisterValue(LC3Register.R7, getProgramCounter());
        byte immFlag = (byte) ((instruction >>> 11) & 0x1);
        if(immFlag == 0) {
            LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
            setProgramCounter(getRegisterValue(r0));
        }
        else {
            short pcOffset = signExtend((short) (instruction & 0x7FF), (byte) 11);
            setProgramCounter((short) (getProgramCounter() + pcOffset));
        }
    }

    public void and(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        byte immFlag = (byte) ((instruction >>> 5) & 0x1);
        if(immFlag == 0) {
            LC3Register r2 = LC3Register.valueOf((byte) (instruction & 0x7));
            setRegisterValue(r0, (short) (getRegisterValue(r1) & getRegisterValue(r2)));
        }
        else {
            short imm5 = signExtend((short) (instruction & 0x1F), (byte) 5);
            setRegisterValue(r0, (short) (getRegisterValue(r1) & imm5));
        }

        updateFlag(r0);
    }

    public void ldr(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x3F), (byte) 6);
        int address = getRegisterValue(r1) + pcOffset;
        short value = (short) memory.readInstruction(address);
        setRegisterValue(r0, value);
        updateFlag(r0);
    }

    public void str(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x3F), (byte) 6);
        memory.writeInstruction(getRegisterValue(r1) + pcOffset, getRegisterValue(r0));
    }

    public void rti(short instruction) {
        // TODO need to implement
    }

    public void not(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        setRegisterValue(r0, (short) ~getRegisterValue(r1));
        updateFlag(r0);
    }

    public void ldi(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x01FF), (byte) 9);
        setRegisterValue(r0, (short) memory.readInstruction(memory.readInstruction(getProgramCounter() + pcOffset)));
        updateFlag(r0);
    }

    public void sti(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x1FF), (byte) 9);
        memory.writeInstruction(memory.readInstruction(getProgramCounter() + pcOffset), getRegisterValue(r0));
    }

    public void jmp(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        setProgramCounter(getRegisterValue(r0));
    }

    public void res(short instruction) {
        // TODO need to implement
    }

    public void lea(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x01FF), (byte) 9);
        setRegisterValue(r0, (short) (getProgramCounter() + pcOffset));
        updateFlag(r0);
    }

    public void trap(short instruction) {
        LC3TrapCode trapCode = LC3TrapCode.valueOf((byte) (instruction & 0xFF));
        trapCode.execute(this);
    }

    public void getc() {
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        setRegisterValue(LC3Register.R0, (short) str.charAt(0));
    }

    public void out() {
        char registerValue = (char) getRegisterValue(LC3Register.R0);
        System.out.println(registerValue);
    }

    public void puts() {
        short charAddress = getRegisterValue(LC3Register.R0);
        char character;
        StringBuilder builder = new StringBuilder();
        do {
            character = (char) memory.readInstruction(charAddress);
            builder.append(character);
            charAddress += 16;
        }
        while(character != 0);
        System.out.println(builder);
    }

    public void in() {
        System.out.print(">> ");
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        setRegisterValue(LC3Register.R0, (short) str.charAt(0));
    }

    public void putsp() {

    }

    public void halt() {

    }
}
