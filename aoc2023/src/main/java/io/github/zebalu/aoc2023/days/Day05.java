package io.github.zebalu.aoc2023.days;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Day05 {

    public static void main(String[] args) {
        String input = readInput(); //bbbb; //aaaa; //readInput(); //aaaa; //readInput();
        String[] cats = input.split("\n\n");
        List<Long> seeds = Arrays.stream(cats[0].substring("seeds: ".length()).split(" ")).mapToLong(Long::parseLong).boxed().toList();
        System.out.println(seeds);
        Arrays.stream(cats).map(s->s.lines().limit(1).findAny().orElseThrow()).forEach(System.out::println);
        List<RangeMap> rangeMaps = Arrays.stream(cats).skip(1).map(RangeMap::fromString).toList();
        long min = seeds.stream().mapToLong(seed-> {
            long mapped = seed;
            for(int i=0; i<rangeMaps.size(); ++i) {
                mapped = rangeMaps.get(i).getMapping(mapped);
            }
            return mapped;
        }).min().orElseThrow();
        System.out.println(min);
        RangeMap seedToSoil = rangeMaps.get(0);
        System.out.println(seedToSoil);
        /*
        RangeMap soilToFertilizer = rangeMaps.get(1);
        RangeMap fertilizerToWater = rangeMaps.get(2);
        RangeMap waterToLight = rangeMaps.get(3);
        RangeMap lightToTemperature = rangeMaps.get(4);
        RangeMap temperatureToHumidity = rangeMaps.get(5);
        RangeMap humidityToLocation = rangeMaps.get(6);
        */
        System.out.println(seeds);
        List<Range> seedRanges = IntStream.iterate(0, i->i+2).takeWhile(i->i<seeds.size()).mapToObj(i->new Range(seeds.get(i), seeds.get(i), seeds.get(i+1))).toList();
        for(RangeMap rm: rangeMaps) {
           //seedRanges = rm.mapToRanges(seedRanges).stream().map(r->r.toDestinationrange()).toList();
            /*
            seedRanges = rangeMaps.get(0).mapToRanges(seedRanges).stream().map(r->r.toDestinationrange()).toList();
            seedRanges = rangeMaps.get(1).mapToRanges(seedRanges).stream().map(r->r.toDestinationrange()).toList();
            seedRanges = rangeMaps.get(2).mapToRanges(seedRanges).stream().map(r->r.toDestinationrange()).toList();
            */
        }
        System.out.println("size: "+seedRanges.size());
        System.out.println(seedRanges.stream().filter(sr->sr.length>0).mapToLong(sr->sr.source).min().orElseThrow());
        System.out.println(new Range(3,10,5).getIntersection(new Range(2,2, 2)));
        //System.out.println(new Range(3,10,5).minTogether(new Range(5,5, 1)).stream()/*.map(r->r.toDestinationrange())*/.toList());
        //System.out.println(rangeMaps.get(0).mapToRanges(List.of(seedRanges.get(0))));
        /*System.out.println(seedRanges.stream().flatMapToLong(r->LongStream.range(r.source, r.source+r.length)).parallel().map(seed-> {
            long mapped = seed;
            for(int i=0; i<rangeMaps.size(); ++i) {
                mapped = rangeMaps.get(i).getMapping(mapped);
            }
            return mapped;
        }).min().orElseThrow());*/
        System.out.println(SrcRange.intersect(new SrcRange(3, 7), new SrcRange(6, 10)));
        System.out.println("----");
        System.out.println(new SrcRange(5, 9).hasIntersection(new SrcRange(7, 9)));
        System.out.println(SrcRange.intersect(new SrcRange(4, 9), new SrcRange(2,5)));
        System.out.println(SrcRange.intersect(new SrcRange(4, 9), new SrcRange(7,11)));
        System.out.println(SrcRange.intersect(new SrcRange(4, 9), List.of(new SrcRange(2,5), new SrcRange(7,11))));
        System.out.println("------------");
        System.out.println(SrcRange.intersect(List.of(new SrcRange(3,8), new SrcRange(10, 15)), List.of(new SrcRange(1,4), new SrcRange(6,12))));
        List<SrcRange> srcs = SrcRange.distinct(seedRanges.stream().map(sr->new SrcRange(sr.source, sr.source+sr.length)).sorted(SrcRange.SRC_RANGE_COMPARATOR).distinct().toList());
        System.out.println("mindenek elott:\t"+srcs);
        for(RangeMap map: rangeMaps) {
            System.out.println("idx: "+rangeMaps.indexOf(map));
            System.out.println("pre: "+srcs.size());
            srcs = map.mapSrcs(srcs);
            System.out.println("post: "+srcs.size());
            //System.out.println(srcs);
        }
        System.out.println(srcs);
        System.out.println(srcs.stream().mapToLong(SrcRange::start).min().orElseThrow());
        srcs = IntStream.iterate(0, i->i+2).takeWhile(i->i<seeds.size()).mapToObj(i->new SrcRange(seeds.get(i), seeds.get(i)+seeds.get(i+1))).toList();
        System.out.println("sum:\t"+srcs.stream().mapToLong(SrcRange::length).sum());
        Set<Long> top = new HashSet<>();
        /*for(var src: srcs) {
            for(long ll = src.start; ll<src.end; ++ll) {
                top.add(ll);
            }
        }*/
        long min2 = Long.MAX_VALUE;
        for(var s: srcs) {
            System.out.println(Instant.now()+"\t\t"+srcs.indexOf(s)+"\tsize:\t"+s.length()+"\tsrcs size:\t"+srcs.size());
            var cmin = LongStream.range(s.start, s.end).parallel().map(l->{
                long lll = l;
                for(var rm: rangeMaps) {
                    lll = rm.getMapping(lll);
                }
                return lll;
            }).min().orElseThrow();
            if(cmin<min2) {
                min2 = cmin;
            }
            System.out.println("min so far:\t"+min2);
        }
        System.out.println("min2: "+min2);
        for(var rm: rangeMaps) {
            System.out.println("idx:\t"+rangeMaps.indexOf(rm));
            System.out.println("to process: "+top.size());
            Set<Long> mm = new HashSet<>();
            //System.out.println(top);
            for(long l: top) {
                mm.add(rm.getMapping(l));
            }
            //System.out.println(mm);
            top = mm;
            System.out.println("....");
        }
        /*
        System.out.println(top.stream().mapToLong(Long::longValue).min().orElseThrow());
        var ssee = List.of(new SrcRange(1, 11), new SrcRange(12, 13));
        RangeMap rm = new RangeMap(List.of(new Range(1, 20, 3), new Range(5,30, 36)));
        System.out.println("te: "+rm.mapSrcs(ssee));
        */
        
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
        boolean hasIntersection(Range otherRange) {
            return source <=otherRange.source() && otherRange.source() <= source+length ||
                    otherRange.source() <= source && source <= otherRange.source() + otherRange.length();
        }
        Range getIntersection(Range other) {
            if(contains(other)) {
                return new Range(other.source, destintation+(other.source-source), other.length);
            } else {
                if(source<other.source) {
                    return new Range(other.source, destintation+(other.source-source), (source+length)-other.source-1);
                } else {
                    return new Range(source, destintation, (other.source+other.length)-source);
                }
            }
            /*
            Range first = source<other.source?this:other;
            Range second = first == this? other: this;
            long start = second.source;
            long len = !first.contains(second)? first.source+first.length-start-1:second.length;
            return new Range(second.source, this.destintation+(second.source-first.source), len);
            */
        }
        Range toDestinationrange () {
            return new Range(destintation, destintation, length);
        }
        boolean contains(Range other) {
            return source <= other.source && other.source + other.length <= source + length;
        }
        boolean contains(SrcRange src) {
            return source <= src.start && src.end<=source+length;
        }
        SrcRange map(SrcRange src) {
            long ds = getDest(src.start);
            long de = getDest(src.end);
            return new SrcRange(ds, de);
        }
        List<Range> minTogether(Range other) {
            Range first = source<other.source?this:other;
            Range second = first == this? other: this;
            if(!first.hasIntersection(other)) {
                return List.of(first, second);
            } else {
                if(contains(other)) {
                    Range inters = getIntersection(other);
                    return List.of(new Range(source, destintation, source+length-inters.source-1),
                            inters,
                            new Range(inters.source+inters.length, inters.destintation+inters.length, (source+length)-(inters.source+inters.length)-1));
                } else {
                    
                
                if(source <= other.source) {
                    Range inters = getIntersection(other);
                    return List.of(new Range(source, destintation, source+length-inters.source-1),
                            inters,
                            new Range(inters.source+inters.length, other.destintation+inters.length, (source+length)-(inters.source+inters.length)-1));
                            
                } else {
                    Range inters = getIntersection(other);
                    return List.of(new Range(other.source, other.destintation, other.source+other.length-inters.source-1),
                            inters,
                            new Range(inters.source+inters.length, inters.destintation+inters.length, (source+length)-(inters.source+inters.length)-1));
                }
                }
                /*
            Range intersection = getIntersection(other);
                if(first.contains(other)) {
                    return List.of(
                            new Range(first.source, first.destintation, first.source+first.length-intersection.source-1),
                            intersection,
                            new Range(intersection.source+intersection.length, intersection.destintation+intersection.length, (first.source+first.length)-(intersection.source+intersection.length)-1)
                            );
                } else {
                    return List.of(
                            new Range(first.source, first.destintation, first.source+first.length-intersection.source-1),
                            intersection,
                            new Range(intersection.source+intersection.length+1, intersection.destintation+intersection.length, (second.source+second.length)-(intersection.source+intersection.length)-1)
                            );
                }*/
            }
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
        
        List<Range> mapToRanges(List<Range> incommingRanges) {
            List<Range> result = new ArrayList<>();
            for(Range incommingRange: incommingRanges) {
                var intersectables = ranges.stream().filter(r->r.hasIntersection(incommingRange)).toList();
                if(intersectables.isEmpty()) {
                    result.add(incommingRange);
                } else {
                    intersectables.stream().flatMap(i->i.minTogether(incommingRange).stream()).forEach(result::add);
                }
            }
            return result.stream().filter(r->r.length>0).sorted(Comparator.comparingLong(Range::source)).toList();
        }
        
        List<SrcRange> mapSrcs(List<SrcRange> srcs) {
            List<SrcRange> rangeSources = ranges.stream().map(r->new SrcRange(r.source, r.source + r.length)).toList();
           // var bela = SrcRange.intersect(List.of(new SrcRange(126,128)), List.of(new SrcRange(100, 126), new SrcRange(127, 228)));
            var alma = SrcRange.intersect(srcs, rangeSources);
            System.out.println("alma: "+alma);
            List<SrcRange> subRanges = alma.stream().filter(sr->srcs.stream().filter(s->s.contains(sr)).findAny().isPresent()).toList();
            System.out.println("subR: "+subRanges);
            List<SrcRange> mapped = new ArrayList<>();
            for(SrcRange sub: subRanges) {
                List<Range> containers = ranges.stream().filter(r->r.contains(sub)).toList();
                //System.out.println(this+":\n"+containers+"\t"+sub);
                if(containers.isEmpty()) {
                    mapped.add(sub);
                } else {
                    //System.out.println("map: "+containers.getFirst().map(sub));
                    containers.forEach(c->mapped.add(c.map(sub)));
                }
            }
            mapped.sort(SrcRange.SRC_RANGE_COMPARATOR);
            return SrcRange.distinct(mapped);
        }
        
    }
    
    record SrcRange(long start, long end) {
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
            /*
            List<SrcRange> against = bs;
            for(var a : as) {
                List<SrcRange> coll = new ArrayList<>(against);
                coll.addAll(intersect(a, against));
                against = distinct(coll);
                System.out.println(a+"\t->\t"+against);
            }
            return against;
            */
            
            var ll = as.stream().flatMap(a->intersect(a, bs).stream()).collect(Collectors.toSet());
            var ll2 = ll.stream().sorted(SRC_RANGE_COMPARATOR).toList();
            System.out.println("ll2: "+ll2);
            System.out.println("distinct: "+distinct(ll2));
            return distinct(new ArrayList<>(ll2));
        }
        
        static List<SrcRange> distinct(List<SrcRange> ranges) {
            List<SrcRange> toProcess = ranges;
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
               // System.out.println("precut: "+intersections);
                toProcess = intersections.stream().filter(a->a.length()>0).toList();
               // System.out.println("post:   "+toProcess);
               // System.out.println("");
            } while (changed);
            return toProcess;
        }
    }
    private static final String aaaa="""
seeds: 79 14 55 13

seed-to-soil map:
50 98 2
52 50 48

soil-to-fertilizer map:
0 15 37
37 52 2
39 0 15

fertilizer-to-water map:
49 53 8
0 11 42
42 0 7
57 7 4

water-to-light map:
88 18 7
18 25 70

light-to-temperature map:
45 77 23
81 45 19
68 64 13

temperature-to-humidity map:
0 69 1
1 0 69

humidity-to-location map:
60 56 37
56 93 4""";
    private static final String bbbb="""
            seeds: 0 6 10 3

            seed-to-soil map:
            6 10 3
            121 0 1
            
            laci-to-maci map:
            122 0 1
            123 3 5
            200 100 101
            
            2-to-3 map:
            2 1 5
            10 100 26
            20 127 101""";
}
