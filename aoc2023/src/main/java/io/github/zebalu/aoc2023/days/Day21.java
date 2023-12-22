package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;

/**
 * Based on the formula from: https://github.com/abnew123/aoc2023/blob/main/src/solutions/Day21.java
 */
public class Day21 {
    public static void main(String[] args) {
        String input = readInput();
        List<String> maze = input.lines().toList();
        Coord start = null;
        for(int y=0; y<maze.size() && start == null; ++y) {
            String line = maze.get(y);
            for(int x=0; x<line.length() && start == null; ++x) {
                if(line.charAt(x)=='S') {
                    start = new Coord(x,y);
                }
            }
        }
        System.out.println(withDeltaTracking(start, maze, 64, false));
        System.out.println(withDeltaTracking(start, maze, 26501365, true));
    }
    
    private static long withDeltaTracking(Coord start, List<String> maze, int maxSteps, boolean isInfiniteMap) {
        long getOutCount = stepsToGetOut(start, maze, c->c.y==0);
        Set<Coord> reached = new HashSet<>();
        List<Long> totals = new ArrayList<>();
        List<Long> deltas = new ArrayList<>();
        List<Long> deltaDeltas = new ArrayList<>();
        reached.add(start);
        List<Coord> todo = List.of(start);
        boolean filled = false;
        long totalReached = maxSteps%2 == 0 ? 1L : 0L;
        int index = 0;
        while(index<maxSteps && !filled) {
            ++index;
            todo = todo.stream().flatMap(c->c.neighbours().stream()).filter(c-> {
                if(isInfiniteMap) {
                    return c.extractInfinit(maze) != '#' && !reached.contains(c);
                } else {
                    return c.isValid(maze) && c.extract(maze) != '#' && !reached.contains(c);
                 }
            }).peek(reached::add).toList();
            if ( index % 2 == maxSteps %2) {
                totalReached += todo.size();
                if ((index % maze.size()) == getOutCount) {
                    totals.add(totalReached);
                    if(totals.size() > 1){
                        deltas.add(totals.getLast() - totals.get(totals.size() - 2));
                    };
                    if(deltas.size() > 1){
                        deltaDeltas.add(deltas.getLast() - deltas.get(deltas.size() - 2));
                    }
                    if(deltaDeltas.size() > 1){
                        filled = true;
                    }
                }
            }
        }
        if (filled) {
            long neededLoopCount = maxSteps / (maze.size() * 2) - 1;
            long currentLoopCount = index / (maze.size() * 2) - 1;
            long deltaLoopCount = neededLoopCount - currentLoopCount;
            long deltaLoopCountTriangular = (neededLoopCount * (neededLoopCount + 1)) / 2
                    - (currentLoopCount * (currentLoopCount + 1)) / 2;
            long deltaDelta = deltaDeltas.getLast();
            long initialDelta = deltas.getFirst();
            return deltaDelta * deltaLoopCountTriangular + initialDelta * deltaLoopCount + totalReached;
        }
        return totalReached;
    }
    
    private static long stepsToGetOut(Coord start, List<String> maze, Predicate<Coord> isOut) {
        long count = 0L;
        Set<Coord> available = new HashSet<>();
        Set<Coord> nextAvailable = new HashSet<>();
        available.add(start);
        while(!available.stream().anyMatch(isOut)) {
            available.stream().flatMap(n->n.neighbours().stream()).filter(n->n.isValid(maze)).forEach(nextAvailable::add);
            available = nextAvailable;
            nextAvailable = new HashSet<>();
            ++count;
        }
        return count;
    }
    private static String readInput() {
        try {
            return Files.readString(Path.of("day21.txt").toAbsolutePath());
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
    private record Coord(int x, int y) implements Comparable<Coord> {
        private static final Comparator<Coord> COMPARATOR = Comparator.comparingInt(Coord::x).thenComparing(Coord::y);
        List<Coord> neighbours() {
            return List.of(new Coord(x-1,y), new Coord(x+1, y), new Coord(x, y-1), new Coord(x, y+1));
        }
        boolean isValid(List<String> maze) {
            return x>=0 && y>=0 && y<maze.size() && x<maze.get(y).length();
        }
        char extract(List<String> maze) {
            return maze.get(y).charAt(x);
        }
        char extractInfinit(List<String> maze){
            int fy = ((y % maze.size()) + maze.size()) % maze.size();
            int fx = ((x % maze.getFirst().length()) + maze.getFirst().length()) % maze.getFirst().length();
            return maze.get(fy).charAt(fx);
        }
        @Override
        public int hashCode() {
            return x*100_000+y;
        }
        @Override
        public int compareTo(Coord o) {
            return COMPARATOR.compare(this, o);
        }
        
    }
}
