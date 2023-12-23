package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;

public class Day23 {
    
    public static void main(String[] args) {
        var maze = readInput().lines().toList();
        Coord start = new Coord(1,0);
        Coord target = new Coord(maze.size()-2, maze.size()-1);
        var longest = longestPathLength(maze, start, target, (m,n)->n.part1Neighbours(m), n->true);
        System.out.println(longest);
        WeightedGraph wg = new WeightedGraph(maze, start, target);
        System.out.println(wg.longestPath(start, target));
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day23.txt").toAbsolutePath());
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static SequencedSet<Coord> longestPath(List<String> maze, Coord start, Coord target, BiFunction<List<String>, Coord, List<Coord>> nextList, Predicate<Coord> filter) {
        SequencedSet<Coord> result = new LinkedHashSet<>();
        Stack<SequencedSet<Coord>> stack = new Stack<>();
        stack.add(new LinkedHashSet<>(Set.of(start)));
        while(!stack.isEmpty()) {
            SequencedSet<Coord> curr = stack.pop();
            if(target.equals(curr.getLast())) {
                if (result.size()<curr.size()) {
                    result = curr;
                }
            } else {
                nextList.apply(maze, curr.getLast()).stream().filter(n->filter.test(n) && !curr.contains(n)).forEach(n->{
                    var next = new LinkedHashSet<>(curr);
                    next.add(n);
                    stack.push(next);
                });
            }
        }
        return result;
    }
    
    private static int longestPathLength(List<String> maze, Coord start, Coord target, BiFunction<List<String>, Coord, List<Coord>> nextList, Predicate<Coord> filter) {
        return longestPath(maze, start, target, nextList, filter).size()-1;
    }

    private record Coord(int x, int y) {
        char extract(List<String> maze) {
            return maze.get(y).charAt(x);
        }
        
        char extractAny(List<String> maze) {
            if(y<0 || x<0 || maze.size()<y || maze.getFirst().length()<=x) {
                return '#';
            }
            return extract(maze);
        }
        
        Coord north() {
            return new Coord(x,y-1);
        }
        
        Coord south() {
            return new Coord(x,y+1);
        }
        
        Coord west() {
            return new Coord(x-1,y);
        }
        
        Coord east() {
            return new Coord(x+1,y);
        }
        
        List<Coord> part1Neighbours(List<String> maze) {
            List<Coord> result = new ArrayList<>();
            switch(extract(maze)) {
            case '.' -> {
                appendIfMatch(result, maze, north(), '^');
                appendIfMatch(result, maze, south(), 'v');
                appendIfMatch(result, maze, east(), '>');
                appendIfMatch(result, maze, west(), '<');
            }
            case 'v' -> result.add(south());
            case '^' -> result.add(north());
            case '<' -> result.add(west());
            case '>' -> result.add(east());
            case '#' -> throw new IllegalStateException("can not stand here: "+this);
            }
            return result;            
        }
        
        List<Coord> part2Neighbours(List<String> maze) {
            List<Coord> result = new ArrayList<>();
            switch(extract(maze)) {
            case '#' -> throw new IllegalStateException("can not stand here: "+this);
            default -> {
                appendIfNotMatch(result, maze, north(), '#');
                appendIfNotMatch(result, maze, south(), '#');
                appendIfNotMatch(result, maze, east(), '#');
                appendIfNotMatch(result, maze, west(), '#');
            }
            }
            return result;            
        }
        
        @Override
        public int hashCode() {
            return x*10_000+y;
        }
        
        private static void appendIfMatch(List<Coord> collector, List<String> maze, Coord coord, char accepted) {
            char at = coord.extractAny(maze);
            if(at == '.' || at == accepted) {
                collector.add(coord);
            }
        }
        
        private static void appendIfNotMatch(List<Coord> collector, List<String> maze, Coord coord, char rejected) {
            char at = coord.extractAny(maze);
            if(at == '.' || at != rejected) {
                collector.add(coord);
            }
        }
    }
    
    private static class WeightedGraph {
        private SequencedSet<Coord> forks = new LinkedHashSet<>();
        private SequencedMap<Coord, Integer> forkIds = new LinkedHashMap<>();
        private Map<Integer, BitSet> connections = new LinkedHashMap<Integer, BitSet>();
        private Map<Edge, Integer> costs = new LinkedHashMap<>();
        
        public WeightedGraph(List<String> maze, Coord start, Coord target) {
            forks.add(start);
            int id = 0;
            forkIds.put(start, id);
            for(int y=1; y<maze.size()-1; ++y) {
                String line = maze.get(y);
                for(int x=1; x<line.length()-1; ++x) {
                    char ch = line.charAt(x);
                    if(ch != '#') {
                        Coord c = new Coord(x,y);
                        if(c.part2Neighbours(maze).size()>2) {
                            forks.add(c);
                            forkIds.put(c, ++id);
                        }
                    }
                }
            }
            forks.add(target);
            forkIds.put(target, ++id);
            for(int i=0; i<forkIds.size(); ++i) {
                connections.put(i, new BitSet(forks.size()));
            }
            for(Entry<Coord, Integer> iEntry: forkIds.entrySet()) {
                for(Entry<Coord, Integer> jEntry: forkIds.entrySet()) {
                    if(iEntry.getValue() < jEntry.getValue()) {
                        int length = longestPathLength(maze, iEntry.getKey(), jEntry.getKey(), (m,n)->n.part2Neighbours(m), c->!forks.contains(c) || c.equals(jEntry.getKey()));
                        if(length > 0) {
                            connections.get(iEntry.getValue()).set(jEntry.getValue());
                            connections.get(jEntry.getValue()).set(iEntry.getValue());
                            costs.put(new Edge(iEntry.getValue(), jEntry.getValue()), length);
                            costs.put(new Edge(jEntry.getValue(), iEntry.getValue()), length);
                        }
                    }
                }
            }
        }
        
        int longestPath(Coord start, Coord target) {
            int startId = forkIds.get(start);
            int targetId = forkIds.get(target);
            Stack<Step> stack = new Stack<>();
            BitSet startBs = new BitSet(forks.size());
            startBs.set(forkIds.get(start));
            stack.add(new Step(startId, startBs, 0));
            int[] longest = new int[] {0};
            while(!stack.isEmpty()) {
                Step curr = stack.pop();
                BitSet cons = connections.get(curr.last);
                cons.stream().forEach(i -> {
                    if (cons.get(i) && !curr.history.get(i)) {
                        int newCost = curr.length + costs.get(new Edge(curr.last, i));
                        if (i == targetId && longest[0] < newCost) {
                            longest[0] = newCost;
                        } else {
                            BitSet nextSet = (BitSet) curr.history.clone();
                            nextSet.set(i);
                            Step nextStep = new Step(i, nextSet, newCost);
                            stack.push(nextStep);
                        }
                    }
                });
            }
            return longest[0];
        }
        
        private record Step(int last, BitSet history, int length) {}
        private record Edge(int from, int to) {
            @Override
            public int hashCode() {
                return from*10_000+to;
            }
        }
    }
}
