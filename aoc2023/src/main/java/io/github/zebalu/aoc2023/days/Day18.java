package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;

public class Day18 {
    public static void main(String[] args) {
        String input = readInput(); //example;//readInput(); //example;//readInput();
        var instructs = input.lines().map(DigInstruction::fromString).toList();
        System.out.println(calcArea(instructs));
        var decoded = instructs.stream().map(DigInstruction::convertColor).toList();
        System.out.println(calcArea(decoded));
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
     * <p>The formula states for n points: area = 1/2*SUM{i: 1->n}((y_i + y_[i+1])*(x_i - x_[i+1]))</p>
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
        // the start coord should be included as well
        coords.add(new Coord(0, 0));
        for (var inst : instructions) {
            line += inst.length();
            coords.add(coords.getLast().move(inst.dir(), inst.length()));
        }
        for (int i = 0; i < coords.size(); ++i) {
            Coord ci = coords.get(i);
            // in case it is the last coord, the next coord is the first (start) coord.
            Coord cn = coords.get((i + 1) % coords.size());
            // one of the arguments must be long, so I won't have overflow
            area += ((long)ci.y + cn.y) * (ci.x - cn.x);
        }
        return (Math.abs(area) + line) / 2 + 1;
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day18.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private record Coord(int x, int y) {
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
}
