package ru.desiolab.bnb;

import ru.desiolab.bnb.graph.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TestResources {
    public static void main(String[] args) throws IOException {
        File resourcesDir = new File("resources");
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        Application.GraphJob graphJob = new Application.GraphJob();
        List<File> files = Arrays.stream(resourcesDir.listFiles()).sorted(Comparator.comparingLong(File::length)).collect(Collectors.toList());
        for (File testFile : files) {
            long first = System.currentTimeMillis();
            List<Node> nodes = graphJob.graphJob(testFile.getPath());
            float result = (System.currentTimeMillis() - first) / 1000f;
            System.out.println("done");
            writer.write(testFile.getName() + " " + nodes.size() + " " + result);
            writer.write("\n");
            writer.flush();
        }
        writer.close();
    }
}
