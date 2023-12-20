package io.github.zebalu.aoc2023.days;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
            machine.reset();
            result = lcm(result, counter);
        }
        broadcaster.outputs = originalOutputs;
        return result;
    }

    private static long lcm(long a, long b) {
        return a / gcf(a, b) * b;
    }

    private static long gcf(long a, long b) {
        if (b == 0) {
            return a;
        } else {
            return (gcf(b, a % b));
        }
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
            if (parts[0].equals("broadcaster")) {
                result.type = '?';
                result.name = parts[0];
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
            List<Message> notified = new ArrayList<>();
            switch (type) {
            case '%' -> {
                if (pulse == Pulse.LOW) {
                    onOff = !onOff;
                    outputs.stream().map(s -> new Message(name, s, onOff ? Pulse.HIGH : Pulse.LOW))
                            .forEach(notified::add);
                }
            }
            case '&' -> {
                inputs.put(from, pulse);
                boolean allHigh = inputs.values().stream().allMatch(p -> p == Pulse.HIGH);
                outputs.stream().map(s -> new Message(name, s, allHigh ? Pulse.LOW : Pulse.HIGH))
                        .forEach(notified::add);
            }
            case '?' -> {
                outputs.stream().map(s -> new Message(name, s, pulse)).forEach(notified::add);
            }
            }
            return notified;
        }
        
        void reset() {
            onOff = false;
            inputs.keySet().forEach(k->inputs.put(k, Pulse.LOW));
        }
    }

    private record Machine(Map<String, Node> setup, Counter counter) {
        boolean push() {
            Queue<Message> messageQueue = new LinkedList<>();
            counter.lowCounter.incrementAndGet();
            messageQueue.add(new Message("button", Node.BROADCASTER, Pulse.LOW));
            while (!messageQueue.isEmpty()) {
                Message curr = messageQueue.poll();
                if (setup.containsKey(curr.target())) {
                    var nexts = setup.get(curr.target()).recieve(curr.from(), curr.pulse());
                    nexts.forEach(msg -> {
                        if (msg.pulse() == Pulse.LOW) {
                            counter.lowCounter.incrementAndGet();
                        } else {
                            counter.highCounter.incrementAndGet();
                        }
                        messageQueue.add(msg);
                    });
                }
                if (curr.target.equals("rx") && curr.pulse == Pulse.LOW) {
                    return true;
                }
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
