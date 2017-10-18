package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BranchAndBoundsMaxClique {

    private final Graph graph;
    private List<Node> cliqueMax;

    public BranchAndBoundsMaxClique(Graph graph) {
        this.graph = graph;
    }

    public List<Node> findClique(Map<Integer, Integer> colors) {
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        nodes.sort(Comparator.<Node>comparingInt(value -> colors.get(value.getIndex())).reversed());
        cliqueMax = new ArrayList<>();
        branchAndBounds(nodes, new ArrayList<>(), colors);
        return cliqueMax;
    }

    private void branchAndBounds(List<Node> candidates, List<Node> clique, final Map<Integer, Integer> colors) {
        for (Node candidate : candidates) {
            if (clique.size() + colors.get(candidate.getIndex()) <= cliqueMax.size()) {
                return;
            }
            List<Node> currentClique = new ArrayList<>(clique);
            currentClique.add(candidate);

            ArrayList<Node> currentCandidates = new ArrayList<>(candidates);
            currentCandidates.retainAll(candidate.getNeighbours());
            currentCandidates.sort(Comparator.<Node>comparingInt(value -> colors.get(value.getIndex())).reversed());

            if (currentCandidates.size() > 0) {
                Map<Integer, Integer> newColors = GreedyGraphColoringAlgorithm.forNodes(currentCandidates);
                branchAndBounds(currentCandidates, currentClique, newColors);
            } else if (cliqueMax.size() < currentClique.size()) {
                cliqueMax = currentClique;
            }
        }
    }
}
