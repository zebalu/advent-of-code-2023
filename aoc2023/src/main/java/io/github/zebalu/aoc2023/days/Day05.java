package io.github.zebalu.aoc2023.days;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SequencedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class Day05 {

    public static void main(String[] args) {
        String input = readInput();
        String[] cats = input.split("\n\n");
        List<Long> seeds = Arrays.stream(cats[0].substring("seeds: ".length()).split(" ")).mapToLong(Long::parseLong).boxed().toList();
        List<RangeMap> rangeMaps = Arrays.stream(cats).skip(1).map(RangeMap::fromString).toList();
        
        long min = seeds.stream().mapToLong(seed-> {
            long mapped = seed;
            for(int i=0; i<rangeMaps.size(); ++i) {
                mapped = rangeMaps.get(i).getMapping(mapped);
            }
            return mapped;
        }).min().orElseThrow();
        System.out.println(min);
        
        List<Range> seedRanges = IntStream.iterate(0, i->i+2).takeWhile(i->i<seeds.size()).mapToObj(i->new Range(seeds.get(i), seeds.get(i), seeds.get(i+1))).toList();
        List<SrcRange> srcs = SrcRange.distinct(seedRanges.stream().map(sr->new SrcRange(sr.source, sr.source+sr.length)).toList());
        for(RangeMap map: rangeMaps) {
            srcs = map.mapSrcs(srcs);
        }
        System.out.println(srcs.stream().mapToLong(SrcRange::start).min().orElseThrow());
    }
    
    private static String readInput() {
        try {
            return Files.readString(new File("day05.txt").getAbsoluteFile().toPath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Range(long source, long destintation, long length) {
        boolean isMapped(long src) {
            return source<=src && src < source + length;
        }
        long getDest(long src) {
            return destintation+(src-source);
        }
        boolean contains(SrcRange src) {
            return source <= src.start && src.end<=source+length;
        }
        SrcRange map(SrcRange src) {
            long ds = getDest(src.start);
            long de = getDest(src.end);
            return new SrcRange(ds, de);
        }
        static Range fromString(String line) {
            long[] nums= Arrays.stream(line.split(" ")).mapToLong(Long::parseLong).toArray();
            return new Range(nums[1], nums[0], nums[2]);
        }
    }
    private record RangeMap(List<Range> ranges) { 
        static RangeMap fromString(String cat) {
            return new RangeMap(cat.lines().skip(1).map(Range::fromString).toList());
        }
        
        long getMapping(long src) {
            Range range = ranges.stream().filter(r->r.isMapped(src)).findAny().orElse(new Range(src, src, 1));
            return range.getDest(src);
        }
        
        List<SrcRange> mapSrcs(List<SrcRange> srcs) {
            List<SrcRange> rangeSources = ranges.stream().map(r->new SrcRange(r.source, r.source + r.length)).toList();
            List<SrcRange> allRanges = SrcRange.intersect(srcs, rangeSources);
            List<SrcRange> subRanges = allRanges.stream().filter(range->srcs.stream().filter(seed->seed.contains(range)).findAny().isPresent()).toList();
            List<SrcRange> mapped = new ArrayList<>();
            for(SrcRange sub: subRanges) {
                List<Range> containers = ranges.stream().filter(r->r.contains(sub)).toList();
                if(containers.isEmpty()) {
                    mapped.add(sub);
                } else {
                    containers.forEach(c->mapped.add(c.map(sub)));
                }
            }
            return SrcRange.distinct(mapped);
        }
    }
    
    record SrcRange(long start, long end) implements Comparable<SrcRange> {
        private static final Comparator<SrcRange> SRC_RANGE_COMPARATOR = Comparator.comparing(SrcRange::start).thenComparingLong(SrcRange::end);
        long length() { return end-start;}
        boolean contains(long element) {return start <= element && element < end;}
        boolean contains(SrcRange other) {
            return start <= other.start && other.end <= end;
        }
        boolean hasIntersection(SrcRange other) {
            return contains(other) || start <= other.start && other.start < end && end < other.end;
        }
        boolean isEmpty() { return end <= start; }
        @Override
        public int compareTo(SrcRange other) { return SRC_RANGE_COMPARATOR.compare(this, other); }
        static List<SrcRange> intersect(SrcRange a, SrcRange b) {
            SrcRange first = a.start<=b.start?a:b;
            SrcRange second = a==first?b:a;
            if(first.equals(second)) {
                return List.of(first);
            } else if(first.contains(second)) {
                return List.of(new SrcRange(first.start, second.start), new SrcRange(second.start, second.end), new SrcRange(second.end, first.end));
            } else if(second.contains(first)) {
                return List.of(new SrcRange(first.start, first.end), new SrcRange(first.end, second.end));
            } else if(first.hasIntersection(second)) {
                return List.of(new SrcRange(first.start, second.start), new SrcRange(second.start, first.end), new SrcRange(first.end, second.end));
            } else {
                return List.of(first,second);
            }
        }
        
        static List<SrcRange> intersect(SrcRange a, List<SrcRange> bs) {
            SequencedSet<SrcRange> intersections = new TreeSet<>(SRC_RANGE_COMPARATOR);
            bs.stream().forEach(b->intersections.addAll(intersect(a, b)));
            return distinct(intersections.stream().toList());
        }
        
        static List<SrcRange> intersect(List<SrcRange> as, List<SrcRange> bs) {
            return distinct(as.stream().flatMap(a->intersect(a, bs).stream()).toList());
        }
        
        static List<SrcRange> distinct(List<SrcRange> ranges) {
            List<SrcRange> toProcess = ranges.stream().distinct().sorted().toList();
            boolean changed = false;
            do {
                SequencedSet<SrcRange> intersections = new TreeSet<>(SRC_RANGE_COMPARATOR);
                changed = false;
                for (int i = 0; i < toProcess.size() && !changed; ++i) {
                    SrcRange a = toProcess.get(i);
                    for (int j = i + 1; j < toProcess.size() && !changed; ++j) {
                        SrcRange b = toProcess.get(j);
                        if (a.hasIntersection(b)) {
                            intersections.addAll(intersect(a, b));
                            intersections.addAll(toProcess.subList(j+1, toProcess.size()));
                            changed = true;
                        }
                    }
                    if (!changed) {
                        intersections.add(a);
                    }
                }
                toProcess = intersections.stream().filter(a->a.length()>0).distinct().sorted().toList();
            } while (changed);
            return toProcess;
        }
    }
}
