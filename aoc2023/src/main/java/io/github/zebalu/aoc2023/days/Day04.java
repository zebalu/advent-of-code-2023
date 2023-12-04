package io.github.zebalu.aoc2023.days;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Day04 {
    public static void main(String[] args) {
        List<Card> cards = readInput().lines().map(Card::fromString).toList();
        System.out.println(part1(cards));
        System.out.println(part2(cards));
    }
    
    private static int part1(List<Card> cards) {
        return cards.stream().mapToInt(Card::points).sum();
    }
    
    private static int part2(List<Card> cards) {
        Map<Card, Integer> cardMultipliers = new HashMap<>(cards.stream().collect(Collectors.toMap(c -> c, c -> 1)));
        for (int i = 0; i < cards.size(); ++i) {
            Card card = cards.get(i);
            int m = card.countMultiplier();
            int inc = cardMultipliers.get(card);
            for (int j = i + 1; j <= i + m; ++j) {
                cardMultipliers.compute(cards.get(j), (k, v) ->  v + inc);
            }
        }
        return cardMultipliers.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static String readInput() {
        try {
            return Files.readString(new File("day04.txt").getAbsoluteFile().toPath());
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private record Card(int id, Set<Integer> winingNumbers, Set<Integer> foundNumbers) {
        int points() {
            int times = countMultiplier();
            if (times > 0) {
                return (int) Math.pow(2, times - 1);
            }
            return 0;
        }

        int countMultiplier() {
            return (int) winingNumbers.stream().filter(w -> foundNumbers.contains(w)).count();
        }

        static Card fromString(String printedCard) {
            String line = printedCard.replaceAll(" +", " ");
            String[] idParts = line.split(": ");
            int id = Integer.parseInt(idParts[0].split(" ")[1]);
            String[] numParts = idParts[1].split(" \\| ");
            Set<Integer> wns = Arrays.stream(numParts[0].split(" ")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toSet());
            Set<Integer> fns = Arrays.stream(numParts[1].split(" ")).mapToInt(Integer::parseInt).boxed().collect(Collectors.toSet());
            return new Card(id, wns, fns);
        }
    }

}
