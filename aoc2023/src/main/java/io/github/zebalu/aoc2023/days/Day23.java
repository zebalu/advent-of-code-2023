package io.github.zebalu.aoc2023.days;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Day23 {
    public static void main(String[] args) {
        String input = readInput(); //example; //readInput();
        System.out.println(input);
        List<String> maze =  input.lines().toList();
        Coord start = new Coord(1,0);
        Coord target = new Coord(maze.getLast().length()-2, maze.size()-1);
        System.out.println(longestPath(maze, start, target));
        WeightedGraph wg = new WeightedGraph(maze, start, target);
        System.out.println(wg.longestPath(start, target));
    }
    
    private static long longestPath(List<String> maze, Coord start, Coord target) {
        Queue<StepCount> queue = new PriorityQueue<>(StepCount.LONGEST_COMPARTOR);
        Map<Coord, Integer> steps = new HashMap<>();
        queue.add(new StepCount(start, null, 0));;
        steps.put(start, 0);
        long longest = 0L;
        while(!queue.isEmpty()) {
            StepCount at = queue.poll();
            int nextCount = at.count()+1;
            at.coord().next(maze).stream().filter(n->!n.equals(at.prev) && n.isValid(maze) && (!steps.containsKey(n)||steps.get(n) < nextCount) &&n.extract(maze) != '#').forEach(n->{
                queue.add(new StepCount(n, at.coord(), nextCount));
                steps.put(n, nextCount);
            });
            if(target.equals(at.coord())) {
                longest = Math.max(longest, at.count());
            }
        }
        return longest;
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day23.txt").toAbsolutePath());
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    record Coord(int x, int y) {
        boolean isValid(List<String> maze) {
            return 0<=x && 0 <= y && x<maze.getFirst().length() && y < maze.size();
        }
        char extract(List<String> maze) {
            return maze.get(y).charAt(x);
        }
        List<Coord> neighbours() {
            return List.of(new Coord(x-1, y), new Coord(x+1, y), new Coord (x, y-1), new Coord(x, y+1));
        }
        List <Coord> next(List<String> maze) {
            return switch(extract(maze)) {
            case '^' -> List.of(new Coord(x, y-1));
            case 'v' -> List.of(new Coord(x, y+1));
            case '>' -> List.of(new Coord(x+1, y));
            case '<' -> List.of(new Coord(x-1, y));
            case '.' -> neighbours();
            default -> throw new IllegalStateException("can not start from: "+extract(maze));
            };
        }
    }
    
    record StepCount(Coord coord, Coord prev, int count) {
        static Comparator<StepCount> LONGEST_COMPARTOR = Comparator.comparingInt(StepCount::count).reversed();
    }
    
    record StepCount2(Coord coord, Set<Coord> prevs, int count) {
        static Comparator<StepCount2> LONGEST_COMPARTOR = Comparator.comparingInt(StepCount2::count).reversed();
    }
    
    private static class WeightedGraph {
        private Set<Coord> junktions = new LinkedHashSet<>();
        private Map<Coord, Set<Coord>> directConnections = new LinkedHashMap<>();
        private Map<CostPair, Integer> costs = new HashMap<>();
        WeightedGraph(List<String> maze, Coord start, Coord target) {
            junktions.add(start);
            junktions.add(target);
            for(int y=0; y<maze.size(); ++y) {
                String line = maze.get(y);
                for(int x=0; x<line.length(); ++x) {
                    char ch = line.charAt(x);
                    if(ch!='#') {
                        Coord at = new Coord(x,y);
                        long validNextSteps = at.neighbours().stream().filter(n->n.isValid(maze) && n.extract(maze)!='#').count();
                        if(validNextSteps>2) {
                            junktions.add(at);
                        }
                    }
                }
            }
            fillDirectConnections(maze);
        }
        
        private void fillDirectConnections(List<String> maze) {
            List<Coord> junktionList = new ArrayList<>(junktions);
            for(int i=0; i<junktionList.size(); ++i) {
                Coord from = junktionList.get(i);
                Set<Coord> available = new HashSet<>();
                for(int j=i+1; j<junktionList.size(); ++j) {
                    Coord to = junktionList.get(j);
                    CostPair cp1 = new CostPair(from, to);
                    CostPair cp2 = new CostPair(to, from);
                    int path = longestMazePathWithoutOtherJunktions(maze, junktionList.get(i), junktionList.get(j));
                    if(path>0) {
                        available.add(to);
                        costs.put(cp1, path);
                        costs.put(cp2, path);
                        Set<Coord> toSet = directConnections.computeIfAbsent(to, k->new HashSet<>());
                        toSet.add(from);
                    }
                }
                var stored = directConnections.computeIfAbsent(from, k->new HashSet<>());
                stored.addAll(available);
            }
        }
        
        private int longestMazePathWithoutOtherJunktions(List<String> maze, Coord start, Coord target) {
            Queue<StepCount2> queue = new PriorityQueue<>(StepCount2.LONGEST_COMPARTOR);
            Map<Coord, Integer> steps = new HashMap<>();
            queue.add(new StepCount2(start, Set.of(start), 0));;
            steps.put(start, 0);
            int longest = -1;
            while(!queue.isEmpty()) {
                StepCount2 at = queue.poll();
                int nextCount = at.count()+1;
                if(target.equals(at.coord())) {
                    longest = Math.max(longest, at.count());
                } else {
                at.coord().neighbours().stream().filter(n->!at.prevs().contains(n) && n.isValid(maze) && n.extract(maze) != '#' && (!junktions.contains(n) || target.equals(n))
                        &&(!steps.containsKey(n) || steps.get(n)<nextCount)).forEach(n->{
                    Set<Coord> nps = new HashSet<>(at.prevs);
                    nps.add(at.coord);
                    queue.add(new StepCount2(n, nps, nextCount));
                    steps.put(n, nextCount);
                });
                }
            }
            return longest;
        }
        
        long longestPath(Coord start, Coord target) {
            Queue<StepCount2> queue = new LinkedList<>(); //new PriorityQueue<>(StepCount2.LONGEST_COMPARTOR);
            Map<Coord, Integer> steps = new HashMap<>();
            queue.add(new StepCount2(start, Set.of(start), 0));;
            steps.put(start, 0);
            long longest = -1L;
            while(!queue.isEmpty()) {
                StepCount2 at = queue.poll();
                directConnections.get(at.coord()).stream().filter(n->!at.prevs().contains(n)
                        &&(!steps.containsKey(n) || steps.get(n) < at.count+costs.get(new CostPair(at.coord, n)))).forEach(n->{
                    Set<Coord> nps = new HashSet<>(at.prevs);
                    nps.add(at.coord);
                    queue.add(new StepCount2(n, nps, at.count+costs.get(new CostPair(at.coord, n))));
                    //steps.put(n, at.count+costs.get(new CostPair(at.coord, n)));
                });
                if(target.equals(at.coord())) {
                    longest = Math.max(longest, at.count());
                    //System.out.println("at target: "+longest);
                }
            }
            return longest;
        }
        
        private record CostPair(Coord from, Coord to) {
            
        }
        
    }
    /*
    private static class WeightedGraph1 {
        private Set<Coord> junktions = new LinkedHashSet<>();
        private Map<Coord, Set<Coord>> directConnections = new LinkedHashMap<>();
        private Map<CostPair, Integer> costs = new HashMap<>();
        WeightedGraph1(List<String> maze, Coord start, Coord target) {
            junktions.add(start);
            junktions.add(target);
            for(int y=0; y<maze.size(); ++y) {
                String line = maze.get(y);
                for(int x=0; x<line.length(); ++x) {
                    char ch = line.charAt(x);
                    if(ch!='#') {
                        Coord at = new Coord(x,y);
                        long validNextSteps = at.neighbours().stream().filter(n->n.isValid(maze) && n.extract(maze)!='#').count();
                        if(validNextSteps>2) {
                            junktions.add(at);
                        }
                    }
                }
            }
            for(var c: junktions) {
                System.out.println("junktion:\t"+c);
            }
            fillDirectConnections(maze);
            for(var dc: directConnections.entrySet()) {
                System.out.println("dc:\t"+dc);
            }
            System.out.println("sdc:\t"+directConnections.get(start));
            System.out.println("tdc:\t"+directConnections.get(target));
        }
        
        private void fillDirectConnections(List<String> maze) {
            List<Coord> junktionList = new ArrayList<>(junktions);
            for(int i=0; i<junktionList.size(); ++i) {
                Coord from = junktionList.get(i);
                Set<Coord> available = new HashSet<>();
                for(int j=i+1; j<junktionList.size(); ++j) {
                    Coord to = junktionList.get(j);
                    CostPair cp1 = new CostPair(from, to);
                    CostPair cp2 = new CostPair(to, from);
                    int path = longestMazePathWithoutOtherJunktions(maze, junktionList.get(i), junktionList.get(j));
                    if(path>0) {
                        available.add(to);
                        costs.put(cp1, path);
                        costs.put(cp2, path);
                        Set<Coord> toSet = directConnections.computeIfAbsent(to, k->new HashSet<>());
                        toSet.add(from);
                    }
                }
                var stored = directConnections.computeIfAbsent(from, k->new HashSet<>());
                stored.addAll(available);
            }
        }
        
        private int longestMazePathWithoutOtherJunktions(List<String> maze, Coord start, Coord target) {
            Queue<StepCount2> queue = new PriorityQueue<>(StepCount2.LONGEST_COMPARTOR);
            Map<Coord, Integer> steps = new HashMap<>();
            queue.add(new StepCount2(start, Set.of(start), 0));;
            steps.put(start, 0);
            int longest = -1;
            while(!queue.isEmpty()) {
                StepCount2 at = queue.poll();
                int nextCount = at.count()+1;
                if (target.equals(at.coord())) {
                    longest = Math.max(longest, at.count());
                } else {
                    at.coord().neighbours().stream().filter(n -> !at.prevs().contains(n) && n.isValid(maze)
                            && n.extract(maze) != '#' && (!junktions.contains(n) || target.equals(n))
                            && (!steps.containsKey(n) || steps.get(n)<nextCount)).forEach(n -> {
                                Set<Coord> nps = new HashSet<>(at.prevs);
                                nps.add(at.coord);
                                queue.add(new StepCount2(n, nps, nextCount));
                                steps.put(n, nextCount);
                            });
                }
            }
            return longest;
        }
        
        long longestPath(Coord start, Coord target) {
            Queue<StepCount2> queue = new LinkedList<>();//new PriorityQueue<>(StepCount.LONGEST_COMPARTOR);
            Map<Coord, Integer> steps = new HashMap<>();
            queue.add(new StepCount2(start, Set.of(start), 0));;
            steps.put(start, 0);
            long longest = -1L;
            while(!queue.isEmpty()) {
                StepCount2 at = queue.poll();
                directConnections.get(at.coord()).stream().filter(n->!at.prevs().contains(n)
                        && (!steps.containsKey(n) || steps.get(n)<(at.count+costs.get(new CostPair(at.coord, n))))).forEach(n->{
                    Set<Coord> nps = new HashSet<>(at.prevs);
                    nps.add(at.coord);
                    queue.add(new StepCount2(n, nps, at.count+costs.get(new CostPair(at.coord, n))));
                    steps.put(n, at.count+costs.get(new CostPair(at.coord, n)));
                });
                if(target.equals(at.coord())) {
                    longest = Math.max(longest, at.count());
                    System.out.println("at target: "+longest);
                }
            }
            return longest;
        }
        
        private record CostPair(Coord from, Coord to) {
            
        }
        
    }
    */
}
