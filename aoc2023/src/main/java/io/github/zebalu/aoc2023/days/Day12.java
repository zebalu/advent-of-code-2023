package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day12 {
    public static void main(String[] args) {
        String input = readInput();//readInput();
        var as = input.lines().map(Arrangement::parse).toList();
        System.out.println(as.stream().parallel().mapToLong(a->count(a, new State(0,0,0), new HashMap<>())).sum());
        System.out.println(as.stream().map(a->a.unFold(5)).parallel().mapToLong(a->count(a, new State(0,0,0), new HashMap<>())).sum());
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day12.txt").toAbsolutePath());
        } catch (IOException e) {
        throw new IllegalStateException(e);
        }
    }
    
    private record Arrangement(char[] desc, List<Integer> nums) {
        static Arrangement parse(String line) {
            var parts = line.split(" ");
            return new Arrangement(parts[0].toCharArray(), Arrays.stream(parts[1].split(",")).map(Integer::parseInt).toList());
        }
        
        boolean isFilled() {
            return IntStream.range(0, desc.length).mapToObj(i->(Character)desc[i]).allMatch(c->c!='?');
        }
        
        boolean isCorrect() {
            List<Integer> counted = countHasheGroups();
            if(counted.size()!=nums.size()) {
                return false;
            } else {
                for(int i=0; i<nums.size(); ++i) {
                    if(nums.get(i) != counted.get(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        // original part1: slow
        long countPossibles(Set<String> visited) {
            visited.add(asString());
            if(isCorrect()) {
                return 1L;
            } else if(isFilled()) {
                return 0L;
            } else {
                return extensions(visited).mapToLong(a->a.countPossibles(visited)).sum();
            }
        }
        
        Stream<String> asPossibleStrings(Set<String> visited) {
            visited.add(asString());
            if(isCorrect()) {
                return Stream.of(asString());
            } else if(isFilled()) {
                return Stream.empty();
            } else {
                return extensions(visited).flatMap(a->a.asPossibleStrings(visited));
            }
        }
        
        String asString() {
            return new String(desc);
        }
        
        Stream<Arrangement> extensions(Set<String> visited) {
            if(isFilled()) {
                return Stream.empty();
            } else {
                return IntStream.range(0, desc.length).filter(i->desc[i]=='?').mapToObj(i->{
                    //List<Integer> cpy = new ArrayList<>(nums);
                    char[] nDesc = Arrays.copyOf(desc, desc.length);
                    nDesc[i] = '#';
                    return new Arrangement(nDesc, nums);
                }).filter(a->!visited.contains(a.asString()));
            }
        }
        
        List<Integer> countHasheGroups() {
            List<Integer> result = new ArrayList<>();
            int current = 0;
            for(int i=0; i<desc.length; ++i) {
                if(desc[i]=='#') {
                    ++current;
                } else if(current != 0) {
                    result.add(current);
                    current = 0;
                }
            }
            if(current!=0) {
                result.add(current);
            }
            return result;
        }
        
        Arrangement unFold(int folds) {
            List<Character> newChars = new ArrayList<>();
            List<Integer> newNums = new ArrayList<>();
            for(int i=0; i<folds; ++i) {
                for(int j=0; j<desc.length; ++j) {
                    newChars.add(desc[j]);
                }
                newNums.addAll(nums);
                if(i<folds-1) {
                    newChars.add('?');
                }
            }
            char[] nc = new char[newChars.size()];
            for(int i=0; i<nc.length; ++i) {
                nc[i] = newChars.get(i);
            }
            return new Arrangement(nc, newNums);
        }
        
    }
    
    private record State(int pos, int blockPos, long currLen) {
        
    }
    
    // credits: https://github.com/jonathanpaulson/AdventOfCode/blob/master/2023/12.py
    private static long count(Arrangement arrangement, State state, Map<State, Long> prev) {
        if (prev.containsKey(state)) {
            return prev.get(state);
        }
        if (state.pos == arrangement.desc.length) {
            if (state.blockPos == arrangement.nums.size() && state.currLen == 0) {
                return 1L;
            } else if (state.blockPos == arrangement.nums.size() - 1
                    && arrangement.nums.get(state.blockPos) == state.currLen) {
                return 1L;
            } else {
                return 0L;
            }
        }
        char at = arrangement.desc[state.pos];
        long sum = 0L;
        for (char c : new char[] { '.', '#' }) {
            if (at == c || at == '?') {
                if (c == '.' && state.currLen == 0) {
                    sum += count(arrangement, new State(state.pos + 1, state.blockPos, state.currLen), prev);
                } else if (c == '.' && state.currLen > 0 && state.blockPos < arrangement.nums.size()
                        && arrangement.nums.get(state.blockPos) == state.currLen) {
                    sum += count(arrangement, new State(state.pos + 1, state.blockPos + 1, 0), prev);

                } else if (c == '#') {
                    sum += count(arrangement, new State(state.pos + 1, state.blockPos, state.currLen + 1), prev);
                }
            }
        }
        prev.put(state, sum);
        return sum;
    }
    
    // original part 2: many minutes
    private static Set<List<Integer>> findAllPos(int fromC, int fromL, Arrangement arrangement) {
        if(fromL==arrangement.nums.size()) {
            return Set.of(new ArrayList<>());
        }
        Set<List<Integer>> result = new HashSet<>();
        boolean shouldStop = false;
        for(int i=fromC; i<arrangement.desc.length; ++i) {
            List<Integer> current = new ArrayList<>();
            boolean isPossible = true;

                int length = arrangement.nums.get(fromL);
                int pos = canStartFrom(i, length, arrangement.desc);
                if(pos<0) {
                    current.clear();
                    isPossible = false;
                    //return Set.of(new ArrayList<>());
                } else {
                    Set<List<Integer>> found =  findAllPos(pos+length+1, fromL+1, arrangement);
                    found.stream().peek(l-> l.add(0, pos)).forEach(result::add);
                    if(contains(pos, pos+length, '#', arrangement.desc)) {
                        return result;
                    }
                }

        }
        return result;
    }
    
    private static boolean contains(int start, int end, char ch, char[] chars) {
        for(int i=start; i<end; ++i) {
            if(chars[i] == ch) {
                return true;
            }
        }
        return false;
    }

    private static int canStartFrom(int start, int length, char[] chars) {
        if(chars.length<start+length) {
            return -1;
        }
        for(int i=start; i<chars.length; ++i) {
            if(chars[i]!='.') {
                if(isAllPossible(chars, i, length)) {
                    return i;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    private static boolean isAllPossible(char[] chars, int pos, int length) {
        if(chars.length<pos+length) {
            return false;
        } else {
            for (int i = pos; i < pos + length; ++i) {
                if (chars[i] == '.') {
                    return false;
                }
            }
            if(pos>0 && pos+length<chars.length) {
                return chars[pos-1]!='#' && chars[pos+length] != '#';
            }
            if(pos == 0) {
                if(length == chars.length) {
                    return true;
                }
                return chars[pos+length+1] != '#';
            }
            if(pos+length == chars.length) {
                return chars[pos-1] != '#';
            }
            return true;
        }
    }

    private static final String example = """
???.### 1,1,3
.??..??...?##. 1,1,3
?#?#?#?#?#?#?#? 1,3,1,6
????.#...#... 4,1,1
????.######..#####. 1,6,5
?###???????? 3,2,1""";
    
}
