package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;

public class Day14 {
    public static void main(String[] args) {
        String input = readInput();
        Platform p = new Platform(input);
        p.tiltNorth();
        System.out.println(p.loadOnNorth());
        p.cycle(1_000_000_000);
        System.out.println(p.loadOnNorth());
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day14.txt").toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Coord(int x, int y) {}
    
    private static class Platform {
        int width;
        int height;
        
        Set<Coord> stoppers = new HashSet<>();
        Set<Coord> rollers = new HashSet<>();
        
        Platform(String descS) {
            List<String> desc = descS.lines().toList();
            height = desc.size();
            width = desc.getFirst().length();
            for(int y=0; y<desc.size(); ++y) {
                String line = desc.get(y);
                for(int x=0; x<line.length(); ++x) {
                    switch (line.charAt(x)) {
                    case '#' -> stoppers.add(new Coord(x,y));
                    case 'O' -> rollers.add(new Coord(x,y));
                    }
                }
            }
        }
        
        void tiltNorth() {
            var todo = rollers.stream().sorted(Comparator.comparingInt(Coord::y).thenComparingInt(Coord::x)).toList();
            todo.stream().forEach(move->{
                boolean foundBlocker = false;
                int y = move.y;
                Coord target = null;
                while(y>0 && !foundBlocker) {
                    --y;
                    Coord next = new Coord(move.x, y);
                    foundBlocker = stoppers.contains(next) || rollers.contains(next);
                    if(!foundBlocker) {
                        target = next;
                    }
                }
                if(target != null) {
                    rollers.remove(move);
                    rollers.add(target);
                }
            });
        }
        
        void tiltSouth() {
            var todo = rollers.stream().sorted(Comparator.comparingInt(Coord::y).reversed().thenComparingInt(Coord::x)).toList();
            todo.stream().forEach(move->{
                boolean foundBlocker = false;
                int y = move.y;
                Coord target = null;
                while(y<height-1 && !foundBlocker) {
                    ++y;
                    Coord next = new Coord(move.x, y);
                    foundBlocker = stoppers.contains(next) || rollers.contains(next);
                    if(!foundBlocker) {
                        target = next;
                    }
                }
                if(target != null) {
                    rollers.remove(move);
                    rollers.add(target);
                }
            });
        }
        
        void titlWest() {
            var todo = rollers.stream().sorted(Comparator.comparingInt(Coord::x).thenComparingInt(Coord::y)).toList();
            todo.stream().forEach(move->{
                boolean foundBlocker = false;
                int x = move.x;
                Coord target = null;
                while(x>0 && !foundBlocker) {
                    --x;
                    Coord next = new Coord(x, move.y);
                    foundBlocker = stoppers.contains(next) || rollers.contains(next);
                    if(!foundBlocker) {
                        target = next;
                    }
                }
                if(target != null) {
                    rollers.remove(move);
                    rollers.add(target);
                }
            });
        }
        
        void tiltEast() {
            var todo = rollers.stream().sorted(Comparator.comparingInt(Coord::x).reversed().thenComparingInt(Coord::y)).toList();
            todo.stream().forEach(move->{
                boolean foundBlocker = false;
                int x = move.x;
                Coord target = null;
                while(x<width-1 && !foundBlocker) {
                    ++x;
                    Coord next = new Coord(x, move.y);
                    foundBlocker = stoppers.contains(next) || rollers.contains(next);
                    if(!foundBlocker) {
                        target = next;
                    }
                }
                if(target != null) {
                    rollers.remove(move);
                    rollers.add(target);
                }
            });
        }
        
        void cycle() {
            tiltNorth();
            titlWest();
            tiltSouth();
            tiltEast();
        }
        
        void cycle(int times) {
            int steps = 1;
            cycle();
            SequencedSet<Set<Coord>> history = new LinkedHashSet<>();
            while(!history.contains(rollers)) {
                history.add(new HashSet<>(rollers));
                cycle();
                ++steps;
            }
            int length = 0;
            var iterator = history.reversed().iterator();
            boolean found = false;
            while(iterator.hasNext() && !found) {
                var value = iterator.next();
                ++length;
                found = value.equals(rollers);
            }
            int remaining = (times - steps) % length;
            for(int i=0; i<remaining; ++i) {
                cycle();
            }
        }
        
        int loadOnNorth() {
            return rollers.stream().sorted(Comparator.comparingInt(Coord::y).thenComparingInt(Coord::x)).mapToInt(c->height-c.y).sum();
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for(int y=0; y<height; ++y) {
                for(int x=0; x<width; ++x) {
                    Coord c=new Coord(x,y);
                    if(stoppers.contains(c)) {
                        sb.append('#');
                    } else if(rollers.contains(c)) {
                        sb.append('O');
                    } else {
                        sb.append('.');
                    }
                }
                sb.append('\n');
            }
            return sb.toString();
        }
    }
}
