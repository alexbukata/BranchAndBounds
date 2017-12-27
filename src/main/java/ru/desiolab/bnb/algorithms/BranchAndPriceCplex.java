package ru.desiolab.bnb.algorithms;

import com.google.common.collect.Lists;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ru.desiolab.bnb.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

public class BranchAndPriceCplex {
    private final static double EPSILON = 0.01;

    private final List<Node> nodes;
    private final List<Set<Node>> independentSets;
    private final Integer chromaticNumber;
    private List<Integer> maxClique;
    private IloCplex cplex;
    private IloNumVar[] x;

    public BranchAndPriceCplex(List<Node> nodes,
                               List<Set<Node>> independentSets,
                               Integer chromaticNumber) {
        this.nodes = nodes;
        this.independentSets = independentSets;
        this.chromaticNumber = chromaticNumber;
        this.maxClique = new ArrayList<>();
    }

    private void initSolver() throws IloException {
        this.cplex = new IloCplex();
        cplex.setOut(null);
        this.x = new IloNumVar[nodes.size() + 1];
        for (int i = 1; i <= nodes.size(); i++) {
            x[i] = cplex.numVar(0, 1);
        }
        IloLinearNumExpr objective = cplex.linearNumExpr();
        for (int i = 1; i <= nodes.size(); i++) {
            objective.addTerm(1, x[i]);
        }
        cplex.addMaximize(objective);

        BranchAndBoundConstraints.independentSetsConstraints(cplex, independentSets, x);
    }

