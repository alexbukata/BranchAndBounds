package ru.desiolab.bnb.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ru.desiolab.bnb.graph.Node;

import java.util.List;
import java.util.Set;

public class BranchAndBoundsCplex {
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

    public void compute() {
        try {
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

            cplex.solve();

            double objValue = cplex.getObjValue();
            System.out.println(objValue);
            System.out.println();
            for (int i = 1; i < x.length; i++) {
                System.out.println(i + ": " + cplex.getValue(x[i]));
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
