package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.Node;

import java.util.*;

public class GreedyChromaticNumberAlgorithm {
    public static int forGraph(Graph graph) {
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        Comparator<Node> degreeComparator = Comparator.comparingInt(o -> o.getNeighbours().size());
        nodes.sort(degreeComparator.reversed());
        Map<Integer, Integer> colors = new HashMap<>();
        for (Node node : nodes) {
            Integer color = 1;
            List<Node> neighbours = new ArrayList<>(node.getNeighbours());
            neighbours.sort(degreeComparator.reversed()); // bottleneck!
            ListIterator<Node> neighboursIterator = neighbours.listIterator();
            while (color.equals(colors.getOrDefault(neighboursIterator.next().getIndex(), 0)) && neighboursIterator.hasNext()) {
                color++;
            }
            colors.put(node.getIndex(), color);
        }
        return Collections.max(colors.values());
    }
}
