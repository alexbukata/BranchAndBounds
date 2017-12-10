package ru.desiolab.bnb;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import ilog.concert.IloException;
import ru.desiolab.bnb.algorithms.BranchAndBoundsCplex;
import ru.desiolab.bnb.algorithms.GreedyGraphColoringAlgorithm;
import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.GraphParser;
import ru.desiolab.bnb.graph.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BranchAndBoundCplexApplication {
    public static void main(String[] args) throws IloException {
        String pathToGraph = args[0];

        GraphJob graphJob = new GraphJob();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        SimpleTimeLimiter simpleTimeLimiter = SimpleTimeLimiter.create(executorService);

        Integer timeLimit;
        if (args.length > 1) {
            timeLimit = Integer.parseInt(args[1]);
        } else {
            long first = System.currentTimeMillis();
            graphJob.graphJob(pathToGraph);
            System.out.print((System.currentTimeMillis() - first) / 1000f + "s");
            System.out.println(" " + graphJob.getAlgorithm().getMaxClique().size() + " " + graphJob.getAlgorithm().getMaxClique());
            return;
        }

        try {
            simpleTimeLimiter.runWithTimeout(() -> graphJob.graphJob(pathToGraph), timeLimit, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.out.println("0 " + graphJob.getAlgorithm().getMaxClique());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static class GraphJob {
        private BranchAndBoundsCplex branchAndBoundsCplex;

        public List<Integer> graphJob(String pathToGraph) {
            Graph graph;
            try (BufferedReader reader = new BufferedReader(new FileReader(pathToGraph))) {
                graph = GraphParser.fromStream(reader.lines());
            } catch (IOException e) {
                throw new IllegalStateException("Error", e);
            }
            GreedyGraphColoringAlgorithm coloringAlgorithm = new GreedyGraphColoringAlgorithm(graph.getNodes());
            coloringAlgorithm.calculate();
            List<Set<Node>> independentSets = coloringAlgorithm.getIndependentSets();
            int chromaticNumber = independentSets.size();
            this.branchAndBoundsCplex = new BranchAndBoundsCplex(graph.getNodes(), independentSets, chromaticNumber);
            branchAndBoundsCplex.compute();
            return branchAndBoundsCplex.getMaxClique();
        }

        public BranchAndBoundsCplex getAlgorithm() {
            return branchAndBoundsCplex;
        }
    }
}
