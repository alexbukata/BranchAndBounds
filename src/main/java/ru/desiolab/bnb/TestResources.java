package ru.desiolab.bnb;

import ru.desiolab.bnb.graph.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TestResources {
    public static void main(String[] args) throws IOException {
        File resourcesDir = new File("resources");
        BufferedWriter writer = new BufferedWriter(new FileWriter("resources/output.txt"));
        Application.GraphJob graphJob = new Application.GraphJob();
        for (String testFileName : resourcesDir.list()) {
            long first = System.currentTimeMillis();
            List<Node> nodes = graphJob.graphJob("resources/" + testFileName);
            float result = (System.currentTimeMillis() - first) / 1000f;
            System.out.println("done");
            writer.write(testFileName + " " + nodes.size() + " " + result);
            writer.write("\n");
            writer.flush();
        }
        writer.close();
    }
}
