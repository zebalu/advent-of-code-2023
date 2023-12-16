package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;

import static java.util.function.Predicate.not;

public class Day16 {
    public static void main(String[] args) {
        String input = readInput();
        List<String> tiles = input.lines().toList();
        
        System.out.println(countEnergized(tiles, new Beam(new Coord(0,0), Direction.RIGHT)));
        
        List<Beam> starts = collectStartBeams(tiles.size(), tiles.getFirst().length());
        System.out.println(starts.stream().parallel().mapToLong(start -> countEnergized(tiles, start)).max().orElseThrow());
    }

    private static List<Beam> collectStartBeams(int height, int width) {
        return Stream
                .concat(IntStream.range(0, height).mapToObj(y -> List.of(
                                new Beam(new Coord(0, y), Direction.RIGHT),
                                new Beam(new Coord(width - 1, y), Direction.LEFT))),
                        IntStream.range(0, width).mapToObj(x -> List.of(
                                new Beam(new Coord(x, 0), Direction.DOWN),
                                new Beam(new Coord(x, height - 1), Direction.UP))))
                .flatMap(List::stream).toList();
    }
    
    private static long countEnergized(List<String> tiles, Beam start) {
        int height = tiles.size();
        int width = tiles.getFirst().length();
        Predicate<Beam> isValid = beam -> 0<= beam.position().x && beam.position().x < width && 0<= beam.position().y() && beam.position().y() < height; 
        List<Beam> beams = new ArrayList<>();
        beams.add(start);
        Set<Beam> visited = new HashSet<>();
        while(beams.stream().anyMatch(not(visited::contains))) {
            beams = beams.stream().filter(not(visited::contains)).peek(visited::add).map(beam->{
                char tile = tiles.get(beam.position().y()).charAt(beam.position.x());
                return switch (tile) {
                case '.' -> List.of(beam.step());
                case '|' -> verticalSplit(beam);
                case '-' -> horizontalSplit(beam);
                case '/' -> rightTurn(beam);
                case '\\' -> leftTurn(beam);
                default -> throw new IllegalStateException("Unexpected value: " + tile);
                };
            }).flatMap(List::stream).filter(isValid).toList();
        }
        return visited.stream().map(Beam::position).distinct().count();
    }
    
    private static List<Beam> verticalSplit(Beam beam) {
        return switch (beam.direction) {
        case Direction.LEFT, Direction.RIGHT -> List.of(new Beam(beam.position(), Direction.UP).step(), new Beam(beam.position(), Direction.DOWN).step());
        case Direction.UP, Direction.DOWN -> List.of(beam.step());
        };
    }
    
    private static List<Beam> horizontalSplit(Beam beam) {
        return switch (beam.direction) {
        case Direction.UP, Direction.DOWN -> List.of(new Beam(beam.position(), Direction.LEFT).step(), new Beam(beam.position(), Direction.RIGHT).step());
        case Direction.LEFT, Direction.RIGHT -> List.of(beam.step());
        };
    }
    
    private static List<Beam> rightTurn(Beam beam) {
        return switch (beam.direction) {
        case Direction.LEFT -> List.of(new Beam(beam.position(), Direction.DOWN).step());
        case Direction.RIGHT -> List.of(new Beam(beam.position(), Direction.UP).step());
        case Direction.UP -> List.of(new Beam(beam.position(), Direction.RIGHT).step());
        case Direction.DOWN -> List.of(new Beam(beam.position(), Direction.LEFT).step());
        };
    }

    private static List<Beam> leftTurn(Beam beam) {
        return switch (beam.direction) {
        case Direction.LEFT -> List.of(new Beam(beam.position(), Direction.UP).step());
        case Direction.RIGHT -> List.of(new Beam(beam.position(), Direction.DOWN).step());
        case Direction.UP -> List.of(new Beam(beam.position(), Direction.LEFT).step());
        case Direction.DOWN -> List.of(new Beam(beam.position(), Direction.RIGHT).step());
        };
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day16.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    private record Coord(int x, int y) {
        Coord add(Direction direction) {
            return new Coord(x+direction.step.x, y+direction.step.y);
        }
    }
    private static enum Direction {
        LEFT(new Coord(-1,0)), RIGHT(new Coord(1,0)), UP(new Coord(0,-1)), DOWN(new Coord(0,1));
        private Coord step;
        private Direction(Coord step) {
            this.step=step;
        }
    }
    private record Beam(Coord position, Direction direction) {
        Beam step() {
            return new Beam(position.add(direction), direction);
        }
    }
    
}
