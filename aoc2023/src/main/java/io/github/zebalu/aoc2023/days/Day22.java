package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;

public class Day22 {
    public static void main(String[] args) {
        Map<Brick, BrickSupport> network = makeThemFall(readInput().lines().map(Brick::new).sorted(Brick.Z_COMPARATOR).toList());
        System.out.println(countDesintegrateable(network));
        System.out.println(countDesintegration(network));
    }
    
    private static Map<Brick, BrickSupport> makeThemFall(List<Brick> zSorted) {
        Map<Brick, BrickSupport> network = new HashMap<>();
        Map<Coord, Brick> stopped = new HashMap<>();
        zSorted.forEach(c -> network.put(c, new BrickSupport(c, new LinkedHashSet<>(), new LinkedHashSet<>())));
        zSorted.forEach(brick -> {
            Set<Brick> standingOn = new HashSet<>();
            List<Brick> aboveThese = brick.findAbove(zSorted);
            if (aboveThese.isEmpty()) {
                brick.fallN(brick.z1);
            } else {
                brick.findClosesBelow(aboveThese).filter(b -> brick.z1 - b.z2 > 1)
                        .ifPresent(closest -> brick.fallN(brick.z1 - closest.z2 - 1));
            }
            brick.allCoords().stream().map(Coord::below).filter(stopped::containsKey)
                    .forEach(blocker -> standingOn.add(stopped.get(blocker)));
            brick.allCoords().forEach(c -> stopped.put(c, brick));
            standingOn.forEach(on -> {
                network.get(on).supports.add(brick);
                network.get(brick).supportedBy.add(on);
            });
        });
        return network;
    }
    
    private static long countDesintegrateable(Map<Brick, BrickSupport> network) {
        return network.values().stream().filter(bs->bs.supports().size() == 0 || bs.supports.stream().allMatch(sb -> network.get(sb).supportedBy.size() > 1)).count();
    }
    
    private static long countDesintegration(Map<Brick, BrickSupport> network) {
        return network.keySet().stream().mapToLong(b->countDesintegrationFrom(b, network)).sum();
    }
    
    private static long countDesintegrationFrom(Brick brick, Map<Brick, BrickSupport> network) {
        Set<Brick> desintegrated = new HashSet<>();
        Queue<Brick> toRemove = new LinkedList<>();
        desintegrated.add(brick);
        toRemove.add(brick);
        while(!toRemove.isEmpty()) {
            Brick toDelete = toRemove.poll();
            network.get(toDelete).supports.forEach(b->{
                BrickSupport support = network.get(b);
                long remainingSupport = support.supportedBy.stream().filter(s->!desintegrated.contains(s)).count();
                if(remainingSupport < 1) {
                    toRemove.add(b);
                    desintegrated.add(b);
                }
            });
        }
        return desintegrated.size()-1;
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day22.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Coord(int x, int y, int z) {

        Coord below() {
            return new Coord(x,y,z-1);
        }
        
        @Override
        public int hashCode() {
            return x*1_000_000 + y*1_000 + z;
        }

    }
    
    private static class Brick implements Cloneable {
        static final Comparator<Brick> Z_COMPARATOR = Comparator.comparingInt((Brick b) ->b.z1).thenComparing(Comparator.comparingInt((Brick b)->b.z2).reversed());
        int x1,x2,y1,y2,z1,z2;
        
        Brick(String line) {
            var parts = line.split("~");
            var p1s = parts[0].split(",");
            var p2s = parts[1].split(",");
            x1 = Integer.parseInt(p1s[0]);
            y1 = Integer.parseInt(p1s[1]);
            z1 = Integer.parseInt(p1s[2]);
            x2 = Integer.parseInt(p2s[0]);
            y2 = Integer.parseInt(p2s[1]);
            z2 = Integer.parseInt(p2s[2]);
        }
        
        void fallN(int n) {
            z1-=n;
            z2-=n;
        }
        
        Optional<Brick> findClosesBelow(List<Brick> bricks) {
            return bricks.stream().filter(this::isAbove).min(Comparator.comparingInt(b->z1-b.z2));
        }
        
        List<Brick> findAbove(List<Brick> bricks) {
            return bricks.stream().filter(this::isAbove).toList();
        }
        
        boolean isAbove(Brick other) {
            return other.z2 < z1 && hasCommonX(other) && hasCommonY(other); 
        }
        
        boolean hasCommonX(Brick other) {
            Brick a = x1 <= other.x1 ? this : other;
            Brick b = this == a ? other : this;
            return a.x1 <= b.x1 && b.x1 <= a.x2;
        }
        
        boolean hasCommonY(Brick other) {
            Brick a = y1 <= other.y1 ? this : other;
            Brick b = this == a ? other : this;
            return a.y1 <= b.y1 && b.y1 <= a.y2;
        }
        
        List<Coord> allCoords() {
            List<Coord> coords = new ArrayList<>();
            for(int x=x1; x<=x2; ++x) {
                for(int y=y1; y<=y2; ++y) {
                    for(int z=z1; z<=z2; ++z) {
                        coords.add(new Coord(x,y,z));
                    }
                }
            }
            return coords;
        }
    }
    
    private record BrickSupport(Brick brick, SequencedSet<Brick> supportedBy, SequencedSet<Brick> supports) {
        
    }
}
