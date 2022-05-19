package by.babanin.vm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import by.babanin.vm.exception.VirtualMachineException;
import by.babanin.vm.factory.VirtualMachineFactory;
import by.babanin.vm.lc3.LC3ConditionFlag;
import by.babanin.vm.lc3.LC3Register;
import by.babanin.vm.lc3.LC3VirtualMachine;

class LC3VirtualMachineTest {

    private LC3VirtualMachine virtualMachine;

    @BeforeEach
    void setUp() {
        virtualMachine = (LC3VirtualMachine) new VirtualMachineFactory().lc3VirtualMachine();
    }

    @AfterEach
    void tearDown() {
        virtualMachine = null;
    }

    @Test
    void testAdd1() {
        short instruction = 0b0001_000_001_0_00_010;
        virtualMachine.setRegisterValue(LC3Register.R1, (short) 2);
        virtualMachine.setRegisterValue(LC3Register.R2, (short) 4);
        virtualMachine.add(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R0);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(6, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_POS, conditionFlag);
    }

    @Test
    void testAdd2() {
        short instruction = 0b0001_000_001_0_00_010;
        virtualMachine.setRegisterValue(LC3Register.R1, (short) 2);
        virtualMachine.setRegisterValue(LC3Register.R2, (short) -4);
        virtualMachine.add(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R0);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(-2, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_NEG, conditionFlag);
    }

    @Test
    void testAdd3() {
        short instruction = 0b0001_111_101_0_00_110;
        virtualMachine.setRegisterValue(LC3Register.R5, (short) 2);
        virtualMachine.setRegisterValue(LC3Register.R6, (short) -4);
        virtualMachine.add(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R7);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(-2, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_NEG, conditionFlag);
    }

    @Test
    void testAdd4() {
        short instruction = 0b0001_111_101_1_01111; //+15
        virtualMachine.setRegisterValue(LC3Register.R5, (short) 2);
        virtualMachine.add(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R7);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(17, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_POS, conditionFlag);
    }

    @Test
    void testAdd5() {
        short instruction = 0b0001_111_101_1_11110; //-2
        virtualMachine.setRegisterValue(LC3Register.R5, (short) 2);
        virtualMachine.add(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R7);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(0, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_ZRO, conditionFlag);
    }

    @Test
    void testAdd6() {
        short instruction = 0b0001_111_101_1_10101; //-11
        virtualMachine.setRegisterValue(LC3Register.R5, (short) 2);
        virtualMachine.add(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R7);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(-9, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_NEG, conditionFlag);
    }

    @Test
    void testLdi1() {
        long address = 0x3000;
        short instruction = (short) 0b1010_001_000010000;
        String program =
                "1010001000010000" +
                "0000000000000000" + // PC position
                "0011000001000000" +
                "0000000000000000" +
                "0000000001111111";
        virtualMachine.writeProgram(address, program);
        virtualMachine.setProgramCounter((short) address);
        virtualMachine.getAndIncProgramCounter();

        virtualMachine.ldi(instruction);

        short value = virtualMachine.getRegisterValue(LC3Register.R1);
        Assertions.assertEquals(0x7F, value);
    }

    @Test
    void testLdi2() {
        long address = 0x3000;
        short instruction = (short) 0b1010_001_000010000;
        String program =
                "0000000001111111" +
                "1010001000010000" +
                "0000000000000000" + // PC position
                "0011000000000000";
        virtualMachine.writeProgram(address, program);
        virtualMachine.setProgramCounter((short) address);
        virtualMachine.getAndIncProgramCounter();
        virtualMachine.getAndIncProgramCounter();

        virtualMachine.ldi(instruction);

        short value = virtualMachine.getRegisterValue(LC3Register.R1);
        Assertions.assertEquals(0x7F, value);
    }

    @Test
    void testAndRegisterMode() {
        short instruction = (short) 0b0101_000_000_0_00_001;
        virtualMachine.setRegisterValue(LC3Register.R0, (short) 5);
        virtualMachine.setRegisterValue(LC3Register.R1, (short) -5);

        virtualMachine.and(instruction);

        short value = virtualMachine.getRegisterValue(LC3Register.R0);
        Assertions.assertEquals(1, value);
        Assertions.assertEquals(LC3ConditionFlag.FL_POS, virtualMachine.getConditionFlag());
    }

    @Test
    void testAndImmediateMode() {
        short instruction = (short) 0b0101_000_000_1_11011; // -5
        virtualMachine.setRegisterValue(LC3Register.R0, (short) 5);

        virtualMachine.and(instruction);

        short value = virtualMachine.getRegisterValue(LC3Register.R0);
        Assertions.assertEquals(1, value);
        Assertions.assertEquals(LC3ConditionFlag.FL_POS, virtualMachine.getConditionFlag());
    }

    @Test
    void testNot() {
        short instruction = (short) 0b0101_000_000_1_11111;
        virtualMachine.setRegisterValue(LC3Register.R0, (short) 5);

        virtualMachine.not(instruction);

        short value = virtualMachine.getRegisterValue(LC3Register.R0);
        Assertions.assertEquals(-6, value);
        Assertions.assertEquals(LC3ConditionFlag.FL_NEG, virtualMachine.getConditionFlag());
    }

