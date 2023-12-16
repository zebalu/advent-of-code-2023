package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;
import java.util.stream.IntStream;

public class Day12 {
    public static void main(String[] args) {
        var as = readInput().lines().map(Arrangement::parse).toList();
        System.out.println(as.stream().parallel().mapToLong(a -> count(a, 0, 0, new HashMap<>())).sum());
        System.out.println(as.stream().map(a -> a.unFold(5)).parallel().mapToLong(a -> count(a, 0, 0, new HashMap<>())).sum());
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day12.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private record Arrangement(char[] desc, List<Integer> nums) {
        static Arrangement parse(String line) {
            var parts = line.split(" ");
            return new Arrangement(parts[0].toCharArray(),
                    Arrays.stream(parts[1].split(",")).map(Integer::parseInt).toList());
        }

        Arrangement unFold(int folds) {
            char[] nc = new char[desc.length * folds + folds - 1];
            List<Integer> newNums = new ArrayList<>();
            int k = 0;
            for (int i = 0; i < folds; ++i) {
                for (int j = 0; j < desc.length; ++j) {
                    nc[k++] = desc[j];
                }
                newNums.addAll(nums);
                if (i < folds - 1) {
                    nc[k++] = '?';
                }
            }
            return new Arrangement(nc, newNums);
        }

        int countRequiredLengthFrom(int group) {
            return IntStream.range(group, nums.size()).map(i -> nums.get(i) + 1).sum();
        }
    }

    // credit: https://github.com/p-kovacs/advent-of-code-2023/blob/master/src/main/java/com/github/pkovacs/aoc/y2023/Day12.java
    private static long count(Arrangement arrangement, int fieldIndex, int groupIndex, Map<Long, Long> cache) {
        long cacheKey = toKey(fieldIndex, groupIndex);
        if (cache.containsKey(cacheKey)) {
            return cache.get(cacheKey);
        }
        if (groupIndex == arrangement.nums().size()) {
            return noneMatch(arrangement.desc(), fieldIndex, arrangement.desc().length, '#') ? 1 : 0;
        }
        long count = 0;
        int length = arrangement.nums().get(groupIndex);
        int maxPos = arrangement.desc().length - arrangement.countRequiredLengthFrom(groupIndex) + 1;
        boolean canContinue = true;
        for (int i = fieldIndex; i <= maxPos && canContinue; i++) {
            if (i > fieldIndex && arrangement.desc()[i - 1] == '#') {
                canContinue = false;
            } else if (noneMatch(arrangement.desc(), i, i + length, '.')
                    && (i == arrangement.desc().length - length || arrangement.desc()[i + length] != '#')) {
                count += count(arrangement, i + length + 1, groupIndex + 1, cache);
            }
        }
        cache.put(cacheKey, count);
        return count;
    }

    private static boolean noneMatch(char[] array, int from, int to, char ch) {
        for (int i = from; i < to; i++) {
            if (array[i] == ch) {
                return false;
            }
        }
        return true;
    }

    private static long toKey(long fieldIndex, long groupIndex) {
        return (fieldIndex << 32) | groupIndex;
    }
}
