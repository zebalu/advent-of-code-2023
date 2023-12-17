package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

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
    
    private record Coord(int x, int y) {
        static final Comparator<Coord> TOP_ROW = Comparator.comparingInt(Coord::y);
        static final Comparator<Coord> BOTTOM_ROW = TOP_ROW.reversed();
        static final Comparator<Coord> LEFT_COLUMN = Comparator.comparingInt(Coord::x);
        static final Comparator<Coord> RIGHT_COLUMN = LEFT_COLUMN.reversed();
    }
    
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
            tilt(Coord.TOP_ROW, Coord::y, y->y-1, y->0<y, (y,c)->new Coord(c.x, y));
        }
        
        void tiltSouth() {
            tilt(Coord.BOTTOM_ROW, Coord::y, y->y+1, y->y<height-1, (y,c)->new Coord(c.x, y));
        }
        
        void titlWest() {
            tilt(Coord.LEFT_COLUMN, Coord::x, x->x-1, x->0<x, (x,c)->new Coord(x,c.y));
        }
        
        void tiltEast() {
            tilt(Coord.RIGHT_COLUMN, Coord::x, x->x+1, x->x<width-1, (x,c)->new Coord(x,c.y));
        }
        
        private void tilt(Comparator<Coord> DIRECTION, ToIntFunction<Coord> extractor, IntUnaryOperator step, IntPredicate notEdge, BiFunction<Integer, Coord, Coord> wrapper) {
            var todo = rollers.stream().sorted(DIRECTION).toList();
            todo.stream().forEach(move->{
                boolean foundBlocker = false;
                int i = extractor.applyAsInt(move);
                Coord target = null;
                while(notEdge.test(i) && !foundBlocker) {
                    i = step.applyAsInt(i);
                    Coord next = wrapper.apply(i, move);
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
            var iterator = history.reversed().iterator();
            boolean found = false;
            List<Set<Coord>> reversed = new ArrayList<Set<Coord>>(history.size());
            while(iterator.hasNext() && !found) {
                var value = iterator.next();
                reversed.add(value);
                found = value.equals(rollers);
            }
            int remaining = (times - steps) % reversed.size();
            rollers = reversed.get(remaining);
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
