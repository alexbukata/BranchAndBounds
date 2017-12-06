package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Node;

import java.util.*;

public class GreedyGraphColoringAlgorithm {
    private final List<Set<Node>> independentSets;
    private final Map<Integer, Integer> colors;
    private final List<Node> nodes;

    public GreedyGraphColoringAlgorithm(List<Node> nodes) {
        this.independentSets = new ArrayList<>();
        this.colors = new HashMap<>();
        this.nodes = nodes;
    }


    public Map<Integer, Integer> calculate() {
        int maxColor = 0;
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
            colors.put(node.getIndex(), k + 1);
            independentSets.get(k).add(node);
        }
        return colors;
    }

    public List<Set<Node>> getIndependentSets() {
        return independentSets;
    }

    public Map<Integer, Integer> getColors() {
        return colors;
    }
}
