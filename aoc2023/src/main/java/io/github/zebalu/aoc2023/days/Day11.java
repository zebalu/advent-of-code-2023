package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        
        List<List<Character>> expandable = new ArrayList<>();
        
        List<Integer> countInRow = new ArrayList<>();
        List<Integer> countInCol = new ArrayList<>();
        
        public Universe(String baseMap) {
            baseMap.lines().map(l->l.toCharArray()).map(chs ->{
                List<Character> lchs = new ArrayList<>();
                for(var ch : chs) {
                    lchs.add(ch);
                }
                return lchs;
            }).forEach(expandable::add);
            for(int y=0; y<expandable.size(); ++y) {
                countInRow.add(countInRow(y));
            }
            for(int x=0; x<expandable.getFirst().size(); ++x) {
                countInCol.add(countInColum(x));
            }
        }
        
        List<Coord> getGalaxies() {
            List<Coord> galaxies = new ArrayList<>();
            for(int y=0; y<expandable.size(); ++y) {
                for(int x=0; x<expandable.get(y).size(); ++x) {
                    if(expandable.get(y).get(x) == '#') {
                        galaxies.add(new Coord(x,y));
                    }
                }
            }
            return galaxies;
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
        
        private long walkOn(List<Integer> count, int a, int b, int mul) {
            int f = Math.min(a, b);
            int s = Math.max(a, b);
            long sum = 0L;
            for(int i=f; i<s; ++i) {
                if(count.get(i) == 0) {
                    sum += mul;
                } else {
                    sum += 1;
                }
            }
            return sum;
        }
        
        private int countInColum(int x) {
            return expandable.stream().map(l->l.get(x)).mapToInt(c->'#'==c?1:0).sum();
        }
        
        private int countInRow(int y) {
            return expandable.get(y).stream().mapToInt(c->'#'==c?1:0).sum();
        }
        
    }

}
