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
        List<Node> nodes = graph.getNodes();
        nodes.sort(Comparator.<Node>comparingInt(value -> colors.get(value.getIndex())).reversed());

        cliqueMax = new ArrayList<>();
        branchAndBounds(nodes, new ArrayList<>(), colors);
        return cliqueMax;
    }

    private void branchAndBounds(List<Node> candidates, List<Node> clique, final Map<Integer, Integer> colors) {
        for (Node candidate : candidates) {
            if (clique.size() + colors.get(candidate.getIndex()) <= cliqueMax.size()) { // |Q| + max{Color(candidate)} <= |Qmax|
                return;
            }
            List<Node> currentClique = new ArrayList<>(clique);
            currentClique.add(candidate);

            List<Node> currentCandidates = new ArrayList<>(candidates);
            int trimIndex = currentCandidates.indexOf(candidate); //exclude edge repeatness
            currentCandidates = currentCandidates.subList(trimIndex, currentCandidates.size());
            currentCandidates.retainAll(candidate.getNeighbours()); // clean up candidates (saving order)

            if (currentCandidates.size() > 0) {
                GreedyGraphColoringAlgorithm coloringAlgorithm = new GreedyGraphColoringAlgorithm(currentCandidates);
                coloringAlgorithm.calculate();
                Map<Integer, Integer> newColors = coloringAlgorithm.getColors(); //recalculate subgraph colors
                currentCandidates.sort(Comparator.<Node>comparingInt(value -> newColors.get(value.getIndex())).reversed()); //sort by color numbers (descending)
                branchAndBounds(currentCandidates, currentClique, newColors);
            } else if (cliqueMax.size() < currentClique.size()) {
                cliqueMax = currentClique;
            }
        }
    }

    public List<Node> getCliqueMax() {
        return cliqueMax;
    }
}
