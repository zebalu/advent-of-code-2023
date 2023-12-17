package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public class Day17 {
    public static void main(String[] args) {
        List<String> maze = readInput().lines().toList();
        System.out.println(part1(maze));
        System.out.println(part2(maze));
    }

    private static int part1(List<String> maze) {
        return minHeatLoss(maze, 3, (a, b) -> true);
    }

    private static int part2(List<String> maze) {
        return minHeatLoss(maze, 10, (c, s) -> 4 <= s.straightLength() || s.pos().directionOf(c) == s.dir());
    }

    private static int minHeatLoss(List<String> maze, int maxLength, BiPredicate<Coord, State> nextFilter) {
        int height = maze.size();
        int width = maze.getFirst().length();
        Predicate<Coord> isValidCood = c -> 0 <= c.x() && 0 <= c.y() && c.x < width && c.y < height;
        Predicate<State> isValidState = s -> isValidCood.test(s.pos()) && s.straightLength() <= maxLength;
        Coord target = new Coord(width - 1, height - 1);
        Map<Long, Integer> bestCost = new HashMap<>();
        Queue<State> queue = new PriorityQueue<>();
        queue.add(new State(new Coord(0, 0), 0, 0, Direction.EAST));
        while (!queue.isEmpty()) {
            State curr = queue.poll();
            List<Coord> nexts = curr.pos().nexts(curr.dir());
            List<State> nextStates = nexts
                    .stream().filter(isValidCood).filter(c -> nextFilter.test(c, curr)).map(c -> new State(c,
                            curr.nextStraight(c), curr.heatLoss() + heatCost(c, maze), curr.pos().directionOf(c)))
                    .filter(isValidState).toList();
            for (State s : nextStates) {
                if (s.pos().equals(target)) {
                    return s.heatLoss;
                }
                var costKey = s.toKey();
                if (!bestCost.containsKey(costKey) || s.heatLoss < bestCost.get(costKey)) {
                    bestCost.put(costKey, s.heatLoss);
                    queue.add(s);
                }
            }
        }
        throw new IllegalStateException("Can not solve maze");
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day17.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static int heatCost(Coord coord, List<String> maze) {
        return Integer.parseInt("" + maze.get(coord.y()).charAt(coord.x()));
    }

    private enum Direction {
        NORTH, EAST, SOUTH, WEST;
    }

    private record Coord(int x, int y) {
        List<Coord> nexts(Direction dir) {
            return switch (dir) {
            case EAST -> List.of(new Coord(x + 1, y), new Coord(x, y - 1), new Coord(x, y + 1));
            case WEST -> List.of(new Coord(x - 1, y), new Coord(x, y - 1), new Coord(x, y + 1));
            case NORTH -> List.of(new Coord(x, y - 1), new Coord(x + 1, y), new Coord(x - 1, y));
            case SOUTH -> List.of(new Coord(x, y + 1), new Coord(x + 1, y), new Coord(x - 1, y));
            };
        }

        Direction directionOf(Coord coord) {
            if (y == coord.y) {
                if (x < coord.x) {
                    return Direction.EAST;
                } else if (x > coord.x) {
                    return Direction.WEST;
                }
            } else if (x == coord.x) {
                if (y < coord.y) {
                    return Direction.SOUTH;
                } else if (y > coord.y) {
                    return Direction.NORTH;
                }
            }
            throw new IllegalArgumentException(this + " is equal with " + coord + " ?");
        }
    }

    private record State(Coord pos, int straightLength, int heatLoss, Direction dir) implements Comparable<State> {

        private static final Comparator<State> COMPARATOR = Comparator.comparingLong(State::heatLoss).thenComparingInt(State::straightLength);

        @Override
        public int compareTo(State o) {
            return COMPARATOR.compare(this, o);
        }

        int nextStraight(Coord c) {
            if (pos.directionOf(c) == dir) {
                return straightLength + 1;
            }
            return 1;
        }
        
        long toKey() {
            return pos.x()*1_000_000L+pos.y()*1_000L+straightLength*10L+dir.ordinal();
        }

    }
}
