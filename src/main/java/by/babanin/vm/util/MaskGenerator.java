package by.babanin.vm.util;

public class MaskGenerator {

    public static long generateMask(final int size, final int startZeroRange, final int endZeroRange) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < size; i++) {
            builder.append(i >= startZeroRange && i < endZeroRange ? 0 : 1);
        }
        return Utils.parseLong(builder.toString(), 2);
    }
}
