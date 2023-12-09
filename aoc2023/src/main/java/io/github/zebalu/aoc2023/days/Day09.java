package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day09 {
    public static void main(String[] args) {
        List<List<Long>> readLists = readInput().lines()
                .map(line -> Arrays.stream(line.split(" ")).map(Long::parseLong).toList()).toList();
        List<List<Long>> extendeds = readLists.stream().map(l -> new ArrayList<>(l)).map(l -> extend(l)).toList();
        System.out.println(extendeds.stream().mapToLong(l -> l.getLast()).sum());
        System.out.println(extendeds.stream().mapToLong(l -> l.getFirst()).sum());
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day09.txt").toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Long> extend(List<Long> original) {
        List<List<Long>> pyramid = new ArrayList<>();
        pyramid.add(original);
        while (!isAllZero(pyramid.getLast())) {
            List<Long> last = pyramid.getLast();
            List<Long> diffs = new ArrayList<>();
            for (int i = 1; i < last.size(); ++i) {
                diffs.add(last.get(i) - last.get(i - 1));
            }
            pyramid.add(diffs);
        }
        for (int i = pyramid.size() - 2; i >= 0; --i) {
            List<Long> toExtend = pyramid.get(i);
            List<Long> from = pyramid.get(i + 1);
            toExtend.add(from.getLast() + toExtend.getLast());
            toExtend.add(0, toExtend.getFirst() - from.getFirst());
        }
        return pyramid.getFirst();
    }

    private static boolean isAllZero(List<Long> list) {
        return list.stream().allMatch(l -> l == 0L);
    }

}