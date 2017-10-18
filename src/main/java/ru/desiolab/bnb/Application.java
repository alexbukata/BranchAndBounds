package ru.desiolab.bnb;

import ru.desiolab.bnb.algorithms.BranchAndBoundsMaxClique;
import ru.desiolab.bnb.algorithms.GreedyGraphColoringAlgorithm;
import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.GraphParser;
import ru.desiolab.bnb.graph.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Application {
    public static void main(String[] args) {
        String pathToGraph = args[0];
        Graph graph;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathToGraph))) {
            graph = GraphParser.fromStream(reader.lines());
        } catch (IOException e) {
            throw new IllegalStateException("Error", e);
        }
        long start = System.currentTimeMillis();
        Map<Integer, Integer> colors = GreedyGraphColoringAlgorithm.forNodes(graph.getNodes());
        System.out.println(colors);
        BranchAndBoundsMaxClique algorithm = new BranchAndBoundsMaxClique(graph);
        List<Node> clique = algorithm.findClique(colors);
        for (Node node : clique) {
            System.out.print(node.getIndex() + ", ");
        }
        System.out.println();
        System.out.println(clique.size());
        System.out.println("Time: " + (System.currentTimeMillis() - start));
    }
}
