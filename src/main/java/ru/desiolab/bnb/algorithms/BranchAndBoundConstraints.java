package ru.desiolab.bnb.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ru.desiolab.bnb.graph.Node;

import java.util.List;
import java.util.Set;

public class BranchAndBoundConstraints {
    public static void roundRobinConstraints(IloCplex cplex, List<Node> nodes, IloNumVar[] vars) throws IloException {
        for (Node node1 : nodes) {
            for (Node node2 : nodes) {
                if (node1.getNeighbours().contains(node2) || node1.equals(node2)) {
                    continue;
                }
                IloLinearNumExpr constraint = cplex.linearNumExpr();
                constraint.addTerm(1, vars[node1.getIndex()]);
                constraint.addTerm(1, vars[node2.getIndex()]);
                cplex.addLe(constraint, 1);
            }
        }
    }

    public static void eachNodeConstraint(IloCplex cplex, List<Node> nodes, IloNumVar[] vars) throws IloException {
        for (Node node : nodes) {
            IloLinearNumExpr constraint = cplex.linearNumExpr();
            constraint.addTerm(1, vars[node.getIndex()]);
            cplex.addLe(constraint, 1);
        }
    }

    public static void independentSetsConstraints(IloCplex cplex, List<Set<Node>> independentSets, IloNumVar[] vars) throws IloException {
        for (Set<Node> independentSet : independentSets) {
            IloLinearNumExpr constraint = cplex.linearNumExpr();
            for (Node node : independentSet) {
                constraint.addTerm(1, vars[node.getIndex()]);
            }
            cplex.addLe(constraint, 1);
        }
    }
}
