package ru.desiolab.bnb;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import ru.desiolab.bnb.algorithms.BranchAndBoundsMaxClique;
import ru.desiolab.bnb.algorithms.GreedyGraphColoringAlgorithm;
import ru.desiolab.bnb.graph.Graph;
import ru.desiolab.bnb.graph.GraphParser;
import ru.desiolab.bnb.graph.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Application {
    public static void main(String[] args) throws InterruptedException {
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
            System.out.println("Time: " + (System.currentTimeMillis() - first) / 1000f + "s");
            return;
        }

        try {
            simpleTimeLimiter.runWithTimeout(() -> graphJob.graphJob(pathToGraph), timeLimit, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.out.println("0 " + Arrays.toString(graphJob.getAlgorithm().getCliqueMax().stream().map(Node::getIndex).toArray()));
        }
        System.exit(0);
    }

    public static class GraphJob {
        private BranchAndBoundsMaxClique algorithm;

        public List<Node> graphJob(String pathToGraph) {
            Graph graph;
            try (BufferedReader reader = new BufferedReader(new FileReader(pathToGraph))) {
                graph = GraphParser.fromStream(reader.lines());
            } catch (IOException e) {
                throw new IllegalStateException("Error", e);
            }
            Map<Integer, Integer> colors = GreedyGraphColoringAlgorithm.forNodes(graph.getNodes());
            algorithm = new BranchAndBoundsMaxClique(graph);
            List<Node> clique = algorithm.findClique(colors);

            return clique;
        }

        public BranchAndBoundsMaxClique getAlgorithm() {
            return algorithm;
        }
    }
}
