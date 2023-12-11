package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Day11 {
    public static void main(String[] args) {
        String input = readInput();
        Universe u = new Universe(input);
        List<Coord> galaxies = u.getGalaxies();
        part1(u, galaxies);
        part2(u, galaxies);
    }

    private static void part1(Universe universe, List<Coord> galaxies) {
        System.out.println(sumWithExpand(universe, galaxies, 2));
    }
    
    private static void part2(Universe universe, List<Coord> galaxies) {
        System.out.println(sumWithExpand(universe, galaxies, 1_000_000));
    }
    
    private static long sumWithExpand(Universe universe, List<Coord> galaxies, int expand) {
        long sumDist = 0L;
        for(int i=0; i<galaxies.size()-1; ++i) {
            for(int j=i+1; j<galaxies.size(); ++j) {
                Coord a = galaxies.get(i);
                Coord b = galaxies.get(j);
                sumDist += universe.pathFrom(a, b, expand);
            }
        }
        return sumDist;
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day11.txt").toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Coord(int x, int y) {}
    
    private static class Universe {
        
        private final List<String> expandable;
        
        private final List<Integer> countInRow = new ArrayList<>();
        private final List<Integer> countInCol = new ArrayList<>();
        
        public Universe(String baseMap) {
            expandable = baseMap.lines().toList();
            for(int y=0; y<expandable.size(); ++y) {
                countInRow.add(countInRow(y));
            }
            for(int x=0; x<expandable.getFirst().length(); ++x) {
                countInCol.add(countInColum(x));
            }
        }
        
        List<Coord> getGalaxies() {
            return IntStream.range(0, expandable.size())
                    .mapToObj(y -> IntStream.range(0, expandable.getFirst().length())
                            .filter(x -> expandable.get(y).charAt(x) == '#').mapToObj(x -> new Coord(x, y)))
                    .flatMap(s -> s).toList();
        }
        
        long pathFrom(Coord a, Coord b, int mul) {
            return walkRow(a.x, b.x, mul) + walkCol(a.y,b.y,mul);
        }
        
        private long walkRow(int a, int b, int mul) {
            return walkOn(countInCol, a, b, mul);
        }
        
        private long walkCol(int a, int b, int mul) {
            return walkOn(countInRow, a, b, mul);
        }
        
        private long walkOn(List<Integer> count, int a, int b, long mul) {
            return IntStream.range(Math.min(a, b), Math.max(a, b)).mapToLong(i->count.get(i)==0?mul:1L).sum();
        }
        
        private int countInColum(int x) {
            return expandable.stream().map(l->l.charAt(x)).mapToInt(c->'#'==c?1:0).sum();
        }
        
        private int countInRow(int y) {
            return expandable.get(y).chars().map(c->'#'==c?1:0).sum();
        }
        
    }

}
