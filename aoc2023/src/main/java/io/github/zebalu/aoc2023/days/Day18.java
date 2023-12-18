package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SequencedSet;
import java.util.Set;

public class Day18 {
    public static void main(String[] args) {
        String input = readInput(); //example;//readInput(); //example;//readInput();
        var instructs = input.lines().map(DigInstruction::fromString).toList();
        System.out.println(calcArea(instructs));
        var decoded = instructs.stream().map(DigInstruction::convertColor).toList();
        System.out.println(calcArea(decoded));
    }

    private static void part1(List<DigInstruction> instructs) {
        SequencedSet<Coord> line = new LinkedHashSet<Day18.Coord>();
        List<SequencedSet<Coord>> lines = new ArrayList<>();
        line.add(new Coord(0,0));
        for(var inst: instructs) {
            Coord at = line.getLast();
            SequencedSet<Coord> seq = new LinkedHashSet<>();
            seq.add(at);
            switch (inst.dir()) {
            case 'R' -> {
                for(int i=1; i<=inst.length; ++i) {
                    line.add(new Coord(at.x+i, at.y));
                    seq.add(line.getLast());
                }
            }
            case 'L' -> {
                for(int i=1; i<=inst.length; ++i) {
                    line.add(new Coord(at.x-i, at.y));
                    seq.add(line.getLast());
                }
            }
            case 'U' -> {
                for(int i=1; i<=inst.length; ++i) {
                    line.add(new Coord(at.x, at.y-i));
                    seq.add(line.getLast());
                }
            }
            case 'D' -> {
                for(int i=1; i<=inst.length; ++i) {
                    line.add(new Coord(at.x, at.y+i));
                    seq.add(line.getLast());
                }
            }
            }
            lines.add(seq);
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY= Integer.MAX_VALUE;
        int maxY=Integer.MIN_VALUE;
        for(Coord c: line) {
            minX = Math.min(minX, c.x);
            maxX = Math.max(maxX, c.x);
            minY = Math.min(minY, c.y);
            maxY = Math.max(maxY, c.y);
        }
        int count = 0;
        Set<Coord> inside = new HashSet<>();
        Set<Coord> outside = new HashSet<>();
        for(int x=minX; x<=maxX; ++x) {
            for(int y=minY; y<=maxY; ++y) {
                Coord c=new Coord(x,y);
                /*
                if(y==7 && x == 2) {
                    System.out.println("??\t"+c);
                }
                if(line.contains(c)) {
                    inside.add(c);
                    ++count;
                } else {
                    int cross = 0;
                    List<SequencedSet<Coord>> prev = null;
                    for(int x2 = minX; x2<=x; ++x2) {
                        Coord c2= new Coord(x2,y);
                        if(line.contains(c2)) {
                            var found = findLines(lines, c2);
                            if(found.size()>2) {
                                throw new IllegalStateException("too many lines");
                            }
                            if(prev == null) {
                                prev=found;
                            } else {
                                if(prev.size()==1 && found.size()==1) {
                                    if(!prev.getFirst().equals(found.getFirst())) {
                                        cross += 1;
                                        prev = found;
                                    }
                                } else if(prev.size()==1 && found.size()==2) {
                                    if(!found.contains(prev.getFirst())) {
                                        cross +=1;
                                        prev=found;
                                    }
                                } else if(prev.size()==2 && found.size()==1) {
                                    if(prev.contains(found.getFirst())) {
                                        prev = found;
                                    } else {
                                        cross +=1;
                                        prev = found;
                                    }
                                } else if(prev.size()==2 && found.size()==2) {
                                    if(found.contains(prev.getFirst())||found.contains(prev.getLast()) ) {
                                        prev = found;
                                    } else {
                                    cross += 1;
                                    prev = found;
                                    }
                                }
                            }
                        } else {
                            if(prev!=null) {
                                ++cross;
                                prev = null;
                            }
                        }
                    }
                    if(cross%2==1) {
                        ++count;
                        inside.add(c);
                    }
                }
                */
                if(!line.contains(c)) {
                    explore(c, minX, maxX, minY, maxY, inside, outside, line);
                }
            }
        }
        // wrong 42590
        System.out.println(count+"\t"+inside.size());
        /*
        StringBuilder sb = new StringBuilder();

        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                Coord c = new Coord(x, y);
                if (line.contains(c)) {
                    sb.append(' ');
                } else if(inside.contains(c)) {
                    sb.append('#');
                }                else {
                    sb.append('.');
                }
            }
            sb.append('\n');
        }
        System.out.println(sb.toString());
        */
        System.out.println(inside.size()+line.size());
    }
    
    /**
     * <p>This method applies the <a href="https://en.wikipedia.org/wiki/Shoelace_formula">Trapezoid Shoelace formula</a>.</p>
     * 
     * <p>I have learned about this reading Day10 solutions earlier.</p>
     * 
     * <p>The formula requires a list of coordinates in a counter-clockwise manner.<br /> 
     * The coordinates are the different points of a polygon.<br />
     * The input (DigPlan) guarantees we get the points in order, but it can be clockwise, in this case the area is
     * negative.</p>
     * 
     * <p>The formula states for n points: area = 1/2 SUM{i: 1->n}(y_i + y_[i+1]))*(x_i - x_[i+1])</p>
     * 
     * <p>Other additions: based on the example and part1 I could debug:</p>
     * <ol>
     * <li> I must add <b>HALF</b> of line length to the result as well. ¯\_(ツ)_/¯ </li>
     * <li> I must add +1 to have the correct answer. (This was incredibly hard with the above point together...)</li>
     * <li> I <b>MUST</b> calculate in longs, not to get overflow. </li>
     * </ol>
     * 
     * @param instructions -- list of {@link DigInstruction} to calculate with
     * @return the calculated area (with the line included) 
     */
    private static long calcArea(List<DigInstruction> instructions) {
        long line = 0L;
        long area = 0L;
        List<Coord> coords = new ArrayList<>();
        coords.add(new Coord(0, 0));
        for (var inst : instructions) {
            line += inst.length();
            coords.add(coords.getLast().move(inst.dir(), inst.length()));
        }
        for (int i = 0; i < coords.size(); ++i) {
            Coord ci = coords.get(i);
            Coord cn = coords.get((i + 1) % coords.size());
            area += (ci.y + (long) cn.y) * (ci.x - cn.x);
        }
        return (Math.abs(area) + line) / 2 + 1;
    }
    
    private static void explore(Coord coord, int minX, int maxX, int minY, int maxY, Set<Coord> inside,
            Set<Coord> outside, Set<Coord> line) {
        Queue<Coord> visit = new LinkedList<>();
        Set<Coord> seen = new HashSet<>();
        seen.add(coord);
        visit.add(coord);
        while(!visit.isEmpty()) {
            Coord at = visit.poll();
            at.neighbours().stream().filter(c->!seen.contains(c) && !line.contains(c)).forEach(c->{
                visit.add(c);
                seen.add(c);
            });
            if(outside.contains(at) || at.x<minX || at.y<minY || maxX<at.x||maxY<at.y) {
                outside.addAll(seen);
                return;
            } else if(inside.contains(at)) {
                inside.add(at);
                return;
            }
        }
        inside.addAll(seen);
    }

    private static List<SequencedSet<Coord>> findLines(List<SequencedSet<Coord>> lines, Coord coord) {
        return lines.stream().filter(s->s.contains(coord)).toList();
    }
    
    private static String readInput() {
        try {
            return Files.readString(Path.of("day18.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Coord(int x, int y) {
        List<Coord> neighbours() {
            return List.of(new Coord(x-1, y), new Coord(x+1, y), new Coord(x, y-1), new Coord(x, y+1));
        }
        Coord move(char dir, int length) {
            return switch(dir) {
            case 'L' -> new Coord(x-length, y);
            case 'R' -> new Coord(x+length, y);
            case 'U' -> new Coord(x, y-length);
            case 'D' -> new Coord(x, y+length);
            default -> throw new IllegalStateException("unknown dir: "+dir);
            };
        }
    }
    
    private record DigInstruction(char dir, int length, String color) {
        static DigInstruction fromString(String line) {
            var parts = line.split(" ");
            return new DigInstruction(parts[0].charAt(0), Integer.parseInt(parts[1]), parts[2]);
        }
        DigInstruction convertColor() {
            String num = color.substring(2, color.length()-2);
            char codedDir = color.charAt(color.length()-2);
            int codedLength = Integer.parseInt(num, 16);
            return switch(codedDir) {
            case '0' -> new DigInstruction('R', codedLength, "");
            case '1' -> new DigInstruction('D', codedLength, "");
            case '2' -> new DigInstruction('L', codedLength, "");
            case '3' -> new DigInstruction('U', codedLength, "");
            default -> throw new IllegalStateException("unknown coded dir: "+codedDir);
            };
        }
    }
    
    private static String example = """
R 6 (#70c710)
D 5 (#0dc571)
L 2 (#5713f0)
D 2 (#d2c081)
R 2 (#59c680)
D 2 (#411b91)
L 5 (#8ceee2)
U 2 (#caa173)
L 1 (#1b58a2)
U 2 (#caa171)
R 2 (#7807d2)
U 3 (#a77fa3)
L 2 (#015232)
U 2 (#7a21e3)""";
}
