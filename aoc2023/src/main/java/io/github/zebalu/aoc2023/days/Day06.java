package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class Day06 {
    public static void main(String[] args) {
        List<Race> races = Race.fromLines(readInput());
        
        System.out.println(races.stream().map(Race::countRecordBreakingCases).reduce(1L, (a,b)->a*b));
        
        Race gigaRace = Optional
                .of(races.stream().map(r -> new String[] { "" + r.time, "" + r.distance })
                        .reduce(new String[] { "", "" }, (a, b) -> new String[] { a[0] + b[0], a[1] + b[1] }))
                .map(s -> new long[] { Long.parseLong(s[0]), Long.parseLong(s[1]) }).map(ls -> new Race(ls[0], ls[1]))
                .orElseThrow();

        System.out.println(gigaRace.countRecordBreakingCases());
    }
    
    record Race(long time, long distance) {
        long countRecordBreakingCases() {
            var sqrt_b2_4ac = Math.sqrt(time * time - 4 * distance);
            var res1 = (-time - sqrt_b2_4ac) / -2.0;
            var res2 = (-time + sqrt_b2_4ac) / -2.0;
            var min = (long) Math.ceil(Math.min(res1, res2));
            var max = (long) Math.ceil(Math.max(res1, res2));
            return max - min;
        }
        static List<Race> fromLines(List<String> lines) {
            List<List<Integer>> numList = lines.stream().map(l->l.replaceAll(" +", " ")).map(Race::parseNums).toList();
            return IntStream.range(0, numList.getFirst().size()).mapToObj(i->new Race(numList.get(0).get(i), numList.get(1).get(i))).toList();
        }
        private static List<Integer> parseNums(String stripped) {
            return Arrays.stream(stripped.split(" ")).skip(1).map(Integer::parseInt).toList();
        }
    }

    private static List<String> readInput() {
        try {
            return Files.readAllLines(Path.of("day06.txt").toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
