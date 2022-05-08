package by.babanin.vm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}