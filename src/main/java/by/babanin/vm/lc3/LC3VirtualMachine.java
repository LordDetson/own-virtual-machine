package by.babanin.vm.lc3;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import by.babanin.vm.ConditionFlag;
import by.babanin.vm.VirtualMachine;
import by.babanin.vm.VirtualMachineMemory;
import by.babanin.vm.util.Utils;

public class LC3VirtualMachine implements VirtualMachine {

    private static final Logger logger = LogManager.getLogger();
    private static final byte INSTRUCTION_SIZE = 16;
    private static final short PC_START = 0x3000;
    private final Map<LC3Register, Short> registers = new HashMap<>(LC3Register.values().length);
    private final VirtualMachineMemory memory;

    private boolean running;

    public LC3VirtualMachine() {
        this.memory = new LC3Memory();
    }

    @Override
    public void run() {
        setRegisterValue(LC3Register.R_PC, PC_START);

        running = true;
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
            address ++;
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
        setProgramCounter((short) (pc + 1));
        return pc;
    }

    public void br(short instruction) {
        boolean n = ((instruction >>> 11) & 0x1) == 1;
        boolean z = ((instruction >>> 10) & 0x1) == 1;
        boolean p = ((instruction >>> 9) & 0x1) == 1;
        short pcOffset = signExtend((short) (instruction & 0x01FF), (byte) 9);
        short programCounter = getProgramCounter();
        ConditionFlag conditionFlag = getConditionFlag();

        logger.info("pc = {}; operation = {}; n = {}; z = {}; p = {}; pcOffset = {}",
                Integer.toHexString(programCounter), LC3OperationCode.BR.name(), n, z, p, Integer.toHexString(pcOffset));

        if((!n && !z && !p) ||
                (n && conditionFlag == LC3ConditionFlag.FL_NEG) ||
                (z && conditionFlag == LC3ConditionFlag.FL_ZRO) ||
                (p && conditionFlag == LC3ConditionFlag.FL_POS)) {
            setProgramCounter((short) (programCounter + pcOffset));
        }
    }

    public void add(short instruction) {
        /* destination register (DR) */
        LC3Register dr = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        /* first operand (SR1) */
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        /* whether we are in immediate mode */
        short imm_flag = (short) ((instruction >>> 5) & 0x1);
        short programCounter = getProgramCounter();
        if(imm_flag == 1) {
            short imm5 = signExtend((short) (instruction & 0x1F), (byte) 5);
            setRegisterValue(dr, (short) (getRegisterValue(r1) + imm5));
            logger.info("pc = {}; operation = {}; dr = {}; r1 = {}; imm5 = {}",
                    Integer.toHexString(programCounter), LC3OperationCode.ADD.name(), dr, r1, Integer.toHexString(imm5));
        }
        else {
            LC3Register r2 = LC3Register.valueOf((byte) (instruction & 0x7));
            setRegisterValue(dr, (short) (getRegisterValue(r1) + getRegisterValue(r2)));
            logger.info("pc = {}; operation = {}; dr = {}; r1 = {}; r2 = {}",
                    Integer.toHexString(programCounter), LC3OperationCode.ADD.name(), dr, r1, r2);
        }

        updateFlag(dr);
    }

    public short signExtend(short value, byte size) {
        if((value >> (size - 1)) == 1) {
            value |= (0xFFFF << size);
        }
        return value;
    }

