package com.peersafe.base.java8.utils;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Arrays;

public class Print {
	/**
	 * 
	 * @param args
	 */
    public static void print(Object... args) {
        print(System.out, args);
    }
    /**
     * 
     * @param args
     */
    public static void printErr(Object... args) {
        print(System.err, args);
    }
    /**
     * 
     * @param to
     * @param character
     * @param length
     */
    public static void printRepeat(PrintStream to, char character, int length) {
        String repeated = repeated(character, length);
        to.println(repeated);
    }
    /**
     * 
     * @param character
     * @param length
     * @return String value
     */
    public static String repeated(char character, int length) {
        char[] data = new char[length];
        Arrays.fill(data, character);
        return new String(data);
    }
    /**
     * 
     * @param character
     * @param length
     */
    public static void printRepeat(char character, int length) {
        printRepeat(System.out, character, length);
    }
    /**
     * 
     * @param to
     * @param args
     */
    public static void print(PrintStream to, Object... args) {
        if (args.length == 0) {
            to.println();
        } else {
            String fmt = args[0] + "\n";
            Object[] fmtArgs = new Object[args.length - 1];
            if (fmtArgs.length > 0) {
                System.arraycopy(args, 1, fmtArgs, 0, fmtArgs.length);
            }
            to.print(MessageFormat.format(fmt, fmtArgs));
        }
    }
}
