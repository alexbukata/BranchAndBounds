package ru.desiolab.bnb.algorithms;

import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.Node;

import java.util.*;

public class BranchAndBoundsMaxClique {

    private final Graph graph;
    private final Map<Integer, Integer> colors;
    private final Integer chromaticColor;

    public BranchAndBoundsMaxClique(Graph graph, Map<Integer, Integer> colors) {
        this.graph = graph;
        this.colors = colors;
        this.chromaticColor = Collections.max(colors.values());
    }

    public List<Node> findClique() {
        List<Node> nodes = new ArrayList<>(graph.getNodes());
        nodes.sort(Comparator.<Node>comparingInt(value -> value.getNeighbours().size()).reversed());
        return branchAndBounds(nodes, new ArrayList<>());
    }

    private List<Node> branchAndBounds(List<Node> candidates, List<Node> clique) {
        System.out.println(Arrays.toString(clique.stream().map(Node::getIndex).toArray()));
//        if (clique.size() == 15){
//            System.out.println("gotcha");
//        }
        Set<Integer> cliqueColors = new HashSet<>();
        for (Node node : clique) {
            cliqueColors.add(colors.get(node.getIndex()));
        }

        List<Node> maxClique = new ArrayList<>(clique);
        for (Node candidate : candidates) {
            Integer candidateColor = colors.get(candidate.getIndex());
            if (cliqueColors.contains(candidateColor)) {
                continue;
            }

            List<Node> newCandidates = new ArrayList<>(candidates);
            newCandidates.retainAll(candidate.getNeighbours());

            List<Node> newClique = new ArrayList<>(clique);
            newClique.add(candidate);

            newClique = branchAndBounds(newCandidates, newClique);
            if (newClique.size() >= chromaticColor) {
                return newClique;
            }
            if (maxClique.size() < newClique.size())
                maxClique = newClique;
        }
        return maxClique;
    }
}
