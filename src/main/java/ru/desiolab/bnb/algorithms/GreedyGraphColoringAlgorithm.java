package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.Node;

import java.util.*;

public class GreedyGraphColoringAlgorithm {
    public static Map<Integer, Integer> forGraph(Graph graph) {
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        Comparator<Node> degreeComparator = Comparator.comparingInt(o -> o.getNeighbours().size());
        nodes.sort(degreeComparator.reversed());
        Map<Integer, Integer> colors = new HashMap<>();
        for (Node node : nodes) {
            List<Node> neighbours = new ArrayList<>(node.getNeighbours());
            neighbours.sort(degreeComparator.reversed()); // bottleneck!
            Set<Integer> neighboursColors = new TreeSet<>();
            for (Node neighbour : neighbours) {
                neighboursColors.add(colors.getOrDefault(neighbour.getIndex(), 0));
            }
            Integer color;
            for (color = 1; color <= neighbours.size(); color++) {
                if (!neighboursColors.contains(color)) {
                    break;
                }
            }
            colors.put(node.getIndex(), color);
        }
        return colors;
    }

    public static Map<Integer, Integer> smartForGraph(Graph graph) {
        int maxColor = 0;
        Map<Integer, Integer> colors = new HashMap<>();
        List<Set<Node>> independentSets = new ArrayList<>();
        independentSets.add(new HashSet<>());
        for (Node node : graph.getNodes()) {
            int k = 0;
            while (true) {
                Set<Node> nodes = k < independentSets.size() ? independentSets.get(k) : new HashSet<>();
                Set<Node> nodesCopy = new HashSet<>(nodes);
                nodesCopy.retainAll(node.getNeighbours());
                if (nodesCopy.size() == 0) {
                    break;
                }
                k++;
            }
            if (k > maxColor) {
                maxColor = k;
                independentSets.add(k, new HashSet<>());
            }
            colors.put(node.getIndex(), k);
            independentSets.get(k).add(node);
        }
        return colors;
    }
}
