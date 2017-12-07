package ru.desiolab.bnb.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ru.desiolab.bnb.graph.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BranchAndBoundsCplex {
    private final static double EPSILON = 0.01;

    private final List<Node> nodes;
    private final List<Set<Node>> independentSets;
    private final Integer chromaticNumber;

    public BranchAndBoundsCplex(List<Node> nodes,
                                List<Set<Node>> independentSets,
                                Integer chromaticNumber) {
        this.nodes = nodes;
        this.independentSets = independentSets;
        this.chromaticNumber = chromaticNumber;
    }

    private IloCplex initSolver() throws IloException {
        IloCplex cplex = new IloCplex();
        IloNumVar[] x = new IloNumVar[nodes.size() + 1];
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

        return cplex;
    }

    public void compute() {
        try {
            IloCplex cplex = initSolver();
            cplex.solve();
            Map<Integer, Integer> additionalNodesConstraints = new HashMap<>();
            branching(cplex, additionalNodesConstraints);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void branching(IloCplex previousSolver, Map<Integer, Integer> additionalNodesConstraints) throws IloException {
        Integer branchCandidate = -1;
        for (int i = 1; i < x.length; i++) {
            double value = previousSolver.getValue(x[i]);
            if (value < 1.0 - EPSILON || value > 0.0 + EPSILON) {
                branchCandidate = i;
                break;
            }
        }
        if (branchCandidate == -1) {
            System.out.println("Clique has been found!");
            return;
        }
        IloCplex branch1 = initSolver();
        HashMap<Integer, Integer> firstBranchConstraints = new HashMap<>(additionalNodesConstraints);
        firstBranchConstraints.put(branchCandidate, 0);
        for (Map.Entry<Integer, Integer> constraint : firstBranchConstraints.entrySet()) {
            IloLinearIntExpr linearIntExpr = branch1.linearIntExpr();
            linearIntExpr.addTerm(x[constraint.getKey()]);
            if (constraint.getValue() == 0) {
            }
        }

        IloCplex branch2 = initSolver();
        HashMap<Integer, Integer> secondBranchConstraints = new HashMap<>(additionalNodesConstraints);
        secondBranchConstraints.put(branchCandidate, 1);
    }
}
