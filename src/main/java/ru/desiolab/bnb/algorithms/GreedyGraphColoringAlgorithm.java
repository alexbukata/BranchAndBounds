package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Node;

import java.util.*;

public class GreedyGraphColoringAlgorithm {
    public static Map<Integer, Integer> forNodes(List<Node> nodes) {
        int maxColor = 0;
        Map<Integer, Integer> colors = new HashMap<>();
        List<Set<Node>> independentSets = new ArrayList<>();
        independentSets.add(new HashSet<>());
        for (Node node : nodes) {
            int k = 0;
            while (true) {
                Set<Node> k_independentSet = k < independentSets.size() ? independentSets.get(k) : new HashSet<>();
                Set<Node> nodesCopy = new HashSet<>(k_independentSet);
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
