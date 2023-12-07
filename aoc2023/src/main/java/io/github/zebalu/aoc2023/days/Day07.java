package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.LongStream;

public class Day07 {
public static void main(String[] args) {
    var sl = readInput().lines().map(Game::fromString).sorted().toList();
    System.out.println(LongStream.range(0L, sl.size()).map(v->(v+1)*sl.get((int)v).bid()).sum());
    
    var jl = sl.stream().sorted(Game.JOKER_GAME_COMPARATOR).toList();
    System.out.println(LongStream.range(0L, jl.size()).map(v->(v+1)*jl.get((int)v).bid()).sum());
    
}
private static String readInput() {
    try {
        return Files.readString(Path.of("day07.txt").toAbsolutePath());
    } catch(IOException e) {
        throw new IllegalStateException(e);
    }
}
private record Game(String hand, long bid, Kind kind, String replacedHand, Kind replaced) implements Comparable<Game> {
    private static final Comparator<Game> GAME_COMPARATOR = Comparator.comparingInt(Game::rank).thenComparingLong(Game::numValue);
    
    private static final Comparator<Game> INTERNAL_COMPARATOR = (left, right) -> {
        for(int i=0; i<5; ++i) {
            int c = Integer.compare(jokerCardValue(left.hand.substring(i, i+1)), jokerCardValue(right.hand.substring(i, i+1)));
            if(c != 0) {
                return c;
            }
        }
        return 0;  
    };

    private static final Comparator<Game> JOKER_GAME_COMPARATOR = Comparator.comparingInt(Game::replacedRank).thenComparing(INTERNAL_COMPARATOR);
    
    long numValue() {
        long val = 0;
        for(int i=0; i<hand.length(); ++i) {
            int v = simpleCardValue(hand.substring(i, i+1));
            val = val * 100 + v;
        }
        return val;
    }
    static int simpleCardValue(String str) {
        if(str.length() != 1) {
            throw new IllegalStateException("'"+str+"' is not the right length");
        }
        return switch(str.charAt(0)) {
        case '2' -> 1;
        case '3' -> 2;
        case '4' -> 3;
        case '5' -> 4;
        case '6' -> 5;
        case '7' -> 6;
        case '8' -> 7;
        case '9' -> 8;
        case 'T' -> 9;
        case 'J' -> 10;
        case 'Q' -> 11;
        case 'K' -> 12;
        case 'A' -> 13;
        default -> throw new IllegalStateException("Unknonwn card");
        };
    }
    static int jokerCardValue(String str) {
        if(str.length() != 1) {
            throw new IllegalStateException("'"+str+"' is not the right length");
        }
        return switch(str.charAt(0)) {
        case '2' -> 1;
        case '3' -> 2;
        case '4' -> 3;
        case '5' -> 4;
        case '6' -> 5;
        case '7' -> 6;
        case '8' -> 7;
        case '9' -> 8;
        case 'T' -> 9;
        case 'J' -> 0;
        case 'Q' -> 11;
        case 'K' -> 12;
        case 'A' -> 13;
        default -> throw new IllegalStateException("Unknonwn card: "+str);
        };
    }
    int rank() {
        return kind.rank;
    }
    int replacedRank() {
        return replaced.rank;
    }
    @Override
    public int compareTo(Game other) {
        return GAME_COMPARATOR.compare(this, other);
    }
    static Game fromString(String line) {
        var parts = line.split(" ");
        var replaced = replace(parts[0]);
        return new Game(parts[0], Long.parseLong(parts[1]), Kind.getKind(parts[0]), replaced, Kind.getKind(replaced));
    }
    static String replace(String hand) {
        var mapped = Kind.mapHand(hand);
        if(mapped.containsKey("J")) {
            List<String> p = List.of("2", "3", "4", "5", "6", "7", "8", "9", "T", "Q", "K", "A");
            List<Game> gs = p.stream().map(s->hand.replace("J", s)).map(r->new Game(r, 0, Kind.getKind(r), r, Kind.getKind(r))).sorted().toList();
            return gs.getLast().hand();
        }
        return hand;
    }
}
private static enum Kind {
    FIVE(6, m->m.size()==1 && isExpectedValue(m.values(), List.of(5))), 
    FOUR(5, m->m.size() == 2 && isExpectedValue(m.values(), List.of(4,1))), 
    FULL(4, m->m.size() == 2 && isExpectedValue(m.values(), List.of(3,2))), 
    THREE(3, m->m.size() == 3 && isExpectedValue(m.values(), List.of(3,1,1))), 
    TWO(2, m->m.size() == 3 && isExpectedValue(m.values(), List.of(2,2,1))), 
    ONE(1, m->m.size() == 4 && isExpectedValue(m.values(), List.of(2,1,1,1))), 
    HIGH(0, m->m.size() == 5 && isExpectedValue(m.values(), List.of(1,1,1,1,1)));
    private final int rank;
    private final Function<Map<String, Integer>, Boolean> accepts;
    private Kind(int rank, Function<Map<String, Integer>, Boolean> accepts) {
        this.rank=rank;
        this.accepts = accepts;
    }
    
    private boolean accepts(String hand) {
        Map<String, Integer> counter = mapHand(hand);
        return accepts.apply(counter);
    }
    
    static Map<String, Integer> mapHand(String hand) {
        Map<String, Integer> counter = new HashMap<>();
        for(int i=0; i<hand.length(); ++i) {
            counter.compute(hand.substring(i, i+1), (k,v)->v==null?1:v+1);
        }
        return counter;
    }
    
    private static boolean isExpectedValue(Collection<Integer> values, List<Integer> expected) {
        List<Integer> ordered = values.stream().sorted(Comparator.reverseOrder()).toList();
        for(int i=0; i<ordered.size(); ++i) {
            if(ordered.get(i) != expected.get(i)) {
                return false;
            }
        }
        return true;
    }
    private static Kind getKind(String hand) {
        return Arrays.stream(Kind.values()).filter(k->k.accepts(hand)).findAny().orElseThrow();
    }
}
}