    public void compute() {
        try {
            initSolver();
            branching();
//            System.out.println("Answer: " + maxClique);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void branching() throws IloException {
        if (!cplex.solve() || Math.floor(cplex.getObjValue()) < maxClique.size() || maxClique.size() >= chromaticNumber) {
            return;
        }
        Double[] xValues = new Double[x.length];
        Integer branchCandidate = -1;
        List<Integer> currentClique = new ArrayList<>();
        for (int i = 1; i < x.length; i++) {
            double value = cplex.getValue(x[i]);
            xValues[i] = value;
        }
        for (int i = 1; i < x.length; i++) {
            double value = cplex.getValue(x[i]);
            if (value < 1.0 - EPSILON && value > 0.0 + EPSILON) {
                branchCandidate = i;
                break;
            }
            if (1.0 - value < EPSILON) {
                currentClique.add(i);
            }
        }
        if (branchCandidate == -1) {
            if (checkIfClique(xValues)) {
                if (maxClique.size() < currentClique.size()) {
//                System.out.println("New max clique has been found!");
//                System.out.println(currentClique + " (" + currentClique.size() + ")");
                    maxClique = currentClique;
                }
                return;
            } else {
                List<Integer> indexes = Lists.newArrayList();
                for (int i = 1; i < xValues.length; i++) {
                    if (Math.abs(xValues[i] - 1.0) < EPSILON) {
                        indexes.add(i);
                    }
                }
                List<Node> newConstraints = nodes.stream().filter(n -> indexes.contains(n.getIndex())).collect(Collectors.toList());
                BranchAndBoundConstraints.roundRobinConstraints(cplex, newConstraints, x);
                branching();
                return;
            }
        }
        Set<Node> mostBrokenConstraint = calculateMostBrokenConstraint(xValues);
        if (mostBrokenConstraint == null) {
            IloRange firstBranchConstraint = cplex.addGe(x[branchCandidate], 1);
            branching();
            cplex.remove(firstBranchConstraint);

            IloRange secondBranchConstraint = cplex.addLe(x[branchCandidate], 0);
            branching();
            cplex.remove(secondBranchConstraint);
        } else {
            BranchAndBoundConstraints.independentSetsConstraints(cplex, Collections.singletonList(mostBrokenConstraint), x);
            branching();
        }
    }

    private boolean checkIfClique(Double[] xValues) {
        List<Integer> indexes = Lists.newArrayList();
        for (int i = 1; i < xValues.length; i++) {
            if (Math.abs(xValues[i] - 1.0) < EPSILON) {
                indexes.add(i);
            }
        }
        List<Node> clique = nodes.stream().filter(indexes::contains).collect(Collectors.toList());
        for (Node node1 : clique) {
            for (Node node2 : clique) {
                if (!node1.equals(node2) && !(node1.getNeighbours().contains(node2) && node2.getNeighbours().contains(node1))) {
                    return false;
                }
            }
        }
        return true;
    }

    private Set<Node> calculateMostBrokenConstraint(Double[] xValues) {
        Map<Node, Double> candidateNodes = new HashMap<>();
        for (Node node : nodes) {
            if (xValues[node.getIndex()] > EPSILON)
                candidateNodes.put(node, (double) node.getNeighbours().size());
        }
        List<Node> resultIndependentSet = new ArrayList<>();
        Set<Node> allowedNodes = new HashSet<>(candidateNodes.keySet());
        generateIndependentSet(allowedNodes, candidateNodes, resultIndependentSet);

        Double sum = 0.0;
        for (Node node : resultIndependentSet) {
            sum += xValues[node.getIndex()];
        }
        if (sum > 1 + EPSILON) {
            return new HashSet<>(resultIndependentSet);
        }
        return null;
    }

//    private Set<Node> calculateMostBrokenConstraint(Double[] xValues) {
//        Map<Integer, Double> indexesFractial = new HashMap<>();
//        Map<Integer, Double> indexesIntegral = new HashMap<>();
//        for (int i = 1; i < xValues.length; i++) {
//            if (xValues[i] > EPSILON && xValues[i] < 1.0 - EPSILON) {
//                indexesFractial.put(i, xValues[i]);
//            } else {
//                indexesIntegral.put(i, xValues[i]);
//            }
//        }
//        Map<Node, Double> candidateNodesFractial = new HashMap<>();
//        Map<Node, Double> candidateNodesIntegral = new HashMap<>();
//        for (Node node : nodes) {
//            if (indexesFractial.keySet().contains(node.getIndex())) {
//                candidateNodesFractial.put(node, indexesFractial.get(node.getIndex()));
//            } else if (indexesIntegral.keySet().contains(node.getIndex())) {
//                candidateNodesIntegral.put(node, 1.0);
//            } else {
//                throw new IllegalStateException("WATAFACK");
//            }
//        }
//        List<Node> resultIndependentSet = new ArrayList<>();
//        Set<Node> allowedFractialNodes = new HashSet<>(candidateNodesFractial.keySet());
//        generateIndependentSet(allowedFractialNodes, candidateNodesFractial, resultIndependentSet);
//
//        Set<Node> permittedNodes = new HashSet<>();
//        for (Node node : resultIndependentSet) {
//            permittedNodes.addAll(node.getNeighbours());
//        }
//        Set<Node> allowedIntegralNodes = new HashSet<>(candidateNodesIntegral.keySet()).stream().filter(node -> !permittedNodes.contains(node)).collect(Collectors.toSet());
//        generateIndependentSet(allowedIntegralNodes, candidateNodesIntegral, resultIndependentSet);
//        Double sum = 0.0;
//        for (Node node : resultIndependentSet) {
//            sum += xValues[node.getIndex()];
//        }
//        if (sum > 1) {
//            return new HashSet<>(resultIndependentSet);
//        }
//        return null;
//    }

    public void generateIndependentSet(Set<Node> allowedNodes, Map<Node, Double> candidateNodes, List<Node> resultIndependentSet) {
        List<Map.Entry<Node, Double>> sorted = new ArrayList<>(new HashMap<>(candidateNodes).entrySet()).stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList());
        Double cumulative = 0.0;
        while (!allowedNodes.isEmpty()) {
            List<Map.Entry<Node, Double>> integralCandidates = new ArrayList<>(sorted).stream().filter(entry -> allowedNodes.contains(entry.getKey())).collect(Collectors.toList());
            Double norm = integralCandidates.stream().map(entry -> candidateNodes.get(entry.getKey())).reduce(0.0001, Double::sum);
            for (Map.Entry<Node, Double> entry : integralCandidates) {
                entry.setValue(candidateNodes.get(entry.getKey()) / norm);
                cumulative += entry.getValue();
                if (new Random().nextDouble() < cumulative) {
                    resultIndependentSet.add(entry.getKey());
                    allowedNodes.remove(entry.getKey());
                    allowedNodes.removeAll(entry.getKey().getNeighbours());
                    cumulative = 0.0;
                    break;
                }
            }
        }
    }

    public List<Integer> getMaxClique() {
        return maxClique;
    }
}
