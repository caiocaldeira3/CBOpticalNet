package projects.opticalNet.nodes.models;

import sinalgo.tools.Tools;

/**
 * InfraNodes are used by the NetworkController to represent the current
 * network topology. They store the left and right child and it's parent,
 * as well as their subtree min and max id.
 */
public class InfraNode implements Comparable<InfraNode> {

    /* Attributes */
    private InfraNode parent = null;
    private InfraNode leftChild = null;
    private InfraNode rightChild = null;

    private int minId = -1;
    private int maxId = -1;

    private int id = 0;
    private static int ID = 0;
    /* End of Attributes */

    /* Constructors */

    /**
     * Create a InfraNode unitialized, without children nor parents.
     */
    public InfraNode () {
        this.id = ID++;
        this.parent = new InfraNode(-1);
        this.leftChild = new InfraNode(-1);
        this.rightChild = new InfraNode(-1);
        this.minId = this.id;
        this.maxId = this.id;
    }

    /**
     * Create a dummy node, that should be deemed invalid during the simulation.
     * @param dummy variable to represent dummy id, should be -1
     */
    public InfraNode (int dummy) {
        this.id = dummy;
        this.minId = this.id;
        this.maxId = this.id;
    }

    /**
     * Create a new InfraNode already informing it's parent and children, already initializing
     * it's subtree
     * @param parent        This node parent or a dummy node
     * @param leftChild     This node leftChild or a dummy node
     * @param rightChild    This node rightChild or a dummy node
     */
    public InfraNode (InfraNode parent, InfraNode leftChild, InfraNode rightChild) {
        this.id = ID++;
        this.parent = parent;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.minId = this.leftChild.getId() == -1 ? this.getId() : this.leftChild.getMinId();
        this.maxId = this.rightChild.getId() == -1 ? this.getId() : this.rightChild.getMaxId();
    }
    /* End of Constructors */

    /* Getters */

    /**
     * Getter for this node id in the InfraTree.
     * @return This node id
     */
    public int getId () {
        return this.id;

    }

    /**
     * Getter for the id of this node equivalent NetworkNode
     * @return The equivalent NetworkNode id
     */
    public int getNetId () {
        return this.id + 1;

    }

    /**
     * Getter for this node parent
     * @return This node parent
     */
    public InfraNode getParent () {
        return this.parent;
    }

    /**
     * Getter for this node left child
     * @return This node left child
     */
    public InfraNode getLeftChild () {
        return this.leftChild;
    }

    /**
     * Getter for this node right child
     * @return This node right child
     */
    public InfraNode getRightChild () {
        return this.rightChild;
    }

    /**
     * Getter for this node's subtree minimum id
     * @return This node's subtree minimum id
     */
    public int getMinId () {
        return this.minId;
    }

    /**
     * Getter for this node's subtree maximum id
     * @return This node's subtree maximum id
     */
    public int getMaxId () {
        return this.maxId;
    }
    /* End of Getters */

    /* Setters */
    /**
     * Sets this node new parent and resets the respective child from this
     * node old parent.
     * @param parent This node new parent
     */
    public void setParent (InfraNode parent) {
        if (this.getId() == -1)
            return;

        this.parent.resetChild(this);
        this.parent = parent;
    }

    /**
     * Sets this node new child, calling the apropriated left or right
     * method depending on the comparison betwen this and the child id.
     * This method is used when the child is assuredly not a dummy node.
     * @param child The new child node
     */
    public void setChild (InfraNode child) {
        if (child.getId() == -1) {
            Tools.fatalError(
                "Trying to add a Dummy node to " + ID + " without specifying its parent"
            );

        } else if (this.getId() > child.getId()) {
            this.getLeftChild().resetParent(this);
            this.setLeftChild(child);

        } else {
            this.getRightChild().resetParent(this);
            this.setRightChild(child);

        }
    }

    /**
     * Sets this node new child, calling the apropriated left or right
     * method depending on the comparison betwen this and the child id.
     * If the child is a dummy node, the decision on wether left or right
     * child is made by the child old parent id. [ CONFIRM DECISION ]
     * @param child      new child node
     * @param oldParent  new child old parent
     */
    public void setChild (InfraNode child, InfraNode oldParent) {
        if (child.getId() == -1) {
            if (this.getId() > oldParent.getId()) {
                this.setLeftChild(child);

            } else {
                this.setRightChild(child);

            }

        } else if (this.getId() > child.getId()) {
            this.getLeftChild().resetParent(this);
            this.setLeftChild(child);

        } else {
            this.getRightChild().resetParent(this);
            this.setRightChild(child);

        }
    }

    /**
     * Set the left child of this node as child and set this child parent as this.
     * After that update the minimum id of the subtree.
     * @param child new child node
     */
    public void setLeftChild (InfraNode child) {
        if (this.getId() != -1) {
            child.setParent(this);
            this.leftChild = child;

            this.updateMin(child);

        }

    }

