package projects.cbOptNet.nodes.infrastructureImplementations;

import java.util.ArrayList;
import java.util.Collections;

import projects.opticalNet.nodes.models.InfraNode;

/**
 * The weight tree is a segtree representing the counters of each node in the base of the segtree
 * and the weight of node based on the sum of the range between the minimum and maximum
 * id of a node subtree.
 */
public class WeightTree {

    /* Attributes */

    private ArrayList<Integer> segTree;

    private int size;

    /* End of Attributes */

    /**
     * Constructor of the segtree, initializes all it's nodes with the counter equals to 0.
     * @param numNodes  the number of nodes in the network
     */
    public WeightTree (int numNodes) {
        this.size = numNodes;
        this.segTree = new ArrayList<>(Collections.nCopies(size * 2 + 1, 0));
    }

    /**
     * Getter for the weight of a InfraNode subtree.
     * @param node  the InfraNode
     * @return      the sum of counters in the range minId to maxId
     */
    public int getWeight (InfraNode node) {
        int minId = node.getMinId();
        int maxId = node.getMaxId();

        return this.query(minId, maxId);
    }

    /**
     * Getter for the new weight a InfraNode's subtree would hold if he had a different child
     * @param node          the InfraNode
     * @param newChild      the new child for node
     * @param leftChild     true if the new child is a leftChild, important for dummy nodes
     * @return              the sum of counters in the range minId to maxId
     */
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

    /**
     * Getter for the number of occurences, where a node was source or destination of
     * a message. Equals to the counter on the base of the segtree.
     * @param node  the InfraNode
     * @return      the counter value for node
     */
    public int getOccurrences (InfraNode node) {
        return this.segTree.get(node.getId() + this.size);

    }

    /**
     * Returns the sum of counters between minId and maxId in O(logn)
     * @param minId (int) minId in the subtree
     * @param maxId (int) maxId in the subtree
     * @return      the sum of counters in the range minId to maxId
     */
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

    /**
     * Increment the counter of InfraNode and propagate this change over the segtree.
     * @param node  InfraNode
     */
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
