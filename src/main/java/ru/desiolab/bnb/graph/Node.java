package ru.desiolab.bnb.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {
    private final int index;
    private final List<Node> neighbours;

    public Node(int index) {
        this.index = index;
        this.neighbours = new ArrayList<>();
    }

    public int getIndex() {
        return index;
    }

    public void addNeighbour(Node node) {
        if (!neighbours.contains(node)) {
            neighbours.add(node);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return index == node.index &&
                Objects.equals(neighbours, node.neighbours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, neighbours);
    }
}
