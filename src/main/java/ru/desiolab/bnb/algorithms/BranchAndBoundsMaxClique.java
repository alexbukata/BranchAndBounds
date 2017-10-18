package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BranchAndBoundsMaxClique {

    private final Graph graph;
    private List<Node> cliqueMax;
    private int skips = 0;

    public BranchAndBoundsMaxClique(Graph graph) {
        this.graph = graph;
    }

    public List<Node> findClique(Map<Integer, Integer> colors) {
        List<Node> nodes = graph.getNodes();
        nodes.sort(Comparator.<Node>comparingInt(value -> colors.get(value.getIndex())).reversed());
        System.out.println(nodes.stream().map(Node::getIndex).collect(Collectors.toList()));

        cliqueMax = new ArrayList<>();
        branchAndBounds(nodes, new ArrayList<>(), colors);
        System.out.println("Skips: " + skips);
        return cliqueMax;
    }

    private void branchAndBounds(List<Node> candidates, List<Node> clique, final Map<Integer, Integer> colors) {
//        System.out.println(clique.stream().map(Node::getIndex).collect(Collectors.toList()));
        for (Node candidate : candidates) {
            if (clique.size() + colors.get(candidate.getIndex()) <= cliqueMax.size()) {
                skips++;
                return;
            }
            List<Node> currentClique = new ArrayList<>(clique);
            currentClique.add(candidate);

            List<Node> currentCandidates = new ArrayList<>(candidates);
            int trimIndex = currentCandidates.indexOf(candidate); //exclude edge repeatness
            currentCandidates = currentCandidates.subList(trimIndex, currentCandidates.size());
            currentCandidates.retainAll(candidate.getNeighbours());

            if (currentCandidates.size() > 0) {
                Map<Integer, Integer> newColors = GreedyGraphColoringAlgorithm.forNodes(currentCandidates);
                currentCandidates.sort(Comparator.<Node>comparingInt(value -> newColors.get(value.getIndex())).reversed());
                branchAndBounds(currentCandidates, currentClique, newColors);
            } else if (cliqueMax.size() < currentClique.size()) {
                cliqueMax = currentClique;
            }
        }
    }
}
