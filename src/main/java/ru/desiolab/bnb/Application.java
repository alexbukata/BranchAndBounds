package ru.desiolab.bnb;

import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.GraphParser;
import ru.desiolab.bnb.graph.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Application {
    public static void main(String[] args) {
        String pathToGraph = args[0];
        Graph graph;
        try (BufferedReader reader = new BufferedReader(new FileReader(pathToGraph))) {
            graph = GraphParser.fromStream(reader.lines());
        } catch (IOException e) {
            throw new IllegalStateException("Error", e);
        }
        for (Node node : graph.getNeighbours()) {
            System.out.println(node.getIndex());
        }
    }
}
