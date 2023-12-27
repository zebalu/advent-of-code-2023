package io.github.zebalu.aoc2023.days;

import java.nio.file.*;
import java.util.*;

public class Day25 {
    public static void main(String[] args) {
        String input = readInput();
        Map<String, Set<String>> graph = new HashMap<>();
        input.lines().forEach(l->processLine(l, graph));

        var partitioning = partitionGraphWithMaxCut(graph, 3);
        
        System.out.println(partitioning.part1.size()*partitioning.part2.size());
        System.out.println("Marry Christmas!");
    }

    private static String readInput() {
        try {
            return Files.readString(Path.of("day25.txt").toAbsolutePath());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static void processLine(String line, Map<String, Set<String>> graph) {
        var parts = line.split(": ");
        var name = parts[0];
        var cons = parts[1].split(" ");
        for(var con: cons) {
            markConnectionn(graph, name, con);
        }
    }
    
    private static void markConnectionn(Map<String, Set<String>> graph, String from, String to) {
        graph.computeIfAbsent(from, (k)->new HashSet<>()).add(to);
        graph.computeIfAbsent(to, (k)->new HashSet<>()).add(from);
    }
    
    private static Set<Set<String>> findShortestPath(Map<String, Set<String>> graph, String from, String to, Set<Set<String>> forbiddenEdges) {
        record PathStep(String at, List<String> history) {}
        Set<String> visited = new HashSet<>();
        Queue<PathStep> queue = new LinkedList<>();
        visited.add(from);
        queue.add(new PathStep(from, List.of(from)));
        while (!queue.isEmpty()) {
            var current = queue.poll();
            for (String next : graph.get(current.at).stream()
                    .filter(n -> !visited.contains(n) && !forbiddenEdges.contains(Set.of(current.at, n))).toList()) {
                List<String> history = new ArrayList<>(current.history);
                history.add(next);
                queue.add(new PathStep(next, history));
                visited.add(next);
                if (next.equals(to)) {
                    return constructPath(history);
                }
            }
        }
        return new HashSet<>();
     }
    
    private static SequencedSet<Set<String>> constructPath(List<String> pathElements) {
        SequencedSet<Set<String>> path = new LinkedHashSet<Set<String>>();
        for(int i=0; i<pathElements.size()-1; ++i) {
            path.add(Set.of(pathElements.get(i), pathElements.get(i+1)));
        }
        return path;
    }
    
    private record GraphPartition(Map<String, Set<String>> part1, Map<String, Set<String>> part2, Set<Set<String>> cuttedEdges) {
        
    }
    
    /**
     * Partitions the graph into 2 separate partitions bay cutting the shortest path between 2 vertices maxCut times.
     * This can only work if the partitioning requires less cut than the minimum connectivity of the vertices.
     * @param graph the graph to cut.
     * @param maxCut the times we remove shortest pathes from the graph
     * @return the partitioning with the 2 parts and the bridges
     */
    private static GraphPartition partitionGraphWithMaxCut(Map<String, Set<String>> graph, int maxCut) {
        int minimumConnectivity = graph.values().stream().mapToInt(s->s.size()).min().orElseThrow();
        if(minimumConnectivity <= maxCut) {
            throw new IllegalArgumentException("This algorithm won't work, as the graph has too low connectivity number");
        }
        List<String> vertices = new ArrayList<String>(graph.keySet());
        for(int i=0; i<vertices.size()-1; ++i) {
            String from = vertices.get(i);
            for(int j=i+1; j<vertices.size(); ++j) {
                String to = vertices.get(j);
                Set<Set<String>> forbiddenEdges = new HashSet<>();
                for(int k=0; k<maxCut; ++k) {
                    Set<Set<String>> path = findShortestPath(graph, from, to, forbiddenEdges);
                    if(path.isEmpty()) {
                        throw new IllegalStateException("Too early not to find a path from: "+from+" to: "+to+" in the "+k+". step");
                    }
                    forbiddenEdges.addAll(path);
                }
                Set<Set<String>> path = findShortestPath(graph, from, to, forbiddenEdges);
                if(path.isEmpty()) {
                    // the edges to cut are in the forbidden set 
                    return createPartitioning(graph, from, to, forbiddenEdges);
                } else {
                    // from and to are in the same subgraph (or at least they have more then maxCut shortest pathes between them)
                }
            }
        }
        throw new IllegalStateException("Could not partition the graph in "+maxCut+" steps");
    }

    /**
     * Creates partitions of the two vertices, where the bridge vertices are hidden between the candidate edges.
     * @param graph the graph to partition
     * @param from a vertex in one of the partitions
     * @param to a vertex in the other partition
     * @param candidateEdges some of these edges are the bridges
     * @return the partitioning with the 2 parts and the bridge edges
     */
    private static GraphPartition createPartitioning(Map<String, Set<String>> graph, String from, String to, Set<Set<String>> candidateEdges) {
        Set<Set<String>> bridges = new HashSet<>();
       
        for(var edge: candidateEdges) {
            // forbid the use of all candidate edges, except the current one
            var forbiddenEdges = new HashSet<>(candidateEdges);
            forbiddenEdges.remove(edge);
            var path = findShortestPath(graph, from, to, forbiddenEdges);
            if(!path.isEmpty()) {
                // this is a bridge between the 2 parts
                bridges.add(edge);
            }
        }
        
        Map<String, Set<String>> part1 = new HashMap<>();
        Map<String, Set<String>> part2 = new HashMap<>();
        
        Set<String> part1Vertices = findConnectedVertices(from, graph, bridges);
        
        for(String id: graph.keySet()) {
            Set<String> connections = graph.get(id);
            for(String v2: connections) {
                if(!bridges.contains(Set.of(id, v2))) {
                    if(part1Vertices.contains(id)) {
                        markConnectionn(part1, id, v2);
                    } else {
                        markConnectionn(part2, id, v2);
                    }
                }
            }
        }
        return new GraphPartition(part1, part2, bridges);
    }
    
    /**
     * Finds all reachable vertices, but does not use the forbidden edges.
     * @param start the start vertex to run from
     * @param graph the graph to work with
     * @param forbiddenEdges edges which can not be used
     * @return The partitioning with the 2 separate parts and the bridges
     */
    private static Set<String> findConnectedVertices(String start, Map<String, Set<String>> graph, Set<Set<String>> forbiddenEdges) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            String curr = queue.poll();
            graph.get(curr).stream().filter(n->!visited.contains(n) && ! forbiddenEdges.contains(Set.of(curr, n))).forEach(n->{
                queue.add(n);
                visited.add(n);
            });
        }
        return visited;
    }
}
