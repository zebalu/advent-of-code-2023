package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Day20 {
    public static void main(String[] args) {
        Machine machine = Machine.build(readInput().lines().map(Node::parse).toList());
        System.out.println(part1(machine));
        machine.reset();
        System.out.println(part2(machine));
    }

    private static long part1(Machine machine) {
        for (int i = 0; i < 1000; ++i) {
            machine.push();
        }
        return machine.count();
    }

    private static long part2(Machine machine) {
        var broadcaster = machine.setup.get(Node.BROADCASTER);
        var originalOutputs = broadcaster.outputs;
        var toRxSink = machine.setup.entrySet().stream().filter(e -> e.getValue().outputs.contains("rx")).findAny()
                .orElseThrow().getValue();
        long result = 1L;
        for (String s : originalOutputs) {
            toRxSink.inputs.keySet().forEach(k -> toRxSink.inputs.put(k, Pulse.HIGH));
            broadcaster.outputs = List.of(s);
            long counter = 1L;
            while (!machine.push()) {
                ++counter;
            }
            broadcaster.outputs = originalOutputs;
            machine.reset();
            result = lcm(result, counter);
        }
        return result;
    }

    private static long lcm(long a, long b) {
        return a / gcd(a, b) * b;
    }

    private static long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day20.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private enum Pulse {
        LOW, HIGH
    }

    private record Counter(AtomicLong lowCounter, AtomicLong highCounter) {
        void count(Pulse pulse) {
            switch(pulse) {
            case LOW -> lowCounter.incrementAndGet();
            case HIGH -> highCounter.incrementAndGet();
            }
        }
    }

    private static class Node {
        static final String BROADCASTER = "broadcaster";
        char type;
        String name;
        List<String> outputs = new ArrayList<String>();
        Map<String, Pulse> inputs = new HashMap<>();
        boolean onOff = false;

        static Node parse(String line) {
            Node result = new Node();
            var parts = line.split(" -> ");
            if (parts[0].equals(BROADCASTER)) {
                result.type = '?';
                result.name = BROADCASTER;
            } else {
                result.type = parts[0].charAt(0);
                result.name = parts[0].substring(1);
            }
            for (String dest : parts[1].split(", ")) {
                result.outputs.add(dest);
            }
            return result;
        }

        void markInput(String input) {
            if(type == '&')  {
                inputs.put(input, Pulse.LOW);
            }
        }

        List<Message> recieve(String from, Pulse pulse) {
            return switch (type) {
            case '%' -> {
                yield switch(pulse) {
                case LOW -> { onOff = !onOff; yield forward(onOff ? Pulse.HIGH : Pulse.LOW); }
                case HIGH -> List.of();
                };
            }
            case '&' -> {
                inputs.put(from, pulse);
                boolean allHigh = inputs.values().stream().allMatch(p -> p == Pulse.HIGH);
                yield forward(allHigh ? Pulse.LOW : Pulse.HIGH);
            }
            case '?' -> forward(pulse);
            default -> throw new IllegalStateException("unkown node type: " + type);
            };
        }
        
        private List<Message> forward(Pulse pulse) {
            return outputs.stream().map(s -> new Message(name, s, pulse)).toList();
        }
        
        void reset() {
            onOff = false;
            inputs.keySet().forEach(k->inputs.put(k, Pulse.LOW));
        }
    }

    private record Machine(Map<String, Node> setup, Counter counter) {
        private static final String STOP_NODE = "rx";
        boolean push() {
            Queue<Message> messageQueue = new LinkedList<>();
            counter.lowCounter.incrementAndGet();
            messageQueue.add(new Message("button", Node.BROADCASTER, Pulse.LOW));
            while (!messageQueue.isEmpty()) {
                Message curr = messageQueue.poll();
                if (curr.target.equals(STOP_NODE) && curr.pulse == Pulse.LOW) {
                    return true;
                }
                setup.computeIfPresent(curr.target, (key, node)->{
                    node.recieve(curr.from(), curr.pulse()).forEach(msg -> {
                        counter.count(msg.pulse());
                        messageQueue.add(msg);
                    });
                    return node;
                });
            }
            return false;
        }

        long count() {
            return counter.lowCounter.get() * counter.highCounter.get();
        }
        
        void reset() {
            counter.lowCounter().set(0);
            counter.highCounter().set(0);
            setup.values().forEach(Node::reset);
        }

        static Machine build(List<Node> nodes) {
            Map<String, Node> mapped = nodes.stream().collect(Collectors.toMap(node -> node.name, node -> node));
            mapped.values().stream().forEach(node -> {
                node.outputs.stream().filter(s -> mapped.containsKey(s))
                        .forEach(o -> mapped.get(o).markInput(node.name));
            });
            return new Machine(mapped, new Counter(new AtomicLong(0L), new AtomicLong(0L)));
        }
    }

    private record Message(String from, String target, Pulse pulse) {
    }

}
