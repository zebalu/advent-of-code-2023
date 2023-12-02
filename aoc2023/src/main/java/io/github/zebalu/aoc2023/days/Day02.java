package io.github.zebalu.aoc2023.days;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

public class Day02 {

    public static void main(String[] args) {
        List<Game> games = readLines().stream().map(Game::fromString).toList();
        int sumIds = games.stream().filter(g -> g.maxRed() <= 12 && g.maxGreen() <= 13 && g.maxBlue() <= 14)
                .mapToInt(Game::id).sum();
        System.out.println(sumIds);
        int sumPows = games.stream().mapToInt(g -> g.maxRed() * g.maxBlue() * g.maxGreen()).sum();
        System.out.println(sumPows);
    }

    private static List<String> readLines() {
        try (BufferedReader br = new BufferedReader(
                new FileReader(new File("day02.txt").getAbsoluteFile(), StandardCharsets.UTF_8))) {
            return br.lines().toList();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private record Round(int red, int green, int blue) {
        static Round fromString(String str) {
            int r = 0;
            int g = 0;
            int b = 0;
            for (String s : str.split(", ")) {
                String[] ps = s.split(" ");
                int v = Integer.parseInt(ps[0]);
                String color = ps[1];
                switch (color) {
                case "red" -> r = v;
                case "green" -> g = v;
                case "blue" -> b = v;
                default -> throw new IllegalStateException(s);
                }
            }
            return new Round(r, g, b);
        }
    }

    private record Game(int id, List<Round> rounds) {
        int maxRed() {
            return maxOfRounds(Round::red);
        }

        int maxGreen() {
            return maxOfRounds(Round::green);
        }

        int maxBlue() {
            return maxOfRounds(Round::blue);
        }

        private int maxOfRounds(ToIntFunction<Round> extractor) {
            return rounds.stream().mapToInt(extractor).max().orElseThrow();
        }

        static Game fromString(String line) {
            String[] idPs = line.split(": ");
            int id = Integer.parseInt(idPs[0].split(" ")[1]);
            List<Round> rs = Arrays.stream(idPs[1].split("; ")).map(s -> Round.fromString(s)).toList();
            return new Game(id, rs);
        }
    }
}
