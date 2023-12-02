/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.zebalu.aoc2023;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class App {

    public static void main(String[] args) {
        exec(new DisplayData(1, "Trebuchet?!", Day01::main));
        exec(new DisplayData(2, "Cube Conundrum", Day02::main));
    }

    private static void exec(DisplayData dd) {
        String title = String.format(" Day %02d: %s ", dd.number(), dd.name());
        StringBuffer titleLine = new StringBuffer();
        titleLine.append(repeat("#", (80 - title.length()) / 2));
        titleLine.append(title);
        titleLine.append(repeat("#", 80 - titleLine.length()));
        System.out.println(titleLine.toString());
        PrintStream save = System.out;
        MeasuringPrintStream measuringStream = new MeasuringPrintStream(save);
        System.setOut(measuringStream);
        try {
            Instant start = Instant.now();
            dd.main().accept(new String[] {});
            Instant end = Instant.now();
            System.setOut(save);
            System.out.println(repeat("-", 80));
            System.out.println(
                    "part1 time:\t" + Duration.between(start, measuringStream.times.getFirst()).toMillis() + " ms");
            System.out.println("part2 time:\t"
                    + Duration.between(measuringStream.times.getFirst(), measuringStream.times.getLast()).toMillis()
                    + " ms");
            System.out.println("full time:\t" + Duration.between(start, end).toMillis() + " ms");
            System.out.println(repeat("*", 80));
        } finally {
            System.setOut(save);
        }
    }

    private static String repeat(String data, int times) {
        return String.join("", Collections.nCopies(times, data));
    }

    record DisplayData(int number, String name, Consumer<String[]> main) {
    }

    private static class MeasuringPrintStream extends PrintStream {
        private final List<Instant> times = new ArrayList<>();

        public MeasuringPrintStream(OutputStream out) {
            super(out);
        }

        @Override
        public void println(int i) {
            times.add(Instant.now());
            super.println(i);
        }

        public void println(long L) {
            times.add(Instant.now());
            super.println(L);
        }

        public void println(double d) {
            times.add(Instant.now());
            super.println(d);
        }

        public void println(String s) {
            times.add(Instant.now());
            super.println(s);
        }

        public void println(Object o) {
            times.add(Instant.now());
            super.println(o);
        }
    }
}