    public void ld(short instruction) {
        LC3Register dr = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x1FF), (byte) 9);
        short programCounter = getProgramCounter();
        short value = (short) memory.readInstruction(programCounter + pcOffset);
        setRegisterValue(dr, value);
        updateFlag(dr);
        logger.info("pc = {}; operation = {}; dr = {}; pcOffset = {}",
                Integer.toHexString(programCounter), LC3OperationCode.LD.name(), dr, Integer.toHexString(pcOffset));
    }

    public void st(short instruction) {
        LC3Register r = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x1FF), (byte) 9);
        short programCounter = getProgramCounter();
        memory.writeInstruction(programCounter + pcOffset, getRegisterValue(r));
        logger.info("pc = {}; operation = {}; r = {}; pcOffset = {}",
                Integer.toHexString(programCounter), LC3OperationCode.ST.name(), r, Integer.toHexString(pcOffset));
    }

    public void jsr(short instruction) {
        short programCounter = getProgramCounter();
        setRegisterValue(LC3Register.R7, programCounter);
        byte immFlag = (byte) ((instruction >>> 11) & 0x1);
        if(immFlag == 0) {
            LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
            setProgramCounter(getRegisterValue(r0));
            logger.info("pc = {}; operation = {}; r0 = {}",
                    Integer.toHexString(programCounter), LC3OperationCode.JSR.name(), r0);
        }
        else {
            short pcOffset = signExtend((short) (instruction & 0x7FF), (byte) 11);
            setProgramCounter((short) (programCounter + pcOffset));
            logger.info("pc = {}; operation = {}; pcOffset = {}",
                    Integer.toHexString(programCounter), LC3OperationCode.JSR.name(), Integer.toHexString(pcOffset));
        }
    }

    public void and(short instruction) {
        LC3Register dr = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        byte immFlag = (byte) ((instruction >>> 5) & 0x1);
        short programCounter = getProgramCounter();
        if(immFlag == 0) {
            LC3Register r2 = LC3Register.valueOf((byte) (instruction & 0x7));
            setRegisterValue(dr, (short) (getRegisterValue(r1) & getRegisterValue(r2)));
            logger.info("pc = {}; operation = {}; dr = {}; r1 = {}; r2 = {}",
                    Integer.toHexString(programCounter), LC3OperationCode.AND.name(), dr, r1, r2);
        }
        else {
            short imm5 = signExtend((short) (instruction & 0x1F), (byte) 5);
            setRegisterValue(dr, (short) (getRegisterValue(r1) & imm5));
            logger.info("pc = {}; operation = {}; dr = {}; r1 = {}; imm5 = {}",
                    Integer.toHexString(programCounter), LC3OperationCode.AND.name(), dr, r1, Integer.toHexString(imm5));
        }

        updateFlag(dr);
    }

    public void ldr(short instruction) {
        LC3Register dr = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x3F), (byte) 6);
        int address = getRegisterValue(r1) + pcOffset;
        short value = (short) memory.readInstruction(address);
        setRegisterValue(dr, value);
        updateFlag(dr);
        logger.info("pc = {}; operation = {}; dr = {}; r1 = {}; pcOffset = {}",
                Integer.toHexString(getProgramCounter()), LC3OperationCode.LDR.name(), dr, r1, Integer.toHexString(pcOffset));
    }

    public void str(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x3F), (byte) 6);
        memory.writeInstruction(getRegisterValue(r1) + pcOffset, getRegisterValue(r0));
        logger.info("pc = {}; operation = {}; r0 = {}; r1 = {}; pcOffset = {}",
                Integer.toHexString(getProgramCounter()), LC3OperationCode.STR.name(), r0, r1, Integer.toHexString(pcOffset));
    }

    public void rti(short instruction) {
        // TODO need to implement
    }

    public void not(short instruction) {
        LC3Register dr = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        LC3Register r1 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        setRegisterValue(dr, (short) ~getRegisterValue(r1));
        updateFlag(dr);
        logger.info("pc = {}; operation = {}; dr = {}; r1 = {}",
                Integer.toHexString(getProgramCounter()), LC3OperationCode.NOT.name(), dr, r1);
    }

    public void ldi(short instruction) {
        LC3Register dr = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x01FF), (byte) 9);
        short programCounter = getProgramCounter();
        setRegisterValue(dr, (short) memory.readInstruction(memory.readInstruction(programCounter + pcOffset) & 0xFFFF));
        updateFlag(dr);
        logger.info("pc = {}; operation = {}; dr = {}; pcOffset = {}",
                Integer.toHexString(programCounter), LC3OperationCode.LDI.name(), dr, Integer.toHexString(pcOffset));
    }

    public void sti(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x1FF), (byte) 9);
        short programCounter = getProgramCounter();
        memory.writeInstruction(memory.readInstruction(programCounter + pcOffset), getRegisterValue(r0));
        logger.info("pc = {}; operation = {}; r0 = {}; pcOffset = {}",
                Integer.toHexString(programCounter), LC3OperationCode.STI.name(), r0, Integer.toHexString(pcOffset));

    }

    public void jmp(short instruction) {
        LC3Register r0 = LC3Register.valueOf((byte) ((instruction >>> 6) & 0x7));
        short programCounter = getProgramCounter();
        setProgramCounter(getRegisterValue(r0));
        logger.info("pc = {}; operation = {}; r0 = {}",
                Integer.toHexString(programCounter), LC3OperationCode.JMP.name(), r0);
    }

    public void res(short instruction) {
        // TODO need to implement
    }

    public void lea(short instruction) {
        LC3Register dr = LC3Register.valueOf((byte) ((instruction >>> 9) & 0x7));
        short pcOffset = signExtend((short) (instruction & 0x01FF), (byte) 9);
        short programCounter = getProgramCounter();
        setRegisterValue(dr, (short) (programCounter + pcOffset));
        updateFlag(dr);
        logger.info("pc = {}; operation = {}; dr = {}; pcOffset = {}",
                Integer.toHexString(programCounter), LC3OperationCode.LEA.name(), dr, Integer.toHexString(pcOffset));
    }

    public void trap(short instruction) {
        LC3TrapCode trapCode = LC3TrapCode.valueOf((byte) (instruction & 0xFF));
        short programCounter = getProgramCounter();
        trapCode.execute(this);
        logger.info("pc = {}; operation = {}; trap = {}",
                Integer.toHexString(programCounter), LC3OperationCode.TRAP.name(), trapCode.name());
    }

    public void getc() {
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        short value = (short) (str.isEmpty() ? 0 : str.charAt(0));
        setRegisterValue(LC3Register.R0, value);
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
            character = (char) memory.readInstruction(charAddress++);
            builder.append(character);
        }
        while(character != 0);
        System.out.print(builder);
    }

    public void in() {
        System.out.print(">> ");
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        setRegisterValue(LC3Register.R0, (short) str.charAt(0));
    }

    public void putsp() {
        short charAddress = getRegisterValue(LC3Register.R0);
        char twoCharacters;
        StringBuilder builder = new StringBuilder();
        do {
            twoCharacters = (char) memory.readInstruction(charAddress++);
            builder.append((char) ((twoCharacters >> 8) & 0xFF));
            builder.append((char) (twoCharacters & 0xFF));
        }
        while(twoCharacters != 0);
        System.out.print(builder);
    }

    public void halt() {
        System.out.println("HALT");
        running = false;
    }
}
