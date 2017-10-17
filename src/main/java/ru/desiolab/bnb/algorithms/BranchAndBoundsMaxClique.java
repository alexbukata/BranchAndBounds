package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.Node;

import java.util.*;

public class BranchAndBoundsMaxClique {

    private final Graph graph;
    private final Map<Integer, Integer> colors;
    private final Integer chromaticColor;
    private List<Node> cliqueMax;

    public BranchAndBoundsMaxClique(Graph graph, Map<Integer, Integer> colors) {
        this.graph = graph;
        this.colors = colors;
        this.chromaticColor = Collections.max(colors.values());
    }

    public List<Node> findClique() {
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        nodes.sort(Comparator.<Node>comparingInt(value -> colors.get(value.getIndex())).reversed());
        cliqueMax = new ArrayList<>();
        branchAndBounds(nodes, new ArrayList<>());
        return cliqueMax;
    }

    private void branchAndBounds(List<Node> candidates, List<Node> clique) {
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
                branchAndBounds(currentCandidates, currentClique);
            } else if (cliqueMax.size() < currentClique.size()) {
                cliqueMax = currentClique;
            }
        }
    }
}
