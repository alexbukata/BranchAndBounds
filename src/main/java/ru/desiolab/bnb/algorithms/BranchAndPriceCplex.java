package ru.desiolab.bnb.algorithms;

import com.google.common.collect.Lists;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ru.desiolab.bnb.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
        List<Node> neighbours = nodes.stream().filter(n -> n.getIndex() == indexes.get(0)).findFirst().get().getNeighbours();
        for (Integer index : indexes) {
            Node currentNode = nodes.stream().filter(n -> n.getIndex() == index).findFirst().get();
            List<Node> currentNeighbours = currentNode.getNeighbours();
            neighbours.retainAll(currentNeighbours);
            neighbours.add(currentNode);
        }
        return neighbours.size() == indexes.size();
    }

    //TODO What the fuck?
    private Set<Node> calculateMostBrokenConstraint(Double[] xValues) {
        ArrayList<Node> nodesCopy = Lists.newArrayList(this.nodes);
        Collections.shuffle(nodesCopy);
        GreedyGraphColoringAlgorithm coloringAlgorithm = new GreedyGraphColoringAlgorithm(nodesCopy);
        coloringAlgorithm.calculate();
        List<Set<Node>> independentSets = coloringAlgorithm.getIndependentSets();
        double maxValue = -1;
        Set<Node> mostBroken = null;
        for (Set<Node> independentSet : independentSets) {
            int constraintValue = 0;
            for (Node node : independentSet) {
                constraintValue += xValues[node.getIndex()];
            }
            if (constraintValue > 1 && constraintValue > maxValue) {
                maxValue = constraintValue;
                mostBroken = independentSet;
            }
        }
        return mostBroken;
    }

    public List<Integer> getMaxClique() {
        return maxClique;
    }
}
