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
        
        WeightedGraph part1Graph = new WeightedGraph(maze, start, target, (m,c)->c.part1Neighbours(m));
        WeightedGraph part2Graph = new WeightedGraph(maze, start, target, (m,c)->c.part2Neighbours(m));
       
        System.out.println(part1Graph.longestPath(start, target));
        System.out.println(part2Graph.longestPath(start, target));
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day23.txt").toAbsolutePath());
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static Map<Coord, Integer> longestPathToAny(List<String> maze, Coord start, Map<Coord, Integer> idMap, BiFunction<List<String>, Coord, List<Coord>> nextList) {
        Map<Coord, Integer> result = new HashMap<>();
        Stack<SequencedSet<Coord>> stack = new Stack<>();
        stack.add(new LinkedHashSet<>(Set.of(start)));
        while(!stack.isEmpty()) {
            SequencedSet<Coord> curr = stack.pop();
            if(idMap.containsKey(curr.getLast()) && !curr.getLast().equals(start)) {
                result.compute(curr.getLast(), (k,v)->(v == null) ? curr.size()-1 : Math.max(v, curr.size()-1));
            } else {
                nextList.apply(maze, curr.getLast()).stream().filter(n->!curr.contains(n)).forEach(n->{
                    var next = new LinkedHashSet<>(curr);
                    next.add(n);
                    stack.push(next);
                });
            }
        }
        return result;
    }
    
    private record Coord(int x, int y) {
        char extract(List<String> maze) {
            return maze.get(y).charAt(x);
        }
        
        char extractAny(List<String> maze) {
            if(y<0 || x<0 || maze.size()<=y || maze.getFirst().length()<=x) {
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
        private List<Integer>[] connections; 
        private int[][] costs;
        
        @SuppressWarnings("unchecked")
        public WeightedGraph(List<String> maze, Coord start, Coord target, BiFunction<List<String>, Coord, List<Coord>> walkableNeighbourExtractor) {
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
            connections = new List[forkIds.size()];
            costs = new int[forkIds.size()][];
            for(int i=0; i<forkIds.size(); ++i) {
                //connections[i] = 0L; //.put(i, new BitSet(forks.size()));
                connections[i] = new ArrayList<Integer>();
                costs[i] = new int[forkIds.size()];
            }
            List<Entry<Coord, Integer>> entries = new ArrayList<>(forkIds.entrySet());
            for(int i=0; i<entries.size(); ++i) {
                Entry<Coord, Integer> iEntry = entries.get(i);
                Map<Coord, Integer> pathes = longestPathToAny(maze, iEntry.getKey(), forkIds, walkableNeighbourExtractor);
                for(var distE: pathes.entrySet()) {
                    int distId = forkIds.get(distE.getKey());
                    connections[iEntry.getValue()].add(distId);
                    costs[iEntry.getValue()][distId]=distE.getValue();
                }
            }
        }
        
        int longestPath(Coord start, Coord target) {
            int startId = forkIds.get(start);
            int targetId = forkIds.get(target);
            return longestPath(startId, targetId, 0, 0L);
        }
        
        
        int longestPath(int startId, int targetId, int dist, long visited) {
            int longest = Integer.MIN_VALUE;
            for(int c : connections[startId]) {
                long v = 1L << c;
                if ((visited&v) == 0L) {
                    int newCost = dist + costs[startId][c];
                    if (c == targetId) {
                        longest = Math.max(longest, newCost);
                    } else {
                        longest = Math.max(longest, longestPath(c, targetId, newCost, visited | v));
                    }
                }
            }
            return longest;
        }
    }
}
