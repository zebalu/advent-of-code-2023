package io.github.zebalu.aoc2023.days;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * This is a mess; I will clean it once...
 * Based on the formula from: https://github.com/abnew123/aoc2023/blob/main/src/solutions/Day21.java
 */
public class Day21 {
    public static void main(String[] args) {
        String input = readInput();//example; //readInput(); //example; //readInput();
        //System.out.println(input);
        List<String> maze = input.lines().toList();
//        List<String> maze = expand(input.lines().toList(), 5);
        Coord start = null;
        for(int y=0; y<maze.size() && start == null; ++y) {
            String line = maze.get(y);
            for(int x=0; x<line.length() && start == null; ++x) {
                if(line.charAt(x)=='S') {
                    start = new Coord(x,y);
                }
            }
        }
        //System.out.println(start);
        final int maxSteps = 50;
        //int 
        //System.out.println(part1(maze, start, 64));
        //part2(maze, start, 500);//26501365);
        /*
        System.out.println("out north: \t"+stepsToGetOut(start, maze, c->c.y==0));
        System.out.println("out south: \t"+stepsToGetOut(start, maze, c->c.y==maze.size()-1));
        System.out.println("out east: \t"+stepsToGetOut(start, maze, c->c.x==maze.getFirst().length()-1));
        System.out.println("out west: \t"+stepsToGetOut(start, maze, c->c.x==0));
        System.out.println("out north2: \t"+stepsToGetOut(new Coord(start.x, maze.size()-1), maze, c->c.y==0));
        System.out.println(findAllAvailable(start, maze).size());
        System.out.println(findAllAvailableInParity(start, maze, true).size());
        System.out.println(findAllAvailableInParity(start, maze, false).size());
        System.out.println(countStepsToFille(new Coord(0,0), maze, findAllAvailableInParity(new Coord(0,0), maze, true)));
        System.out.println(countStepsToFille(new Coord(0,0), maze, findAllAvailableInParity(new Coord(0,0), maze, false)));
        System.out.println("------------");
        System.out.println(countStepsToFille(new Coord(start.x,0), maze, findAllAvailableInParity(new Coord(start.x,0), maze, true)));
        System.out.println(countStepsToFille(new Coord(start.x,0), maze, findAllAvailableInParity(new Coord(start.x,0), maze, false)));
        */
        /*
        var ass = assumption(maze, start, 26501365);
        System.out.println(ass);
        System.out.println("592723929260582\t<--\texpected");
        System.out.println(592723929260582L-ass+"\t<--<\tdiff");
        */
        System.out.println(withDeltaTracking(start, maze, 64));
        System.out.println(withDeltaTracking(start, maze, 26501365));
    }
    
    private static long assumption(List<String> maze, Coord start, int step) {
        long getoutCount = stepsToGetOut(start, maze, c->c.y==0);
        long remaing = step-getoutCount;
        int tileRadious = (int)remaing / maze.size();
        long borderLength = tileRadious*4; 
        //tileRadious = 6;
        long etc1 = 2*tileRadious-1;//tileRadious;
        long otc1 = 2*tileRadious+1;//tileRadious-1;
        System.out.println(etc1+"\tetc1");
        System.out.println(otc1+"\totc1");
        long evenTileCount = (etc1+1)*(tileRadious) / 2;
        long oddTileCount = (otc1 + 1)*(tileRadious+1) / 2 - borderLength;
        System.out.println("otc: "+oddTileCount);
        System.out.println("etc: "+evenTileCount);
        System.out.println("tile radious: "+tileRadious);
        System.out.println("bl: "+borderLength);
        long internalPoints = findInternalArea(
                List.of(new Coord(-tileRadious, 0), new Coord(0, -tileRadious), new Coord(tileRadious, 0), new Coord(0, tileRadious)),
                borderLength) - borderLength;
        System.out.println(borderLength+"\tassumption bl");
        System.out.println(internalPoints+"\tassumption ip");
        var allAvailbalePoints = findAllAvailable(start, maze);
        var oddAvailablePoints = findAllAvailableInParity(start, maze, false);
        var evenAvailablePoints = findAllAvailableInParity(start, maze, true);
        long internalCount = internalPoints * oddAvailablePoints.size();// evenTileCount * evenAvailablePoints.size() + oddTileCount * oddAvailablePoints.size();
        System.out.println(internalCount+"\tassumption internal count");
        long westCount = part1(maze, new Coord(maze.get(start.y).length()-1, start.y), maze.size());
        long eastCount = part1(maze, new Coord(0, start.y), maze.size());
        long northCount = part1(maze, new Coord(start.x, maze.size()-1), maze.size());
        long southCount = part1(maze, new Coord(start.x, 0), maze.size());
        long internal_and_vertexes = internalCount + westCount + eastCount + northCount + southCount;
        System.out.println(internal_and_vertexes +"\tass-no-border");
        List<Coord> corners = List.of(new Coord(0,0), 
                new Coord(maze.getFirst().length()-1,0),
                new Coord(maze.size()-1, 0),
                new Coord(maze.size()-1, maze.getLast().length()-1));
        long borderPointCount = corners.stream().mapToLong(c->part1(maze, c, maze.size())*(tileRadious)).sum();
        return borderPointCount + internal_and_vertexes;
    }
    
