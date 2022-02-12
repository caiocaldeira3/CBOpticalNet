package projects.cbOptNet.nodes.infrastructureImplementations;

import java.util.ArrayList;
import java.util.Collections;

import projects.opticalNet.nodes.models.InfraNode;

public class WeightTree {

    /* Attributes */

    private ArrayList<Integer> segTree;

    private int size;

    /* End of Attributes */

    public WeightTree (int numNodes) {
        this.size = numNodes;
        this.segTree = new ArrayList<>(Collections.nCopies(size * 2 + 1, 0));
    }

    public int getWeight (InfraNode node) {
        int minId = node.getMinId();
        int maxId = node.getMaxId();

        return this.query(minId, maxId);
    }

    public int getNewWeight (InfraNode node, InfraNode newChild, boolean leftChild) {
        int minId, maxId;
        if (leftChild) {
            minId = (newChild.getId() != -1 ? newChild.getMinId() : node.getId());
            maxId = node.getMaxId();
        } else {
            minId = node.getMinId();
            maxId = (newChild.getId() != -1 ? newChild.getMaxId() : node.getId());
        }

        return this.query(minId, maxId);
    }

    public int getOccurrences (InfraNode node) {
        return this.segTree.get(node.getId() + this.size);

    }

    public int query (int minId, int maxId) {
        int weight = 0;

        for (int a = minId + this.size, b = maxId + this.size; a <= b; a /= 2, b /= 2) {
            if (a % 2 == 1) {
                weight += this.segTree.get(a);
                a += 1;
            }
            if (b % 2 == 0) {
                weight += this.segTree.get(b);
                b -= 1;
            }
        }

        return weight;
    }

    public void incrementWeight (InfraNode node) {
        int nodeId = node.getId();

        int pos = nodeId + this.size;
        this.segTree.set(pos, this.segTree.get(pos) + 1);

        pos /= 2;
        while (pos > 0) {
            int x = this.segTree.get(pos * 2);
            int y = this.segTree.get(pos * 2 + 1);

            this.segTree.set(pos, x + y);

            pos /= 2;
        }
    }

}
