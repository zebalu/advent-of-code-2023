package io.github.zebalu.aoc2023.days;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

public class Day16 {
    public static void main(String[] args) {
        String input = readInput();
        List<String> tiles = input.lines().toList();
        
        System.out.println(countEnergized(tiles, new Beam(new Coord(0,0), Coord.RIGHT)));
        List<Beam> starts = collectStartBeams(tiles.size(), tiles.getFirst().length());
        System.out.println(starts.stream().parallel().mapToLong(start -> countEnergized(tiles, start)).max().orElseThrow());
    }

    private static List<Beam> collectStartBeams(int height, int width) {
        List<Beam> starts = new ArrayList<>();
        for(int y=0; y<height; ++y) {
            starts.add(new Beam(new Coord(0,y), Coord.RIGHT));
            starts.add(new Beam(new Coord(width-1,y), Coord.RIGHT));
        }
        for(int x=0; x<width; ++x) {
            starts.add(new Beam(new Coord(x,0), Coord.DOWN));
            starts.add(new Beam(new Coord(0, height-1), Coord.UP));
        }
        return starts;
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
                case '|' -> {
                    if(beam.direction.x() != 0) {
                        yield List.of(new Beam(beam.position(), Coord.UP).step(), new Beam(beam.position(), Coord.DOWN).step());
                    } else {
                        yield List.of(beam.step());
                    }
                }
                case '-' -> {
                    if(beam.direction.y() != 0) {
                        yield List.of(new Beam(beam.position(), Coord.LEFT).step(), new Beam(beam.position(), Coord.RIGHT).step());
                    } else {
                        yield List.of(beam.step());
                    }
                }
                case '/' -> {
                    if (beam.direction().x() != 0) {
                        if (beam.direction().x() < 0) {
                            yield List.of(new Beam(beam.position(), Coord.DOWN).step());
                        } else {
                            yield List.of(new Beam(beam.position(), Coord.UP).step());
                        }
                    } else {
                        if (beam.direction().y() < 0) {
                            yield List.of(new Beam(beam.position(), Coord.RIGHT).step());
                        } else {
                            yield List.of(new Beam(beam.position(), Coord.LEFT).step());
                        }
                    }
                }
                case '\\' -> {
                    if (beam.direction().x() != 0) {
                        if (beam.direction().x() < 0) {
                            yield List.of(new Beam(beam.position(), Coord.UP).step());
                        } else {
                            yield List.of(new Beam(beam.position(), Coord.DOWN).step());
                        }
                    } else {
                        if (beam.direction().y() < 0) {
                            yield List.of(new Beam(beam.position(), Coord.LEFT).step());
                        } else {
                            yield List.of(new Beam(beam.position(), Coord.RIGHT).step());
                        }
                    }
                }
                default -> throw new IllegalArgumentException("Unexpected value: " + tile);
                };
            }).flatMap(List::stream).filter(isValid).toList();
        }
        return (visited.stream().map(Beam::position).distinct().count());
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day16.txt").toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    private record Coord(int x, int y) {
        private static Coord LEFT = new Coord(-1,0);
        private static Coord RIGHT = new Coord(1,0);
        private static Coord UP = new Coord(0,-1);
        private static Coord DOWN = new Coord(0,1);
        Coord add(Coord other) {
            return new Coord(x+other.x, y+other.y);
        }
    }
    private record Beam(Coord position, Coord direction) {
        Beam step() {
            return new Beam(position.add(direction), direction);
        }
    }
    
}