    private static long findInternalArea(List<Coord> coords, long line) {
        long area = 0L;
        for(int i=0; i< coords.size(); ++i) {
            Coord ci = coords.get(i);
            Coord cn = coords.get((i + 1) % coords.size());
            area += ((long)ci.y + cn.y) * (ci.x - cn.x);
        }
        return (Math.abs(area) + (long)line) / 2 + 1;
    }
    
    private static long part1(List<String> maze, Coord start, final int maxSteps) {
        Queue<State> queue = new LinkedList<>();
        Map<Coord, Integer> stepCount = new HashMap<>();
        stepCount.put(start, maxSteps);
        queue.add(new State(start, maxSteps));
        Set<Coord> visited = new HashSet<>();
        visited.add(start);
        int stepsc = 0;
        for(int i=0; i<maxSteps; ++i) {
            visited.clear();
            while (!queue.isEmpty()) {
                State curr = queue.poll();
                int nextStep = curr.steps() - 1;
                if (nextStep >= 0) {
                    curr.coord().neighbours().stream().filter(n -> n.isValid(maze) && n.extract(maze) != '#'
                            && !visited.contains(n)
                    /* && (stepCount.get(n) == null || stepCount.get(n) < nextStep) */).forEach(pos -> {
                        stepCount.put(pos, nextStep);
                        visited.add(pos);
                    });
                }
            }
            int ii = i;
            visited.forEach(c->{
                queue.add(new State(c, maxSteps-ii-1));
            });
        }
        return visited.size();
    }
    private static long part1_infinite(List<String> maze, Coord start, final int maxSteps) {
        Queue<State> queue = new LinkedList<>();
        Map<Coord, Integer> stepCount = new HashMap<>();
        stepCount.put(start, maxSteps);
        queue.add(new State(start, maxSteps));
        Set<Coord> visited = new HashSet<>();
        visited.add(start);
        for(int i=0; i<maxSteps; ++i) {
            visited.clear();
            while (!queue.isEmpty()) {
                State curr = queue.poll();
                int nextStep = curr.steps() - 1;
                if (nextStep >= 0) {
                    curr.coord().neighbours(maze).stream().filter(n -> n.extract(maze) != '#'
                            && !visited.contains(n)
                    /* && (stepCount.get(n) == null || stepCount.get(n) < nextStep) */).forEach(pos -> {
                        stepCount.put(pos, nextStep);
                        visited.add(pos);
                    });
                }
            }
            int ii = i;
            visited.forEach(c->{
                queue.add(new State(c, maxSteps-ii-1));
            });
        }
        return visited.size();
    }
    private static void part2(List<String> maze, Coord start, final int maxSteps) {
        Set<Coord> validCoordsInOriginalGared = new HashSet<>();
        for(int y=0; y<maze.size(); ++y) {
            String line = maze.get(y);
            for(int x=0; x<line.length(); ++x) {
                if('#'!=line.charAt(x)) {
                    validCoordsInOriginalGared.add(new Coord(x,y));
                }
            }
        }
        Queue<State> queue = new LinkedList<>();
        queue.add(new State(start, maxSteps));
        int stepsTook = 0;
        Set<Coord> visited = new HashSet<>();
        Set<Coord> prev = new HashSet<>();
        Set<Coord> prev2 = new HashSet<>();
        List<Long> diffs = new ArrayList<>();
        visited.add(start);
        for(int i=0; i<maxSteps && true/*!allFound(validCoordsInOriginalGared, visited, prev)*/; ++i) {
            ++stepsTook;
            /*
            System.out.println(i+"\t/\t"+maxSteps);
            System.out.println(visited.size()+"\t"+prev.size()+"\tchange:\t"+(visited.size() - prev.size())+"\t"+countMissing(visited, prev));
            System.out.println(prev.size()+"\t"+prev2.size()+"\tchange:\t"+(prev.size() - prev2.size())+"\t"+countMissing(prev, prev2));
            System.out.println(visited.size()+"\t"+prev2.size()+"\tchange:\t"+(visited.size() - prev2.size())+"\t"+countMissing(visited, prev2));            
            diffs.add(countMissing(visited, prev2));
            prev2 = new HashSet<>(prev);
            prev = new HashSet<>(visited);
            */
            visited.clear();
            while (!queue.isEmpty()) {
                State curr = queue.poll();
                int nextStep = curr.steps() - 1;
                if (nextStep >= 0) {
                    curr.coord().neighbours().stream().filter(n -> n.extractInfinit(maze) != '#'
                            && !visited.contains(n)).forEach(pos -> {
                        visited.add(pos);
                    });
                }
            }
            int ii = i;
            visited.forEach(c->{
                queue.add(new State(c, maxSteps-ii-1));
            });
        }
        System.out.println("full gared after: "+stepsTook);
        long lll = (maxSteps / stepsTook )*validCoordsInOriginalGared.size();
        System.out.println(visited.size());
        System.out.println(lll);
        System.out.println(diffs);
        var evens = IntStream.iterate(0, i->i<diffs.size()-1, i->i+2).mapToObj(i->diffs.get(i)).toList();
        var odds = IntStream.iterate(1, i->i<diffs.size()-1, i->i+2).mapToObj(i->diffs.get(i)).toList();
        System.out.println(evens);
        System.out.println(odds);
    }
    
