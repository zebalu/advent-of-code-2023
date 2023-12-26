package io.github.zebalu.aoc2023.days;

import java.math.*;
import java.nio.file.*;
import java.util.*;

public class Day24 {
    private static final int PRECISION_100 = 100;

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
        var m2 = createBDLinearMatrix(find3UsableHails(hails));
        solve(m2);
        var x2 = m2[0][6];
        var y2 = m2[1][6];
        var z2 = m2[2][6];
        return x2.add(y2).add(z2).setScale(0, RoundingMode.HALF_EVEN).longValue();
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
    
    private static List<Hail> find3UsableHails(List<Hail> hails) {
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
    
    private static BigDecimal[][] createBDLinearMatrix(List<Hail> hails) {
        if(hails.size() != 3) {
            throw new IllegalArgumentException("It can only work with 3 hails; got: "+hails.size());
        }
        
        BigDecimal zero = BigDecimal.ZERO;
        
        BDCoord ap = BDCoord.fromCoord(hails.get(0).position);
        BDCoord av = BDCoord.fromCoord(hails.get(0).velocity);
        BDCoord bp = BDCoord.fromCoord(hails.get(1).position);
        BDCoord bv = BDCoord.fromCoord(hails.get(1).velocity);
        BDCoord cp = BDCoord.fromCoord(hails.get(2).position);
        BDCoord cv = BDCoord.fromCoord(hails.get(2).velocity);
// @formatter: off
        BigDecimal[][] result = {
                {av.y.subtract(bv.y),          av.x.subtract(bv.x).negate(), zero,                         ap.y.subtract(bp.y).negate(), ap.x.subtract(bp.x),          zero,                         bp.y.multiply(bv.x).subtract(bp.x.multiply(bv.y)).subtract(ap.y.multiply(av.x).subtract(ap.x.multiply(av.y)))},
                {av.y.subtract(cv.y),          av.x.subtract(cv.x).negate(), zero,                         ap.y.subtract(cp.y).negate(), ap.x.subtract(cp.x),          zero,                         cp.y.multiply(cv.x).subtract(cp.x.multiply(cv.y)).subtract(ap.y.multiply(av.x).subtract(ap.x.multiply(av.y)))},
                {av.z.subtract(bv.z).negate(), zero,                         av.x.subtract(bv.x),          ap.z.subtract(bp.z),          zero,                         ap.x.subtract(bp.x).negate(), bp.x.multiply(bv.z).subtract(bp.z.multiply(bv.x)).subtract(ap.x.multiply(av.z).subtract(ap.z.multiply(av.x)))},
                {av.z.subtract(cv.z).negate(), zero,                         av.x.subtract(cv.x),          ap.z.subtract(cp.z),          zero,                         ap.x.subtract(cp.x).negate(), cp.x.multiply(cv.z).subtract(cp.z.multiply(cv.x)).subtract(ap.x.multiply(av.z).subtract(ap.z.multiply(av.x)))},
                {zero,                         av.z.subtract(bv.z),          av.y.subtract(bv.y).negate(), zero,                         ap.z.subtract(bp.z).negate(), ap.y.subtract(bp.y),          bp.z.multiply(bv.y).subtract(bp.y.multiply(bv.z)).subtract(ap.z.multiply(av.y).subtract(ap.y.multiply(av.z)))},
                {zero,                         av.z.subtract(cv.z),          av.y.subtract(cv.y).negate(), zero,                         ap.z.subtract(cp.z).negate(), ap.y.subtract(cp.y),          cp.z.multiply(cv.y).subtract(cp.y.multiply(cv.z)).subtract(ap.z.multiply(av.y).subtract(ap.y.multiply(av.z)))}
        };
// @formatter: on
        return result;
    }
    
    /**
     * Gauss elimination
     * 
     * @param c the matrix of the linear equation
     */
    private static void solve(BigDecimal[][] c) {
        for (int row = 0; row < c.length; row++) {
            // 1. set c[row][row] equal to 1
            BigDecimal factor = c[row][row];
            for (int col = 0; col < c[row].length; col++) {
                c[row][col] = c[row][col].divide(factor, PRECISION_100, RoundingMode.HALF_EVEN);
            }

            // 2. set c[row][row2] equal to 0
            for (int row2 = 0; row2 < c.length; row2++) {
                if (row2 != row) {
                    BigDecimal factor2 = c[row2][row].negate();
                    for (int col = 0; col < c[row2].length; col++) {
                        c[row2][col] = c[row2][col].add(factor2.multiply(c[row][col]));
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
    
    private record BDCoord(BigDecimal x, BigDecimal y, BigDecimal z) {
        static BDCoord fromCoord(Coord coord) {
            return new BDCoord(BigDecimal.valueOf(coord.x), BigDecimal.valueOf(coord.y), BigDecimal.valueOf(coord.z));
        }
    }
}
