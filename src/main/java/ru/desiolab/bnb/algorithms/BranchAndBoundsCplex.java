package ru.desiolab.bnb.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ru.desiolab.bnb.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BranchAndBoundsCplex {
    private final static double EPSILON = 0.01;

    private final List<Node> nodes;
    private final List<Set<Node>> independentSets;
    private final Integer chromaticNumber;
    private List<Integer> maxClique;
    private IloCplex cplex;
    private IloNumVar[] x;

    public BranchAndBoundsCplex(List<Node> nodes,
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

        BranchAndBoundConstraints.eachNodeConstraint(cplex, nodes, x);
        BranchAndBoundConstraints.roundRobinConstraints(cplex, nodes, x);
        BranchAndBoundConstraints.independentSetsConstraints(cplex, independentSets, x);
    }

    public void compute() {
        try {
            initSolver();
            branching();
            System.out.println("Answer: " + maxClique);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void branching() throws IloException {
        if (!cplex.solve() || Math.floor(cplex.getObjValue()) < maxClique.size() || maxClique.size() >= chromaticNumber) {
            return;
        }
        Integer branchCandidate = -1;
        List<Integer> currentClique = new ArrayList<>();
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
            if (maxClique.size() < currentClique.size()) {
                System.out.println("New max clique has been found!");
                System.out.println(currentClique);
                maxClique = currentClique;
            }
            return;
        }
        IloRange firstBranchConstraint = cplex.addGe(x[branchCandidate], 1);
        branching();
        cplex.remove(firstBranchConstraint);

        IloRange secondBranchConstraint = cplex.addLe(x[branchCandidate], 0);
        branching();
        cplex.remove(secondBranchConstraint);
    }
}
