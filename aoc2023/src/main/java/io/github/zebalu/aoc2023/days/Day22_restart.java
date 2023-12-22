package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;

public class Day22_restart {
    public static void main(String[] args) {
        String input = readInput(); //example; //readInput();
        var lines = input.lines().toList();
        List<Brick> bricks = new ArrayList<>();
        for(int i=0; i<lines.size(); ++i) {
            bricks.add(new Brick(i, lines.get(i)));
        }
        bricks.sort(Brick.Z_COMPARATOR);
        SequencedMap<Brick, BrickSupport> network = makeThemFall(bricks);
      //338 too low
        //340 too high
        System.out.println(countDesintegrateable(network));
        System.out.println(countDesintegration(network));
    }
    
    private static SequencedMap<Brick, BrickSupport> makeThemFall(List<Brick> zSorted) {
        SequencedMap<Brick, BrickSupport> network = new LinkedHashMap<>();
        SequencedMap<Coord, Brick> stopped = new LinkedHashMap<>();
        List<Brick> copy = zSorted.stream().map(Brick::clone).peek(c->network.put(c, new BrickSupport(c,  new LinkedHashSet<>(), new LinkedHashSet<>()))).toList();
        copy.forEach(brick -> {
            SequencedSet<Brick> standingOn = new LinkedHashSet<>();
            while(brick.z1>0 && standingOn.isEmpty()) {
                for(int x=brick.x1; x<=brick.x2; ++x) {
                    for(int y=brick.y1; y<=brick.y2; ++y) {
                        for(int z=brick.z1; z<=brick.z2; ++z) {
                            Coord below = new Coord(x,y,z-1);
                            if(stopped.containsKey(below)) {
                                standingOn.add(stopped.get(below));
                            }
                        }   
                    }
                }
                if(standingOn.isEmpty()) {
                    --brick.z1;
                    --brick.z2;
                }
            }
            for (int x = brick.x1; x <= brick.x2; ++x) {
                for (int y = brick.y1; y <= brick.y2; ++y) {
                    for (int z = brick.z1; z <= brick.z2; ++z) {
                        stopped.put(new Coord(x, y, z), brick);
                    }
                }
            }
            for (Brick on : standingOn) {
                network.get(on).supports.add(brick);
                network.get(brick).supportedBy.add(on);
            }
        });
        return network;
    }
    
    private static long countDesintegrateable(SequencedMap<Brick, BrickSupport> network) {
        return network.values().stream().filter(bs->bs.supports().size() == 0 || bs.supports.stream().allMatch(sb -> network.get(sb).supportedBy.size() > 1)).count();
    }
    
    private static long countDesintegration(SequencedMap<Brick, BrickSupport> network) {
        return network.keySet().stream().mapToLong(b->countDesintegrationFrom(b, network)).sum();
    }
    
    private static long countDesintegrationFrom(Brick brick, SequencedMap<Brick, BrickSupport> network) {
        SequencedSet<Brick> desintegrated = new LinkedHashSet<>();
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

        @Override
        public int hashCode() {
            return x*1_000_000 + y*1_000 + z;
        }

    }
    
    private static class Brick implements Cloneable {
        static final Comparator<Brick> Z_COMPARATOR = Comparator.comparingInt(b->b.z1);
        int id;
        int x1,x2,y1,y2,z1,z2;
        
        Brick(int id, String line) {
            this.id = id;
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
        
        @Override
        public Brick clone() {
            try {
                return (Brick) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Brick other = (Brick) obj;
            return id == other.id && x1 == other.x1 && x2 == other.x2 && y1 == other.y1 && y2 == other.y2
                    && z1 == other.z1 && z2 == other.z2;
        }
        
        @Override
        public String toString() {
            return String.format("{%4d %3d,%3d,%3d~%3d,%3d,%3d}", id, x1, y1, z1, x2, y2, z2);
        }
    }
    
    private record BrickSupport(Brick brick, SequencedSet<Brick> supportedBy, SequencedSet<Brick> supports) {
        
    }
}
