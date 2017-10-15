package ru.desiolab.bnb.graph;

import java.util.stream.Stream;

public class GraphParser {

    public static Graph fromStream(Stream<String> input) {
        Graph graph = new Graph();
        input.forEach(edge -> {
            String[] values = edge.split(" ");
            if (values[0].equals("e")) {
                int firstNodeIndex = Integer.parseInt(values[1]);
                int secondNodeIndex = Integer.parseInt(values[2]);
                graph.createEdge(firstNodeIndex, secondNodeIndex);
            }
        });
        return graph;
    }
}
