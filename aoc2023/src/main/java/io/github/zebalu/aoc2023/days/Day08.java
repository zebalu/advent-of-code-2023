package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day08 {
    public static void main(String[] args) {
        String input = readInput();
        String[] parts = input.split("\n\n");
        String instructions = parts[0];
        Map<String, LeftRight> map = new HashMap<>();
        parts[1].lines().forEach(l->addTo(map, l));
        part1(instructions, map);
        part2(instructions, map);
    }
    
    private static void part1(String instructions, Map<String, LeftRight> map) {
        System.out.println(pathLength(instructions, map, "AAA"));
    }

    private static void part2(String instructions, Map<String, LeftRight> map) {
        List<String> at = map.keySet().stream().filter(s->s.endsWith("A")).toList();
        List<Integer> steps = at.stream().map(s->pathLength(instructions, map, s)).toList();
        System.out.println(steps.stream().mapToLong(i->(long)i).reduce(1L, (a,b)->leastCommonMultiplier(a,b)));
    }
    
    private static int pathLength(String instructions, Map<String, LeftRight> map, String start) {
        int count = 0;
        int pointer = 0;
        String at = start;
        while(!at.endsWith("Z")) {
            ++count;
            char step = instructions.charAt(pointer);
            at = map.get(at).get(step);
            pointer = (pointer+1)%instructions.length();
        }
        return count;
    }
    
    public static long leastCommonMultiplier(long a, long b) {
        return (a * b) / greatestCommonDenominator(a, b);
    }
    
    public static long greatestCommonDenominator(long a, long b) {
        if (b == 0) {
            return a;
        } else {
            return (greatestCommonDenominator(b, a % b));
        }
    }
    
    private static void addTo(Map<String, LeftRight> map, String line) {
        var parts = line.split(" = ");
        map.put(parts[0], LeftRight.parse(parts[1]));
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day08.txt").toAbsolutePath());
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
    
    private record LeftRight(String left, String right) {
        static LeftRight parse(String str) {
            String[] parts = str.split(", ");
            return new LeftRight(parts[0].substring(1), parts[1].substring(0, parts[1].length()-1));
        }
        String get(char ch) {
            return switch(ch) {
            case 'L' -> left;
            case 'R' -> right;
            default -> throw new IllegalStateException("unknonwn: "+ch);
            };
        }
    }
}