    /**
     * Set the right child of this node as child and set this child parent as this.
     * After that update the maximum id of the subtree.
     * @param child new child node
     */
    public void setRightChild (InfraNode child) {
        if (this.getId() != -1) {
            child.setParent(this);
            this.rightChild = child;

            this.updateMax(child);

        }
    }

    /**
     * Remove its node child equals to rstNode, removing its subtree for the
     * respective left or right side.
     * @param rstNode the old child node.
     */
    public void resetChild (InfraNode rstNode) {
        if (this.getId() != -1) {
            if (this.leftChild.getId() != -1 && this.leftChild == rstNode) {
                this.updateMin(new InfraNode(-1));
                this.leftChild = new InfraNode(-1);

            } else if (this.rightChild.getId() != -1 && this.rightChild == rstNode) {
                this.updateMax(new InfraNode(-1));
                this.rightChild = new InfraNode(-1);

            }

        }
    }

    /**
     * Reset this node parent. Setting it as a dummy node.
     * @param rstNode this node old parent.
     */
    public void resetParent (InfraNode rstNode) {
        if (this.getId() == -1) {
            return;

        }

        if (this.parent.getId() != -1 && this.parent.getId() == rstNode.getId()) {
            this.parent = new InfraNode(-1);

        }
    }

    /**
     * Update minimum id from this node subtree. Setting it as this node id if the new child
     * is a dummy node or as the child node minimum id in their subtree.
     * @param child This node new child
     */
    public void updateMin (InfraNode child) {
        if (child.getId() == -1) {
            this.minId = this.getId();

        } else {
            this.minId = child.getMinId();

        }
    }

    /**
     * Update maximum id from this node subtree. Setting it as this node id if the new child
     * is a dummy node or as the child node maximum id in their subtree.
     * @param child This node new child
     */
    public void updateMax (InfraNode child) {
        if (child.getId() == -1) {
            this.maxId = this.getId();

        } else {
            this.maxId = child.getMaxId();

        }

    }

    public InfraNode getRoutingNode (InfraNode toNode) {
        return this.getRoutingNode(this.getRoutingDirection(toNode));

    }

    /**
     * Getter for which node is next in the specified direction.
     * Left child for the LEFT and LEFTROUT direction
     * Right child for the RIGHT and RIGHTROUT direction
     * Parent for the PARENT and PARENTROUT direction
     * This for the NULL direction
     * @param direction     the routing direction.
     * @return              the next node in the direction.
     */
    public InfraNode getRoutingNode (Direction direction) {
        if (direction == Direction.NULL) {
            return this;

        } else if (direction == Direction.RIGHT || direction == Direction.RIGHTROUT) {
            return this.rightChild;

        } else if (direction == Direction.LEFT || direction == Direction.LEFTROUT) {
            return this.leftChild;

        } else {
            return this.parent;

        }
    }

    /**
     * Getter for the routing direction the next node in the path between this and the
     * destination node is.
     * @param toNode    destination node
     * @return          the routing the direction the next node in the path is
     */
    public Direction getRoutingDirection (InfraNode toNode) {
        if (toNode.getId() == -1) {
            System.out.println("Invalid toNode");
            Tools.fatalError("Invalid toNode");

        }

        if (this == toNode) {
            return Direction.NULL;

        } else if (this.getLeftChild() == toNode) {
            return Direction.LEFTROUT;

        } else if (this.getRightChild() == toNode) {
            return Direction.RIGHTROUT;

        } else if (this.getParent() == toNode) {
            return Direction.PARENTROUT;

        } else if (this.getId() < toNode.getId() && toNode.getId() <= this.maxId) {
            return Direction.RIGHT;

        } else if (this.minId <= toNode.getId() && toNode.getId() < this.getId()) {
            return Direction.LEFT;

        } else {
            return Direction.PARENT;

        }
    }

    /* End of Setters */

    /* Auxiliary Functions */

    /**
     * Method to print this InfraNode informations, such as his left and right child ids,
     * his parent id, and his minimum and maximum id of his subtree.
     */
    public void debugNode () {
        if (this.id == -1) {
            System.out.println("Dummy Node");

        } else {
            System.out.println(
                "INFRAID: " + this.getId()
                + " lftID: " + this.getLeftChild().getId()
                + " rgtID: " + this.getRightChild().getId()
                + " parentId: " + this.getParent().getId()
                + " leftSUB: " + this.getMinId() + " rightSUB: " + this.getMaxId()
            );

        }
    }

    /**
     * Comparator between two InfraNodes, returning less than if this node is a dummy node,
     * bigger than if the other node is a dummy node. And comparing their id if both of them
     * are valid nodes.
     */
    @Override
    public int compareTo (InfraNode othNode) {
        if (this.getId() == -1) {
            return -1;

        } else if (othNode.getId() == -1) {
            return 1;

        }

        return Integer.compare(this.getId(), othNode.getId());
    }
    /* End of Auxiliary Functions */
}
