package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;

public class Day24 {
    public static void main(String[] args) {
        List<Hail> hails = readInput().lines().map(Hail::read).toList();
        System.out.println(part1(hails));
        System.out.println(part2(hails));
    }
    
    private static long part1(List<Hail> hails) {
        return countCollisionsInTestArea(hails, 200_000_000_000_000L, 400_000_000_000_000L);
    }
    
    private static long part2(List<Hail> hails) {
        // for the details, please find: https://github.com/DeadlyRedCube/AdventOfCode/blob/1f9d0a3e3b7e7821592244ee51bce5c18cf899ff/2023/AOC2023/D24.h#L66-L294
        // explains, how to get linear equation system correctly
        var m = createLinearMatrix(find3UsableHails(hails));
        solve(m);
        long x = (long)m[0][6];
        long y = (long)m[1][6];
        long z = (long)m[2][6];
        return x+y+z;
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day24.txt").toAbsolutePath());
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
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
    
    private static List<Hail> find3UsableHails(List<Hail> hailsOrig) {
        var hails = hailsOrig.stream().sorted(Comparator.comparingLong(h->h.velocity.x)).toList();
        List<Hail> result = new ArrayList<>();
        for(int aInd = 0; aInd < hails.size() - 2 && result.size() < 3; ++aInd) {
            Hail a = hails.get(aInd);
            for(int bInd = aInd+1; bInd<hails.size()-1 && result.size()<3; ++bInd) {
                Hail b = hails.get(bInd);
                if(!a.velocity.equals(b.velocity)) {
                    for(int cInd=bInd+1; cInd < hails.size()&&result.size()<3; ++cInd) {
                        Hail c = hails.get(cInd);
                        if(!a.velocity.equals(c.velocity) && !b.velocity.equals(c.velocity)) {
                            result.add(a);
                            result.add(b);
                            result.add(c);
                        }
                    }
                }
            }
        }
        if(result.size()<3) {
            throw new NoSuchElementException("Can not find 3 usable hails");
        }
        return result;
    }
    
    private static double[][] createLinearMatrix(List<Hail> hails) {
        if(hails.size() != 3) {
            throw new IllegalArgumentException("It can only work with 3 hails; got: "+hails.size());
        }
        Coord ap = hails.get(0).position;
        Coord av = hails.get(0).velocity;
        Coord bp = hails.get(1).position;
        Coord bv = hails.get(1).velocity;
        Coord cp = hails.get(2).position;
        Coord cv = hails.get(2).velocity;
// @formatter: off
        double[][] result = {
                {av.y-bv.y, -(av.x-bv.x), 0.0, -(ap.y-bp.y), ap.x-bp.x, 0.0, (bp.y*bv.x - bp.x*bv.y)-(ap.y*av.x-ap.x*av.y)},
                {av.y-cv.y, -(av.x-cv.x), 0.0, -(ap.y-cp.y), ap.x-cp.x, 0.0, (cp.y*cv.x - cp.x*cv.y)-(ap.y*av.x-ap.x*av.y)},
                {-(av.z-bv.z), 0.0, av.x-bv.x, ap.z-bp.z, 0.0, -(ap.x-bp.x), (bp.x*bv.z - bp.z*bv.x)-(ap.x*av.z-ap.z*av.x)},
                {-(av.z-cv.z), 0.0, av.x-cv.x, ap.z-cp.z, 0.0, -(ap.x-cp.x), (cp.x*cv.z - cp.z*cv.x)-(ap.x*av.z-ap.z*av.x)},
                {0.0, av.z-bv.z, -(av.y-bv.y), 0.0, -(ap.z-bp.z), ap.y-bp.y, (bp.z*bv.y - bp.y*bv.z)-(ap.z*av.y-ap.y*av.z)},
                {0.0, av.z-cv.z, -(av.y-cv.y), 0.0, -(ap.z-cp.z), ap.y-cp.y, (cp.z*cv.y - cp.y*cv.z)-(ap.z*av.y-ap.y*av.z)}
        };
// @formatter: on
        return result;
    }

    /**
     * Gauss elimination
     * 
     * @param c the matrix of the linear equation
     */
    private static void solve(double[][] c) {
        for (int row = 0; row < c.length; row++) {
            // 1. set c[row][row] equal to 1
            double factor = c[row][row];
            for (int col = 0; col < c[row].length; col++) {
                c[row][col] /= factor;
            }

            // 2. set c[row][row2] equal to 0
            for (int row2 = 0; row2 < c.length; row2++) {
                if (row2 != row) {
                    double factor2 = -c[row2][row];
                    for (int col = 0; col < c[row2].length; col++) {
                        c[row2][col] += factor2 * c[row][col];
                    }
                }
            }
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
    }
    
    private record Hail(Coord position, Coord velocity) {
        static Hail read(String desc) {
            var parts = desc.split(" @ ");
            return new Hail(Coord.read(parts[0]), Coord.read(parts[1]));
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
}