    private static long withDeltaTracking(Coord start, List<String> maze, int maxSteps) {
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
            todo = todo.stream().flatMap(c->c.neighbours().stream()).filter(c->!reached.contains(c) && c.extractInfinit(maze) != '#').peek(reached::add).toList();
            if ( index % 2 == maxSteps %2) {
                totalReached += todo.size();
                if (index % maze.size() == getOutCount) {
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
    private static Set<Coord> findAllAvailable(Coord start, List<String> maze) {
        Set<Coord> visited = new HashSet<>();
        Queue<Coord> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            var curr = queue.poll();
            curr.neighbours().stream().filter(n -> n.isValid(maze) && n.extract(maze) != '#' && !visited.contains(n))
                    .forEach(n -> {
                        queue.add(n);
                        visited.add(n);
                    });
        }
        return visited;
    }
    private static Set<Coord> findAllAvailableWithinSteps(Coord start, List<String> maze, int steps) {
        Set<Coord> visited = new HashSet<>();
        Queue<Coord> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            var curr = queue.poll();
            curr.neighbours().stream().filter(n -> n.isValid(maze) && n.extract(maze) != '#' && !visited.contains(n))
                    .forEach(n -> {
                        queue.add(n);
                        visited.add(n);
                    });
        }
        return visited;
    }
    private static Set<Coord> findAllAvailableInParity(Coord start, List<String> maze, boolean isEven) {
        Set<Coord> evens = new HashSet<>();
        Set<Coord> odds = new HashSet<>();
        Set<Coord> visited = new HashSet<>();
        Queue<State> queue = new LinkedList<>(); 
        queue.add(new State(start, 0));
        while (!queue.isEmpty()) {
            var curr = queue.poll();
            visited.add(curr.coord());
            if(curr.steps()%2==0) {
                evens.add(curr.coord());
            } else {
                odds.add(curr.coord());
            }
            curr.coord().neighbours().stream().filter(n -> n.isValid(maze) && n.extract(maze) != '#' && !visited.contains(n))
                    .forEach(n -> {
                        queue.add(new State(n, curr.steps()+1));
                        visited.add(n);
                    });
        }
        return isEven?evens:odds;
    }
    private static long countStepsToFille(Coord start, List<String> maze, Set<Coord> targets) {
        Queue<State> queue = new LinkedList<>();
        queue.add(new State(start, 0));
        Set<Coord> visited = new HashSet<>();
        visited.add(start);
        int max = -1;
        while (!queue.isEmpty() && !visited.containsAll(targets)) {
            State curr = queue.poll();
            max = curr.steps() + 1;
            curr.coord().neighbours().stream().filter(n -> n.isValid(maze) && n.extract(maze) != '#'
                    && !visited.contains(n)).forEach(pos -> {
                visited.add(pos);
                queue.add(new State(pos, curr.steps()+1));
            });
        }
        return max;
    }
    private static boolean allFound(Set<Coord> target, Set<Coord> current, Set<Coord> prev) {
        Set<Coord> common = new HashSet<>(current.size() + prev.size());
        common.addAll(current);
        common.addAll(prev);
        return common.containsAll(target);
    }
    private static long countMissing(Set<Coord> current, Set<Coord> prev) {
        return current.stream().filter(c->!prev.contains(c)).count();
    }
    
    private static List<String> expand(List<String> original, int times) {
        if(times%2!=1) {
            throw new IllegalArgumentException("I can only work with odd numbers");
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < times; ++i) {
            for (String o : original) {
                String replaced = o.replace('S', '.');
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < times; ++j) {
                    if (i == times / 2 && j == times / 2) {
                        sb.append(o);
                    } else {
                        sb.append(replaced);
                    }
                }
                result.add(sb.toString());
            }
        }
        return result;
    }
    private static String readInput() {
        try {
            return Files.readString(Path.of("day21.txt").toAbsolutePath());
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
    private record Coord(int x, int y) {
        List<Coord> neighbours() {
            return List.of(new Coord(x-1,y), new Coord(x+1, y), new Coord(x, y-1), new Coord(x, y+1));
        }
        List<Coord> neighbours(List<String> maze) {
            int width = maze.get(y).length();
            int height = maze.size();
            return List.of(new Coord((x-1+width)%width,y), new Coord((x+1)%width, y), new Coord(x, (y-1+height)%height), new Coord(x, (y+1)%height));
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
    }
    private record State (Coord coord, int steps) {}
    
    private static String example = """
...........
.....###.#.
.###.##..#.
..#.#...#..
....#.#....
.##..S####.
.##..#...#.
.......##..
.##.#.####.
.##..##.##.
...........""";
}
