package ru.desiolab.bnb;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import ru.desiolab.bnb.graph.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class TestResources {
    public static void main(String[] args) throws IOException {
        File resourcesDir = new File("resources");
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        Application.GraphJob graphJob = new Application.GraphJob();
        List<File> files = Arrays.stream(resourcesDir.listFiles()).sorted(Comparator.comparingLong(File::length)).collect(Collectors.toList());
        for (File testFile : files) {
            System.out.println(testFile.getName());
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            SimpleTimeLimiter simpleTimeLimiter = SimpleTimeLimiter.create(executorService);
            try {
                long first = System.currentTimeMillis();
                simpleTimeLimiter.runWithTimeout(() -> graphJob.graphJob(testFile.getPath()), 10, TimeUnit.SECONDS);
                float result = (System.currentTimeMillis() - first) / 1000f;
                System.out.println("done");
                List<Node> nodes = graphJob.getAlgorithm().getCliqueMax();
                writer.write(testFile.getName() + " " + nodes.size() + " " + result + "sec");
                writer.write("\n");
            } catch (TimeoutException e) {
                List<Node> nodes = graphJob.getAlgorithm().getCliqueMax();
                writer.write(testFile.getName() + " " + nodes.size() + " >=1hr");
                writer.write("\n");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writer.flush();
        }
        writer.close();
    }
}
