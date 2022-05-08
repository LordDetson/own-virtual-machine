package by.babanin.vm.util;

public class Utils {

    public static long parseLong(final String s, final int radix) throws NumberFormatException {
        if(s == null) {
            throw new NumberFormatException("null");
        }

        if(radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }
        if(radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }

        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;

        if(len > 0) {
            char firstChar = s.charAt(0);
            if(firstChar < '0') { // Possible leading "+" or "-"
                if(firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                }
                else if(firstChar != '+') {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }

                if(len == 1) { // Cannot have lone "+" or "-"
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                i++;
            }
            long result = 0;
            while(i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i++), radix);
                result *= radix;
                if(result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result -= digit;
            }
            return negative ? result : -result;
        }
        else {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }
    }

    public static int parseInt(String s, int radix)
            throws NumberFormatException {
        /*
         * WARNING: This method may be invoked early during VM initialization
         * before IntegerCache is initialized. Care must be taken to not use
         * the valueOf method.
         */

        if(s == null) {
            throw new NumberFormatException("null");
        }

        if(radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }

        if(radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }

        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;

        if(len > 0) {
            char firstChar = s.charAt(0);
            if(firstChar < '0') { // Possible leading "+" or "-"
                if(firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                }
                else if(firstChar != '+') {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }

                if(len == 1) { // Cannot have lone "+" or "-"
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                i++;
            }
            int result = 0;
            while(i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i++), radix);
                result *= radix;
                if(result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + s + "\"");
                }
                result -= digit;
            }
            return negative ? result : -result;
        }
        else {
            throw new NumberFormatException("For input string: \"" + s + "\"");
        }
    }
}
