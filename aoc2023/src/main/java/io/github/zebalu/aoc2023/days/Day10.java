package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.IntStream;

public class Day10 {
    public static void main(String[] args) {
        String input = readInput();
        List<String> lines = input.lines().toList();
        Coord start = findStart(lines);
        var fixedStart = replace(lines, start);
        var pipe = findPipe(start, fixedStart);
        System.out.println(pipe.size()/2);
        var internals = countInTernal(pipe, fixedStart);
        System.out.println(internals.size());
        
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day10.txt").toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static Coord findStart(List<String> lines) {
        Coord start = null;
        for(int y = 0; y<lines.size() && start == null; ++y) {
            int x = lines.get(y).indexOf('S');
            if(0<=x) {
                return new Coord (x,y);
            }
        }
        throw new IllegalStateException();
    }
    
    private static List<String> replace(List<String> lines, Coord start) {
       char startCh =  List.of('|', '-', 'F', 'L', 'J', '7').stream().filter(ch->makesSense(lines, start, ch)).findAny().orElseThrow();
       return IntStream.range(0, lines.size()).mapToObj(y->{
           if(y!= start.y) {
               return lines.get(y);
           } else {
               char[] chs = lines.get(y).toCharArray();
               chs[start.x] = startCh;
               return new String(chs);
           }
       }).toList();
    }
    
    private static boolean makesSense(List<String> lines, Coord start, Character ch) {
        char before = lines.get(start.y).charAt(start.x-1);
        char after = lines.get(start.y).charAt(start.x+1);
        char over = lines.get(start.y-1).charAt(start.x);
        char under = lines.get(start.y+1).charAt(start.x);
        return switch(ch) {
        case '-' -> (before == 'F' || before == '-' || before == 'L') && (after == '-' || after == 'J' || after == '7');
        case '|' -> (under == '|' || under == 'L' || under == 'J') && (over == '|' || over == '7' || over == 'F');
        case 'F' -> (under == '|' || under == 'L' || under == 'J') && (after == '-' || after == 'J' || after == '7');
        case 'L' -> (over == 'F' || over == '7' || over == '|') && (after == '-' || after == 'J' || after == '7');
        case 'J' -> (over == 'F' || over == '7' || over == '|') && (before == '-' || before == 'L' || before == 'F');
        case '7' -> (before == 'F' || before == '-' || before == 'L') && (under == 'L' || under == 'J' || under == '|');
        default -> throw new IllegalStateException();
        };
    }
    
    private static Set<Coord> findPipe(Coord start, List<String> lines) {
        SequencedSet<Coord> visited = new LinkedHashSet<>();
        Queue<Coord> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        while(!queue.isEmpty()) {
            Coord top = queue.poll();
            List<Coord> possible = findTargets(pipeAt(top, lines), top).stream().filter(c->!visited.contains(c) && isValid(pipeAt(top, lines), top, c, lines)).toList();
            visited.addAll(possible);
            queue.addAll(possible);
        }
        return visited;
    }
    
    private static Set<Coord> countInTernal(Set<Coord> pipe, List<String> lines) {
        Set<Coord> internal = new HashSet<>();
        for(int y=0; y<lines.size(); ++y) {
            Set<Coord> found = new HashSet<>();
            String line = lines.get(y);
            boolean inside = false;
            char start = ' ';
            for(int x=0; x<line.length(); ++x) {
                char cAt = line.charAt(x);
                Coord coord = new Coord(x,y);
                if (pipe.contains(coord)) {
                    if (start == ' ') {
                        start = cAt;
                    }
                    if (cAt == '|') {
                        inside = !inside; 
                        start = ' ';  
                    } else if(cAt == 'J' && start == 'F') { 
                        inside = !inside; 
                        start = ' '; 
                    } else if(cAt == 'J' && start == 'L') { 
                        start = ' '; 
                    } else if (cAt == '7' && start == 'L'){ 
                        inside = !inside; 
                        start = ' '; 
                    } else if (cAt == '7' && start == 'F'){ 
                        start = ' ';
                    }
                    if (!found.isEmpty()) {
                        internal.addAll(found);
                        found.clear();
                    }
                }else {
                    if (inside) {
                        found.add(coord);
                        start = ' ';
                    }
                }
            }
        }
        return internal;
    }
    
    private static List<Coord> findTargets(char at, Coord c) {
        return switch (at) {
        case '|' ->List.of(new Coord(c.x, c.y+1), new Coord(c.x, c.y-1));
        case '-' -> List.of(new Coord(c.x+1, c.y), new Coord(c.x-1, c.y));
        case 'L' -> List.of(new Coord(c.x, c.y-1), new Coord(c.x+1, c.y));
        case 'J' -> List.of(new Coord(c.x-1, c.y), new Coord(c.x, c.y-1));
        case '7' -> List.of(new Coord(c.x, c.y+1), new Coord(c.x-1, c.y));
        case 'F' -> List.of(new Coord(c.x+1, c.y), new Coord(c.x, c.y+1));
        default -> throw new IllegalArgumentException();
        };
    }
    
    private static char pipeAt(Coord c, List<String> lines) {
        return lines.get(c.y).charAt(c.x);
    }
    
    private static boolean isValid(char currT, Coord currC, Coord nextC, List<String> lines) {
        if(nextC.x<0 || lines.getFirst().length() <= nextC.x || nextC.y <0 || lines.size() <= nextC.y) {
            return false;
        }
        char nextT = pipeAt(nextC, lines);
        return switch(currT) {
        case 'S' -> true;
        case '|' -> nextT == '|' || (nextC.y<currC.y && (nextT == 'F' || nextT == '7')) || (nextC.y>currC.y && (nextT == 'L' || nextT == 'J'));
        case '-' -> nextT == '-' || (nextC.x<currC.x && (nextT == 'L' || nextT == 'F')) || (nextC.x>currC.x && (nextT == '7' || nextT == 'J'));
        case 'L' -> (nextC.y<currC.y && (nextT == 'F' || nextT == '7' || nextT == '|')) || (nextC.x>currC.x && (nextT == '7' || nextT == 'J' || nextT=='-'));
        case 'J' -> (nextC.y<currC.y && (nextT == 'F' || nextT == '7' || nextT == '|')) || (nextC.x<currC.x && (nextT == 'L' || nextT == 'F' || nextT=='-'));
        case '7' -> (nextC.x<currC.x && (nextT == 'F' || nextT == 'L' || nextT == '-')) || (nextC.y>currC.y && (nextT == 'L' || nextT == 'J' || nextT=='|'));
        case 'F' -> (currC.x<nextC.x && (nextT == 'J' || nextT == '7' || nextT == '-')) || (nextC.y>currC.y && (nextT == 'L' || nextT == 'J' || nextT=='|'));
        default -> throw new IllegalStateException();
        };
    }
    
    private record Coord(int x, int y) {};
    
}
