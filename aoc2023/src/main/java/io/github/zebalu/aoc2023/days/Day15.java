package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day15 {
    public static void main(String[] args) {
        String input = readInput(); // example;//readInput();
        var list = Arrays.stream(input.strip().split(",")).toList();
        System.out.println(list.stream().mapToInt(Day15::hash).sum());
        var boxes = calculateHashMap(list.stream().map(String::strip).map(Operation::fromString).toList());
        int sum = calculateSum(boxes);
        System.out.println(sum);
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day15.txt").toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int calculateSum(List<List<Operation>> boxes) {
        int sum = 0;
        for (int i = 0; i < boxes.size(); ++i) {
            var box = boxes.get(i);
            for (int j = 0; j < box.size(); ++j) {
                var o = box.get(j);
                sum += (i + 1) * (j + 1) * o.focal();
            }
        }
        return sum;
    }

    private static List<List<Operation>> calculateHashMap(List<Operation> ops) {
        List<List<Operation>> boxes = new ArrayList<>(256);
        for (int i = 0; i < 256; ++i) {
            boxes.add(new ArrayList<>());
        }
        ops.forEach(o -> doMap(o, indexOf(boxes.get(o.hash()), o), boxes.get(o.hash())));
        return boxes;
    }

    private static void doMap(Operation o, int i, List<Operation> box) {
        if (i >= 0) {
            if (o.add()) {
                box.set(i, o);
            } else {
                box.remove(i);
            }
        } else if (o.add()) {
            box.add(o);
        }
    }

    private static int indexOf(List<Operation> box, Operation o) {
        for (int i = 0; i < box.size(); ++i) {
            if (o.label().equals(box.get(i).label())) {
                return i;
            }
        }
        return -1;
    }

    private static int hash(String str) {
        int h = 0;
        for (int i = 0; i < str.length(); ++i) {
            h = ((h + str.charAt(i)) * 17) % 256;
        }
        return h;
    }

    private record Operation(String label, int focal, boolean add) {
        static Operation fromString(String desc) {
            if (desc.endsWith("-")) {
                return new Operation(desc.substring(0, desc.length() - 1), Integer.MIN_VALUE, false);
            }
            var parts = desc.split("=");
            return new Operation(parts[0], Integer.parseInt(parts[1]), true);
        }

        int hash() {
            return Day15.hash(label);
        }
    }

}
