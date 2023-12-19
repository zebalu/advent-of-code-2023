package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Day19 {
    public static void main(String[] args) {
        String input = readInput();
        String[] inputParts = input.split("\n\n");
        Machine machine = Machine.read(inputParts[0]);
        List<Part> parts = inputParts[1].lines().map(Part::read).toList();
        System.out.println(parts.stream().filter(machine::check).reduce((p1, p2)->p1.add(p2)).orElseThrow().sum());
        System.out.println(machine.calcRanges(new PartRanges(List.of(new IntRange(1, 4000)), List.of(new IntRange(1, 4000)), List.of(new IntRange(1, 4000)), List.of(new IntRange(1, 4000)))));
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day19.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Part(int x, int m, int a, int s) {
        int get(char ch) {
            return switch (ch) {
            case 'x' -> x;
            case 'm' -> m;
            case 'a' -> a;
            case 's' -> s;
            default -> throw new IllegalArgumentException("unkown value requested: "+ch);
            };
        }
        
        Part add(Part o) {
            return new Part(x+o.x, m+o.m, a+o.a, s+o.s);
        }
        
        long sum() {
            return 0L + x + m +a +s;
        }
        
        static Part read(String line) {
            int x = Integer.MIN_VALUE;
            int m = Integer.MIN_VALUE;
            int a = Integer.MIN_VALUE;
            int s = Integer.MIN_VALUE;
            for(var p : line.substring(1, line.length()-1).split(",")) {
                if(p.startsWith("x=")) {
                    x=parseSub(p);
                } else if(p.startsWith("m=")) {
                    m=parseSub(p);
                }else if(p.startsWith("a=")) {
                    a=parseSub(p);
                }else if(p.startsWith("s=")) {
                    s=parseSub(p);
                }else {
                    throw new IllegalStateException("Unknown String: "+p);
                }
            }
            return new Part(x, m, a, s);
        }
        
        private static int parseSub(String str) {
            return Integer.parseInt(str.substring(2));
        }
    }
    
    private record Rule(char selector, char operator, int value, String result) {
        String doCheck(Part p) {
            if(selector == '*') {
                return result;
            } else {
                return switch(operator) {
                case '<' -> p.get(selector) < value ? result : null;
                case '>' -> p.get(selector) > value ? result : null;
                default -> throw new IllegalStateException("unknown operator: "+operator);
                };
            }
        }
        
        Step toAccepted(PartRanges ranges) {
            if(selector == '*') {
                return new Step(result, ranges);
            }
            List<IntRange> before = new ArrayList<>();
            List<IntRange> after = new ArrayList<>();
            fillBeforeAfter(ranges, before, after);
            if(before.isEmpty() && after.isEmpty()) {
                return null;
            }
            if(operator == '<') {
                return new Step(result, ranges.set(selector, before));
            } else {
                return new Step(result, ranges.set(selector, after));
            }
        }
        
        PartRanges toRejected(PartRanges ranges) {
            if(selector == '*') {
                return ranges;
            }
            List<IntRange> before = new ArrayList<>();
            List<IntRange> after = new ArrayList<>();
            fillBeforeAfter(ranges, before, after);
            if(before.isEmpty() && after.isEmpty()) {
                return ranges;
            }
            if(operator == '<') {
                return ranges.set(selector, after);
            } else {
                return ranges.set(selector, before);
            }
        }
        
        private void fillBeforeAfter(PartRanges ranges, List<IntRange> before, List<IntRange> after) {
            List<IntRange> toCheck = ranges.get(selector);
            if(toCheck.isEmpty()) {
                return;
            }
            for(var ir: toCheck) {
                if(!ir.contains(value) && ir.max<value) {
                    before.add(ir);
                } else if(!ir.contains(value) && value<ir.min) {
                    after.add(ir);
                } else if(ir.contains(value)) {
                    List<IntRange> cuted = ir.cut(value, operator);
                    if(!cuted.isEmpty()) {
                        if(cuted.size() == 1) {
                            IntRange r = cuted.getFirst();
                            if(r.length()!=1) {
                                if(operator == '<') {
                                    before.add(r);
                                } else {
                                    after.add(r);
                                }
                            }
                                
                        } else {
                            before.add(cuted.getFirst());
                            after.add(cuted.getLast());
                        }
                    }
                } else {
                    throw new IllegalStateException("Should not happen: "+this+"\tranges: "+ranges+"\tir:"+ir);
                }
            }
        }
    }
    
    private record Algorithm(String name, List<Rule> rules) {
        static Algorithm read(String line) {
            var parts = line.substring(0, line.length()-1).split("\\{");
            String name = parts[0];
            String[] preRules = parts[1].split(",");
            List<Rule> rules = Arrays.stream(preRules).map((String str)->{
                if(str.contains(":")) {
                    var rps = str.split(":");
                    char val = rps[0].charAt(0);
                    char operand = rps[0].charAt(1);
                    int intVal = Integer.parseInt(rps[0].substring(2));
                    return new Rule(val, operand, intVal, rps[1]);
                } else {
                    return new Rule('*', '?', Integer.MIN_VALUE, str);
                }
            }).toList();
            return new Algorithm(name, rules);
        }
        
        String execute(Part part) {
            String result = null;
            for(int i=0; i<rules.size() && result == null; ++i) {
                result = rules.get(i).doCheck(part);
            }
            return result;
        }
    }
    
    private record Machine(Map<String, Algorithm> tree) {
        private static final String START_VALUE = "in";
        private static final String ACCEPT = "A";
        private static final String REJECT = "R";
        static Machine read(String string) {
            return new Machine(string.lines().map(Algorithm::read).collect(Collectors.toMap(a->a.name(), a->a)));
        }
        boolean check(Part p) {
            return check(START_VALUE, p);
        }
        
        private boolean check(String startValue, Part p) {
            String val = startValue;
            while(!(ACCEPT.equals(val) || REJECT.equals(val))) {
                val = tree.get(val).execute(p);
            }
            return ACCEPT.equals(val);
        }
        
       long calcRanges(PartRanges partRange) {
           long result = 0L;
           Queue<Step> queue = new LinkedList<>();
           queue.add(new Step(START_VALUE, partRange));
           while(!queue.isEmpty()) {
               Step step = queue.poll();
               if(ACCEPT.equals(step.name)) {
                   result += step.ranges().size();
               } else if(!REJECT.equals(step.name())) {
                   Algorithm alg = tree.get(step.name());
                   PartRanges current = step.ranges();
                   for (Rule rule : alg.rules) {
                       Step accepted = rule.toAccepted(current);
                       if (accepted != null) {
                           queue.add(accepted);
                           current = rule.toRejected(current);
                       }
                   }
               }
           }
           return result;
       }
    }
    
    private record IntRange(int min, int max) {
        boolean contains(int v) {
            return min <= v && v <= max;
        }
        boolean internal(int v) {
            return min < v && v < max;
        }
        int length() {
            return max - min + 1;
        }
        List<IntRange> cut(int at, char operator) {
            if(!contains(at)) {
                return List.of(this);
            } else {
                if(internal(at)) {
                    if(operator == '<') {
                        return List.of(new IntRange(min, at-1), new IntRange(at, max));
                    } else {
                        return List.of(new IntRange(min, at), new IntRange(at+1, max));
                    }
                } else if (length()==1) {
                    return List.of(this);
                } else {
                    if(min == at) {
                        if(operator == '<') {
                            return List.of(this);
                        } else {
                            return List.of(new IntRange(min, min), new IntRange(at+1, max));
                        }
                    } else {
                        if(operator == '<') {
                            return List.of(new IntRange(min, at-1), new IntRange(max, max));
                        } else {
                            return List.of(this);
                        }
                    }
                }
            }
        }
    }
    
    private record PartRanges(List<IntRange> x, List<IntRange> m, List<IntRange> a, List<IntRange> s) {
        List<IntRange> get(char ch) {
            return switch (ch) {
            case 'x' -> x;
            case 'm' -> m;
            case 'a' -> a;
            case 's' -> s;
            default -> throw new IllegalArgumentException("unkown value requested: "+ch);
            };
        }
        
        PartRanges set(char ch, List<IntRange> ranges) {
            return switch (ch) {
            case 'x' -> new PartRanges(ranges, m, a, s);
            case 'm' -> new PartRanges(x, ranges, a, s);
            case 'a' -> new PartRanges(x, m, ranges, s);
            case 's' -> new PartRanges(x, m, a, ranges);
            default -> throw new IllegalArgumentException("unkown value requested: "+ch);
            };
        }
        
        long size() {
            return 1L * sizeOf(x) * sizeOf(m) * sizeOf(a) * sizeOf(s);
        }
        
        private long sizeOf(List<IntRange> ranges) {
            return ranges.stream().mapToLong(IntRange::length).reduce(1L, (a,b)->a*b);
        }
    }
    
    private record Step(String name, PartRanges ranges) {
        
    }
}
