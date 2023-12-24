package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.List;

public class Day24 {
    public static void main(String[] args) {
        String input = readInput(); //example; readInput();
        System.out.println(input);
        System.out.println(input.lines().count());
        List<Hail> hails = input.lines().map(Hail::read).toList();
        System.out.println(hails);
        hails.stream().forEach(h->System.out.println(h.slopeOnPlain()));
        Hail a = new Hail(new Coord(0,0,0), new Coord(1,1,0));
        Hail b = new Hail(new Coord(0,1,0), new Coord(-1,1,0));
        Hail b2 = new Hail(b.positionAfter(-5), new Coord(-1,1,0));
        System.out.println(b2);
        System.out.println(a.intersectOnPlain(b));
        System.out.println(a.intersectOnPlain(b2));
        System.out.println(a.isIntersectionInFutureOnPlain(a.intersectOnPlain(b)));
        System.out.println(b.isIntersectionInFutureOnPlain(b.intersectOnPlain(a)));
        System.out.println(countCollisionsInTestArea(hails, 200000000000000L, 400000000000000L));
        //System.out.println(countCollisionsInTestArea(hails, 7, 27));
    }
    
    private static int countCollisionsInTestArea(List<Hail> hails, long min, long max) {
        int count = 0;
        for(int i=0; i<hails.size(); ++i) {
            Hail a = hails.get(i);
            for(int j=i+1; j<hails.size(); ++j) {
                Hail b = hails.get(j);
                var intersection = a.intersectOnPlain(b);
                if(a.isIntersectionInFutureOnPlain(intersection) && b.isIntersectionInFutureOnPlain(intersection)) {
                    if(min < intersection.x && intersection.x < max && min < intersection.y && intersection.y < max) {
                        ++count;
                    }
                }
            }
        }
        return count;
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day24.txt").toAbsolutePath());
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Coord(long x, long y, long z) {
        static Coord read(String desc) {
            var parts = desc.split(", ");
            return new Coord(
                    Long.parseLong(parts[0].strip()),
                    Long.parseLong(parts[1].strip()),
                    Long.parseLong(parts[2].strip())
                    );
        }
    }
    
    private record IntersectCoords(double x, double y, double z) {
        double distanceFromOnPlain(Coord coord) {
            return Math.sqrt(Math.pow(x-coord.x,2.0)+Math.pow(y-coord.y, 2.0));
        }
    }
    
    private record Hail(Coord position, Coord velocity) {
        static Hail read(String desc) {
            var parts = desc.split(" @ ");
            return new Hail(Coord.read(parts[0]), Coord.read(parts[1]));
        }
        Coord positionAfter(long nanos) {
            return new Coord(position.x+nanos*velocity.x, position.y+nanos*velocity.y, position.z+nanos*velocity.z);
        }
        double slopeOnPlain() {
            double top = -velocity.y;
            double bottom = -velocity.x;
            return top / bottom;
        }
        double calcB() {
            return position.y-slopeOnPlain()*position.x;
        }
        IntersectCoords intersectOnPlain(Hail hail) {
            double m1 = slopeOnPlain();
            double m2 = hail.slopeOnPlain();
            double b1 = calcB();
            double b2 = hail.calcB();
            double x = (b1-b2)/(m2-m1);
            double y = m1*x+b1;
            return new IntersectCoords(x, y, 0.0);
        }
        
        boolean isIntersectionInFutureOnPlain(IntersectCoords intersection) {
            return Math.signum(intersection.x-position.x) == Math.signum(velocity.x) && Math.signum(intersection.y-position.y) == Math.signum(velocity.y);
        }
    }
    
    private static String example = """
19, 13, 30 @ -2,  1, -2
18, 19, 22 @ -1, -1, -2
20, 25, 34 @ -2, -2, -4
12, 31, 28 @ -1, -2, -1
20, 19, 15 @  1, -5, -3""";
}
