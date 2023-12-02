package io.github.zebalu.aoc2023.days;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Day01 {

    private static final Map<String, Integer> DIGIT_NAMES = Map.of("one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
            "six", 6, "seven", 7, "eight", 8, "nine", 9);

    public static void main(String[] args) {
        var lines = readLines();
        var sum1 = lines.stream().map(Day01::keepDigits).mapToInt(l -> l.getFirst() * 10 + l.getLast()).sum();
        System.out.println(sum1);
        var sum2 = lines.stream().map(Day01::keepAllDigits).mapToInt(l -> l.getFirst() * 10 + l.getLast()).sum();
        System.out.println(sum2);
    }

    private static List<Integer> keepDigits(String s) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if ('1' <= c && c <= '9') {
                res.add(c - '0');
            }
        }
        return res;
    }

    private static List<Integer> keepAllDigits(String s) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if ('1' <= c && c <= '9') {
                res.add(Integer.parseInt("" + c));
            } else {
                var ss = s.substring(i);
                DIGIT_NAMES.keySet().stream().filter(k -> ss.startsWith(k)).map(k -> DIGIT_NAMES.get(k)).findAny()
                        .ifPresent(res::add);
            }
        }
        return res;
    }

    private static List<String> readLines() {
        try (BufferedReader br = new BufferedReader(
                new FileReader(new File("day01.txt").getAbsoluteFile(), StandardCharsets.UTF_8))) {
            return br.lines().toList();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
