package com.cauldronjs.sourcemap;

import java.util.ArrayList;

public class Base64VqlDecoder {
    public static Integer[] decode(String input) {
        var result = new ArrayList<>();
        var provider = new Base64CharProvider(input);
        while (!provider.isEmpty()) {
            result.add(decodeNextInt(provider));
        }
        return result.toArray(new Integer[0]);
    }

    private static int decodeNextInt(Base64CharProvider provider) {
        var result = 0;
        boolean continuation;
        var shift = 0;
        do {
            var c = provider.getNextChar();
            var digit = Base64Converter.fromBase64(c);
            continuation = (digit & Constants.vlqContinuationBit) != 0;
            digit &= Constants.vlqBaseMask;
            result += digit << shift;
            shift += Constants.vlqBaseShift;
        } while (continuation);
        return fromVqlSigned(result);
    }

    private static int fromVqlSigned(int value) {
        return (int)Integer.toUnsignedLong(value);
    }

    private static class Base64CharProvider {
        final String backingString;
        int currentIndex = 0;

        public Base64CharProvider(String s) {
            this.backingString = s;
        }

        public char getNextChar() {
            return this.backingString.charAt(this.currentIndex++);
        }

        public boolean isEmpty() {
            return this.currentIndex >= this.backingString.length();
        }
    }
}
