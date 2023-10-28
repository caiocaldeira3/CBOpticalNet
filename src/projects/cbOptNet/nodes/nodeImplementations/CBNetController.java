package projects.cbOptNet.nodes.nodeImplementations;

import java.util.ArrayList;

import projects.bstOpticalNet.nodes.messages.HasMessage;
import projects.bstOpticalNet.nodes.messages.NewMessage;
import projects.bstOpticalNet.nodes.messages.OpticalNetMessage;
import projects.bstOpticalNet.nodes.messages.RoutingInfoMessage;
import projects.bstOpticalNet.nodes.models.Direction;
import projects.bstOpticalNet.nodes.models.InfraNode;
import projects.bstOpticalNet.nodes.models.Rotation;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkController;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * The CBNetController implements the remaining abstract methods left by the NetworkControllers
 * it's constructor calls it's parent class constructor. This layer manages the management of the
 * node weight and counter, and uses as an extra check before performing a rotation.
 * This class implements the blunt of the CBNet algortithm over the OpticalNet framework.
 */
public class CBNetController extends NetworkController {
    private double epsilon = -1.5;

    /**
     * Initializes the CBNetController and makes a call for it's parent constructor.
     * This constructor builds the network as a balanced BST.
     * @param numNodes      Number of nodes in the network
     * @param switchSize    Number of input/output ports in the switch
     * @param netNodes      Array with the initialized NetworkNodes
     */
    public CBNetController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, boolean mirrored
    ) {
        super(numNodes, switchSize, netNodes, mirrored);
        this.projectName = "cbOptNet";
    }

    /**
     * Initializes the CBNetController and makes a call for it's parent constructor. If an
     * edgeList is provided the tree topology follow the specified one. If the edge list
     * can't build an BST, the constructor builds a balanced BST instead.
     * @param numNodes      Number of nodes in the network
     * @param switchSize    Number of input/output ports in the switch
     * @param netNodes      Array with the initialized NetworkNodes
     * @param edgeList      Array with the network edges, if provided.
     */
    public CBNetController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes,
        ArrayList<Integer> edgeList, boolean mirrored
    ) {
        super(numNodes, switchSize, netNodes, edgeList, mirrored);
        this.projectName = "cbOptNet";
    }

    @Override
    public void controllerStep () {
        super.controllerStep();
    }

    /* Rotations */

    /**
     * {@inheritDoc} Specific to the CBNet Controller, this method first checks if the new
     * edges disposition changes the network potential enough to justify the rotation, only
     * performing it if the change is bigger than the predefined epsilon.
     */
    @Override
    protected boolean zigZigBottomUp (InfraNode x) {
        InfraNode y = x.getParent();
        InfraNode z = y.getParent();

        boolean leftZigZig = (y == z.getLeftChild());
        InfraNode c = (leftZigZig ? y.getRightChild() : y.getLeftChild());

        double deltaRank = this.zigDiffRank(y, z);

        if (deltaRank < this.epsilon && super.zigZigBottomUp(x)) {
            this.zigZigWeightUpdate(y, z, c);

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc} Specific to the CBNet Controller, this method first checks if the new
     * edges disposition changes the network potential enough to justify the rotation, only
     * performing it if the change is bigger than the predefined epsilon.
     */
    @Override
    protected boolean zigZagBottomUp (InfraNode x) {
        InfraNode y = x.getParent();
        InfraNode z = y.getParent();

        boolean leftZigZag = (y == z.getLeftChild());
        InfraNode b = (leftZigZag) ? x.getLeftChild() : x.getRightChild();
        InfraNode c = (leftZigZag) ? x.getRightChild() : x.getLeftChild();

        double deltaRank = this.zigZagDiffRank(x, y, z);

        if (deltaRank < this.epsilon && super.zigZagBottomUp(x)) {
            this.zigZagWeightUpdate(x, y, z, b, c);

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc} Specific to the CBNet Controller, this method first checks if the new
     * edges disposition changes the network potential enough to justify the rotation, only
     * performing it if the change is bigger than the predefined epsilon.
     */
    @Override
    protected boolean zigZigLeftTopDown (InfraNode z) {
        InfraNode y = z.getLeftChild();
        InfraNode c = y.getRightChild();

        double deltaRank = this.zigDiffRank(y, z);

        if (deltaRank < this.epsilon && super.zigZigLeftTopDown(z)) {
            this.zigZigWeightUpdate(y, z, c);

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc} Specific to the CBNet Controller, this method first checks if the new
     * edges disposition changes the network potential enough to justify the rotation, only
     * performing it if the change is bigger than the predefined epsilon.
     */
    @Override
    protected boolean zigZigRightTopDown (InfraNode z) {
        InfraNode y = z.getRightChild();
        InfraNode c = y.getLeftChild();

        double deltaRank = this.zigDiffRank(y, z);

        if (deltaRank < this.epsilon && super.zigZigRightTopDown(z)) {
            this.zigZigWeightUpdate(y, z, c);

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc} Specific to the CBNet Controller, this method first checks if the new
     * edges disposition changes the network potential enough to justify the rotation, only
     * performing it if the change is bigger than the predefined epsilon.
     */
    @Override
    protected boolean zigZagLeftTopDown (InfraNode z) {
        InfraNode y = z.getLeftChild();
        InfraNode x = y.getRightChild();
        InfraNode b = x.getLeftChild();
        InfraNode c = x.getRightChild();

        double deltaRank = this.zigZagDiffRank(x, y, z);

        if (deltaRank < this.epsilon && super.zigZagLeftTopDown(z)) {
            this.zigZagWeightUpdate(x, y, z, b, c);

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc} Specific to the CBNet Controller, this method first checks if the new
     * edges disposition changes the network potential enough to justify the rotation, only
     * performing it if the change is bigger than the predefined epsilon.
     */
    @Override
    protected boolean zigZagRightTopDown (InfraNode z) {
        InfraNode y = z.getRightChild();
        InfraNode x = y.getLeftChild();
        InfraNode b = x.getRightChild();
        InfraNode c = x.getLeftChild();

        double deltaRank = this.zigZagDiffRank(x, y, z);

        if (deltaRank < this.epsilon && super.zigZagRightTopDown(z)) {
            this.zigZagWeightUpdate(x, y, z, b, c);

            return true;
        }

        return false;
    }

    /**
     * This function updates the the weigth of the y and z's subtree, after one
     * zig-zig rotation. Recalculating y's weigth with z as it's child, and z's
     * weigth with c as it's child
     * @param y initial parent of the x node in zig-zig rotation
     * @param z initial parent of the y node in zig-zig rotation
     * @param c initial child of x in zig-zig operation
     */
    private void zigZigWeightUpdate (InfraNode y, InfraNode z, InfraNode c) {
        long yOldWeight = y.getWeight();
        long zOldWeight = z.getWeight();

        long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

        long zNewWeight = zOldWeight - yOldWeight + cWeight;
        long yNewWeight = yOldWeight - cWeight + zNewWeight;

        z.setWeight(zNewWeight);
        y.setWeight(yNewWeight);

    }

    /**
     * This function updates the the weigth of the x, y and z's subtree, after one
     * zig-zag rotation. Recalculating y's weigth with b as it's child in the place of x,
     * and z's weigth with c as it's child in the place of y, and x as the parent of both
     * y and z.
     * @param x reference node in zig-zag rotation
     * @param y initial parent of the x node in zig-zag rotation
     * @param z initial parent of the y node in zig-zag rotation
     * @param b initial child of x in zig-zag operation
     * @param c initial child of x in zig-zag operation
     */
    private void zigZagWeightUpdate (
        InfraNode x, InfraNode y, InfraNode z, InfraNode b, InfraNode c
    ) {
        long xOldWeight = x.getWeight();
        long yOldWeight = y.getWeight();
        long zOldWeight = z.getWeight();

        long bWeight = (b.getId() != -1) ? b.getWeight() : 0;
        long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

        long yNewWeight = yOldWeight - xOldWeight + bWeight;
        long zNewWeight = zOldWeight - yOldWeight + cWeight;
        long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

        y.setWeight(yNewWeight);
        z.setWeight(zNewWeight);
        x.setWeight(xNewWeight);

    }
    /* End of Rotations */

    /* Private Getters */

    /**
     * Util function used to get the log2 of a value
     * @param value long integer value
     * @return      the log base 2 of this value
     */
    private double log2 (long value) {
        return (value == 0 ? 0 : Math.log(value) / Math.log(2));
    }

    /**
     * Compute the difference in rank between the current network topology
     * and the network topology after realizing a zig-zig rotation
     * @param x     the reference node
     * @param y     x parent node
     * @return      (double) the difference in rank
     */
    private double zigDiffRank (InfraNode x, InfraNode y) {
        boolean leftZig = (x == y.getLeftChild());

        InfraNode b = (leftZig) ? x.getRightChild() : x.getLeftChild();

        long xOldWeight = x.getWeight();
        long yOldWeight = y.getWeight();

        long bWeight = (b.getId() != -1) ? b.getWeight() : 0;

        long yNewWeight = yOldWeight - xOldWeight + bWeight;
        long xNewWeight = xOldWeight - bWeight + yNewWeight;

        double xOldRank = log2(xOldWeight);
        double yOldRank = log2(yOldWeight);
        double xNewRank = log2(xNewWeight);
        double yNewRank = log2(yNewWeight);

        double deltaRank = yNewRank + xNewRank - yOldRank - xOldRank;

        return deltaRank;
    }

    /**
     * Compute the difference in rank between the current network topology
     * and the network topology after realizing a zig-zag rotation
     * @param x     the reference node
     * @param y     x parent node
     * @param z     y parent node
     * @return      (double) the difference in rank
     */
    private double zigZagDiffRank (InfraNode x, InfraNode y, InfraNode z) {
        boolean lefZigZag = (y == z.getLeftChild());

        InfraNode b = lefZigZag ? x.getLeftChild() : x.getRightChild();
        InfraNode c = lefZigZag ? x.getRightChild() : x.getLeftChild();

        long xOldWeight = x.getWeight();
        long yOldWeight = y.getWeight();
        long zOldWeight = z.getWeight();

        long bWeight = b.getWeight();
        long cWeight = c.getWeight();

        long yNewWeight = yOldWeight - xOldWeight + bWeight;
        long zNewWeight = zOldWeight - yOldWeight + cWeight;
        long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

        double xOldRank = log2(xOldWeight);
        double yOldRank = log2(yOldWeight);
        double zOldRank = log2(zOldWeight);
        double xNewRank = log2(xNewWeight);
        double yNewRank = log2(yNewWeight);
        double zNewRank = log2(zNewWeight);

        double deltaRank = xNewRank + yNewRank + zNewRank - xOldRank - yOldRank - zOldRank;

        return deltaRank;
    }

    /**
     * Increments the weigth over the path between the src and dst node. Even though this
     * method is only called when the message reaches is destination, the weight updates after
     * rotations make it so that the path, even if it's altered ends up with the same distribution
     * of weigths as it would have if it wasn't altered.
     * @param src   src node of the message
     * @param dst   dst node of the message
     */
    private void incrementPathWeight (int src, int dst) {
        InfraNode srcNode = this.getInfraNode(src);
        InfraNode dstNode = this.getInfraNode(dst);

        srcNode.incrementPathWeight(dstNode, false);
    }

    /**
     * This method handles the message a CBNetCOntroller receives. If it is a OpticalNetMessage,
     * it means that this message has reached it's destination, so the number of completed
     * messages is incremented, the LoggerLayer reports this message information and the weigth
     * in the path between the src and destination node is updated. If it is a NewMessage the
     * number of received messages is incremented. If it is a HasMessage, the sender node is
     * marked as a nodeWithMessage for the controllerStep. If it is a RoutingInfoMessage, the
     * sender node is marked as a routerNode.
     */
    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof OpticalNetMessage) {
                OpticalNetMessage optmsg = (OpticalNetMessage) msg;
                this.logIncrementCompletedRequests();
                this.logMessageRouting(optmsg.getRouting());

                this.incrementPathWeight(optmsg.getSrc(), optmsg.getDst());

                this.cmpMsgs++;
                this.seq = true;

            } else if (msg instanceof NewMessage) {
                this.rcvMsgs++;

            } else if (msg instanceof HasMessage) {
                HasMessage hasmsg = (HasMessage) msg;
                this.nodesWithMsg.add(hasmsg);

            } else if (msg instanceof RoutingInfoMessage) {
                RoutingInfoMessage routmsg = (RoutingInfoMessage) msg;
                this.routingNodes.add(routmsg);

            }

        }
    }

    /**
     * Getter for the seq flag.
     * @return  True if there is a message in the network false if there isn't
     */
    public boolean getSeq () {
        return this.seq;
    }

    /**
     * Sets the seq flag as false, called when a message reaches it's destination
     */
    public void setSeq () {
        this.seq = false;
    }

}
