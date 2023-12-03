package io.github.zebalu.aoc2023.days;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Day03 {

    public static void main(String[] args) {
        List<String> matrix = readInput().lines().toList();
        part1(matrix);
        part2(matrix);
    }

    private static void part1(List<String> matrix) {
        Set<Coord> startCoords = new HashSet<>();
        for (int y = 0; y < matrix.size(); ++y) {
            String line = matrix.get(y);
            for (int x = 0; x < line.length(); ++x) {
                Coord coord = new Coord(x, y);
                char chr = line.charAt(x);
                if (isSymbol(chr)) {
                    coord.adjecents().stream()
                            .filter(c -> c.isValid(matrix) && Character.isDigit(matrix.get(c.y()).charAt(c.x())))
                            .map(c -> findFirstChar(matrix, c)).forEach(startCoords::add);
                }
            }
        }
        int sum = startCoords.stream().mapToInt(c -> readNumberFrom(matrix, c)).sum();
        System.out.println(sum);
    }

    private static void part2(List<String> matrix) {
        int sum = 0;
        for (int y = 0; y < matrix.size(); ++y) {
            String line = matrix.get(y);
            for (int x = 0; x < line.length(); ++x) {
                Coord coord = new Coord(x, y);
                char chr = line.charAt(x);
                if (chr == '*') {
                    Set<Coord> candidates = coord.adjecents().stream()
                            .filter(c -> c.isValid(matrix) && Character.isDigit(matrix.get(c.y()).charAt(c.x())))
                            .map(c -> findFirstChar(matrix, c)).collect(Collectors.toSet());
                    if (candidates.size() == 2) {
                        sum += candidates.stream().mapToInt(c -> readNumberFrom(matrix, c)).reduce(1, (a, b) -> a * b);
                    }
                }
            }
        }
        System.out.println(sum);
    }

    private static Coord findFirstChar(List<String> matrix, Coord coord) {
        String line = matrix.get(coord.y());
        int x = coord.x();
        while (x > 0 && Character.isDigit(line.charAt(x - 1))) {
            --x;
        }
        return new Coord(x, coord.y());
    }

    private static int readNumberFrom(List<String> matrix, Coord startCoord) {
        String line = matrix.get(startCoord.y());
        int x = startCoord.x();
        while (x < line.length() && Character.isDigit(line.charAt(x))) {
            ++x;
        }
        return Integer.parseInt(line.substring(startCoord.x(), x));
    }

    private static boolean isSymbol(char c) {
        if (c == '.' || Character.isDigit(c)) {
            return false;
        }
        return true;
    }

    private static String readInput() {
        try {
            return Files.readString(new File("day03.txt").getAbsoluteFile().toPath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Coord(int x, int y) {
        List<Coord> adjecents() {
            return List.of(new Coord(x - 1, y - 1), new Coord(x, y - 1), new Coord(x + 1, y - 1), new Coord(x - 1, y),
                    new Coord(x + 1, y), new Coord(x - 1, y + 1), new Coord(x, y + 1), new Coord(x + 1, y + 1));
        }

        boolean isValid(List<String> matrix) {
            return 0 <= y && y < matrix.size() && 0 <= x && x < matrix.get(y).length();
        }
    }
}