    @Test
    void testBr1() {
        short instruction = (short) 0b0000_000_000111111;
        virtualMachine.setConditionFlag(LC3ConditionFlag.FL_ZRO);

        virtualMachine.br(instruction);

        short programCounter = virtualMachine.getProgramCounter();
        Assertions.assertEquals(0x3F, programCounter);
    }

    @Test
    void testBrn() {
        short instruction = (short) 0b0000_100_000111111;
        virtualMachine.setConditionFlag(LC3ConditionFlag.FL_ZRO);

        Assertions.assertThrows(VirtualMachineException.class, () -> virtualMachine.br(instruction));
    }

    @Test
    void testBrz() {
        short instruction = (short) 0b0000_010_000111111;
        virtualMachine.setConditionFlag(LC3ConditionFlag.FL_ZRO);

        virtualMachine.br(instruction);

        short programCounter = virtualMachine.getProgramCounter();
        Assertions.assertEquals(0x3F, programCounter);
    }

    @Test
    void testBrnz() {
        short instruction = (short) 0b0000_110_000111111;
        virtualMachine.setConditionFlag(LC3ConditionFlag.FL_ZRO);

        virtualMachine.br(instruction);

        short programCounter = virtualMachine.getProgramCounter();
        Assertions.assertEquals(0x3F, programCounter);
    }

    @Test
    void testJmp() {
        short instruction = (short) 0b1100_000_011_000000;
        virtualMachine.setRegisterValue(LC3Register.R3, (short) 0x3000);
        virtualMachine.jmp(instruction);
        short programCounter = virtualMachine.getProgramCounter();
        Assertions.assertEquals(0x3000, programCounter);
    }

    @Test
    void testRet() {
        short instruction = (short) 0b1100_000_111_000000;
        virtualMachine.setRegisterValue(LC3Register.R7, (short) 0x3000);
        virtualMachine.jmp(instruction);
        short programCounter = virtualMachine.getProgramCounter();
        Assertions.assertEquals(0x3000, programCounter);
    }

    @Test
    void testJsrImmediateMode() {
        short instruction = (short) 0b0100_1_00000110000;
        virtualMachine.setProgramCounter((short) 0x3000);
        virtualMachine.jsr(instruction);
        short programCounter = virtualMachine.getProgramCounter();
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R7);
        Assertions.assertEquals(0x3030, programCounter);
        Assertions.assertEquals(0x3000, registerValue);
    }

    @Test
    void testJsrRegisterMode() {
        short instruction = (short) 0b0100_0_00_011_000000;
        virtualMachine.setRegisterValue(LC3Register.R3, (short) 0x3030);
        virtualMachine.setProgramCounter((short) 0x3000);
        virtualMachine.jsr(instruction);
        short programCounter = virtualMachine.getProgramCounter();
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R7);
        Assertions.assertEquals(0x3030, programCounter);
        Assertions.assertEquals(0x3000, registerValue);
    }

    @Test
    void testLd() {
        short instruction = (short) 0b0010_000_000110000;
        virtualMachine.setProgramCounter((short) 0x0030);
        virtualMachine.writeProgram(0x0060, "0000000000011010");
        virtualMachine.ld(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R0);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(26, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_POS, conditionFlag);
    }

    @Test
    void testLdr() {
        short instruction = (short) 0b0110_000_000_011000;
        virtualMachine.setRegisterValue(LC3Register.R0, (short) 0x3000);
        virtualMachine.writeProgram(0x3018, "0000000000011010");
        virtualMachine.ldr(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R0);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(26, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_POS, conditionFlag);
    }

    @Test
    void testLea() {
        short instruction = (short) 0b1110_000_000110000;
        virtualMachine.setProgramCounter((short) 0x3000);
        virtualMachine.lea(instruction);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R0);
        ConditionFlag conditionFlag = virtualMachine.getConditionFlag();
        Assertions.assertEquals(0x3030, registerValue);
        Assertions.assertEquals(LC3ConditionFlag.FL_POS, conditionFlag);
    }

    @Test
    void testSt() {
        short instruction = (short) 0b0011_000_000110000;
        virtualMachine.setRegisterValue(LC3Register.R0, (short) 26);
        virtualMachine.setProgramCounter((short) 0x3000);
        virtualMachine.st(instruction);

        short instructionToReadValue = (short) 0b0010_001_000110000;
        virtualMachine.ld(instructionToReadValue);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R1);
        Assertions.assertEquals(26, registerValue);
    }

    @Test
    void testSti() {
        short instruction = (short) 0b1011_000_000110000;
        virtualMachine.setRegisterValue(LC3Register.R0, (short) 26);
        virtualMachine.setProgramCounter((short) 0x3000);
        virtualMachine.writeProgram(0x3030, "0010000000000000"); // 0x2000
        virtualMachine.sti(instruction);

        short instructionToReadValue = (short) 0b1010_001_000110000;
        virtualMachine.ldi(instructionToReadValue);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R1);
        Assertions.assertEquals(26, registerValue);
    }

    @Test
    void testStr() {
        short instruction = (short) 0b0111_000_001_010000;
        virtualMachine.setRegisterValue(LC3Register.R0, (short) 26);
        virtualMachine.setRegisterValue(LC3Register.R1, (short) 0x3000);
        virtualMachine.str(instruction);

        short instructionToReadValue = (short) 0b0110_010_001_010000;
        virtualMachine.ldr(instructionToReadValue);
        short registerValue = virtualMachine.getRegisterValue(LC3Register.R2);
        Assertions.assertEquals(26, registerValue);
    }
}