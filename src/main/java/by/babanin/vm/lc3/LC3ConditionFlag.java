package by.babanin.vm.lc3;

import java.util.Arrays;

import by.babanin.vm.ConditionFlag;
import by.babanin.vm.exception.VirtualMachineException;

public enum LC3ConditionFlag implements ConditionFlag {
    FL_POS((byte) (1 << 0)),
    FL_ZRO((byte) (1 << 1)),
    FL_NEG((byte) (1 << 2)),
    ;

    private final byte flagCode;

    LC3ConditionFlag(byte flagCode) {
        this.flagCode = flagCode;
    }

    @Override
    public byte getFlagCode() {
        return flagCode;
    }

    public static LC3ConditionFlag valueOf(short value, byte instructionSize) {
        if(value == 0) {
            return LC3ConditionFlag.FL_ZRO;
        }
        else if(value >>> instructionSize - 1 == 0) {
            return LC3ConditionFlag.FL_POS;
        }
        else {
            return LC3ConditionFlag.FL_NEG;
        }
    }

    public static LC3ConditionFlag valueOfFlag(short flag) {
        return Arrays.stream(LC3ConditionFlag.values())
                .filter(lc3ConditionFlag -> lc3ConditionFlag.getFlagCode() == flag)
                .findFirst()
                .orElseThrow(() -> new VirtualMachineException(Integer.toHexString(flag) + " flag code is not defined"));
    }
}
