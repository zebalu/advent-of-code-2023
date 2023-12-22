package io.github.zebalu.aoc2023.days;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Day22 {
    public static void main(String[] args) {
        String input = readInput(); //example; //readInput();
        System.out.println(input);
        var bricks = input.lines().map(Brick::read).peek(System.out::println).toList();
        System.out.println(bricks.size());
        System.out.println("-------------------");
        List<Brick> zOrdered = bricks.stream().sorted(Brick.Z_COMPARATOR).toList();
        zOrdered.forEach(System.out::println);
        Map<Brick, VerticalCounter> counterMap = new HashMap<>();
        for(int i=0; i<zOrdered.size(); ++i) {
            Brick lower = zOrdered.get(i);
            counterMap.put(lower, new VerticalCounter(new AtomicInteger(0), new AtomicInteger(0)));
            for(int j=i+1; j<zOrdered.size(); ++j) {
                Brick upper = zOrdered.get(j);
                if(upper.isAbove(lower)) {
                    counterMap.compute(lower, (k,v)-> {
                        if(v== null) {
                            return new VerticalCounter(new AtomicInteger(0), new AtomicInteger(1));
                        } else {
                            v.below.incrementAndGet();
                            return v;
                        }
                    });
                    counterMap.compute(upper, (k,v)-> {
                        if(v== null) {
                            return new VerticalCounter(new AtomicInteger(1), new AtomicInteger(0));
                        } else {
                            v.above.incrementAndGet();
                            return v;
                        }
                    });
                    System.out.println("found");
                }
            }
        }
        Map<Brick, BrickSupport> network = new HashMap<>();
        for(var br : zOrdered) {
            network.put(br, BrickSupport.create(br));
        }
        for(int i=0; i<zOrdered.size(); ++i) {
            Brick lower = zOrdered.get(i);
            for(int j=i+1; j<zOrdered.size(); ++j) {
                Brick upper = zOrdered.get(j);
                if(upper.isAbove(lower)) {
                    var ls = network.get(lower);
                    ls.addSupported(upper, network);
                }
            }
        }
        //network.entrySet().forEach(System.out::println);
        //counterMap.values().stream().filter(vc->vc.above.get()==0 ||)
        int desintegratable = 0;
        Set<Brick> desB = new LinkedHashSet<Day22.Brick>();
        for(int i=0; i<zOrdered.size(); ++i) {
            Brick lower = zOrdered.get(i);
           // System.out.println("ol:\t"+bricks.indexOf(lower));
            int onlySupportCount = 0;
            for(int j=i+1; j<zOrdered.size() && onlySupportCount==0; ++j) {
                Brick upper = zOrdered.get(j);
               // System.out.println("ou:\t"+bricks.indexOf(upper));
                if(upper.isAbove(lower)) {
                    boolean hasOtherSupport = false;
                    for(int k=0; k<j && !hasOtherSupport; ++k) {
                        Brick other = zOrdered.get(k);
                        //System.out.println("oo:\t"+bricks.indexOf(other));
                        if(other != lower && !lower.isAbove(other) && upper.isAbove(other)) {
                            hasOtherSupport = true;
                        }
                    }
                    if(!hasOtherSupport) {
                        ++onlySupportCount;
                    }
                }
            }
            if(onlySupportCount==0) {
                ++desintegratable;
                desB.add(lower);
            }
        }
        //338 too low
        //340 too high
        System.out.println(desintegratable);
        System.out.println(desB);
        System.out.println(desB.size());
    }
    
    private static int countDesintegrationByElimination(Map<Brick, BrickSupport> originalNetwork) {
        Map<Brick, BrickSupport> network = BrickSupport.copyNetwork(originalNetwork);
        boolean changed = false;
        Set<Brick> desintegrated = new HashSet<>();
        do {
            
        } while(changed);
        return desintegrated.size();
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day22.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    private record Brick(int x1, int y1, int z1, int x2, int y2, int z2) {
        private static final Comparator<Brick> Y_COMPARATOR = Comparator.comparingInt(Brick::y1).thenComparingInt(Brick::y2);
        private static final Comparator<Brick> Z_COMPARATOR = Comparator.comparingInt(Brick::z1).thenComparing(Comparator.comparingInt(Brick::z2).reversed());
        
        static Brick read(String line) {
            var parts = line.split("~");
            var p1s = parts[0].split(",");
            var p2s = parts[1].split(",");
            return new Brick(
                    Integer.parseInt(p1s[0]),
                    Integer.parseInt(p1s[1]),
                    Integer.parseInt(p1s[2]),
                    Integer.parseInt(p2s[0]),
                    Integer.parseInt(p2s[1]),
                    Integer.parseInt(p2s[2])
                    );
        }
        
        
        long size() {
            return (Math.abs(x1-x2)+1)*(Math.abs(y1-y2)+1)*(Math.abs(z1-z2)+1);
        }
        
        boolean contains(int x, int y, int z) {
            return x1<=x&& x<=x2 && y1<=y && y<=y2 && z1<= z && z <= z2;
        }
        
        boolean isInRightOrder() {
            return x1 <= x2 && y1 <= y2  && z1 <= z2;
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
        
        boolean hasCommonZ(Brick other) {
            Brick a = z1 <= other.z1 ? this : other;
            Brick b = this == a ? other : this;
            return a.z1 <= b.z1 && b.z1 <= a.z2;
        }
    }
    
    private record VerticalCounter(AtomicInteger above, AtomicInteger below) {
        
    }
    
    private record BrickSupport(Brick brick, SequencedSet<Brick> supports, SequencedSet<Brick> supportedBy) {
        static BrickSupport create(Brick b) {
            return new BrickSupport(b, new LinkedHashSet<>(), new LinkedHashSet<>());
        }
        
        public static Map<Brick, BrickSupport> copyNetwork(Map<Brick, BrickSupport> originalNetwork) {
            Map<Brick, BrickSupport>  result = new HashMap<Day22.Brick, Day22.BrickSupport>();
            for(var k: originalNetwork.keySet()) {
                result.put(k, originalNetwork.get(k).copy());
            }
            return result;
        }

        void addSupported(Brick upper, Map<Brick, BrickSupport> network) {
            if(supports.contains(upper)) {
                return;
            }
            supports.forEach(s -> {
                if(upper.isAbove(s)) {
                    network.get(s).addSupported(upper, network);
                }
            });
            supports.add(upper);
            supportedBy.forEach(sb -> {
                network.get(sb).addSupported(upper, network);
            });
        }
        
        BrickSupport copy() {
            return new BrickSupport(brick, new LinkedHashSet<>(supports), new LinkedHashSet<>(supportedBy));
        }
    }
    
    private static String example ="""
1,0,1~1,2,1
0,0,2~2,0,2
0,2,3~2,2,3
0,0,4~0,2,4
2,0,5~2,2,5
0,1,6~2,1,6
1,1,8~1,1,9""";
}
