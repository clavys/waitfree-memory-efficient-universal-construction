package example;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class shows a wait-free universal construction based on fetch-And-Add and compare-And-Set
 */
public class FAACasUniversal<S extends Universal.State<S>> implements Universal<S>{

    /**
     * Definition of an operation node
     * @param <R> The return type of the operation stored in the operation node
     */
    private class OperationNode <R> {
        final Operation<S, R> operation;
        R result = null;
        volatile boolean done;

        /**
         * Creates an operation node for a given operation
         */
        OperationNode(Operation<S, R> operation) {
            this.operation = operation;
            this.done = false;
        }

        /**
         * Creates a dummy operation node
         */
        OperationNode () {
            this.operation = null;
            this.done = true;
        }
    }

    /**
     * Definition of an announce node
     */
    public class AnnounceNode {
        AtomicReference<AnnounceNode> next;
        volatile OperationNode<?> o;
        final int rank;

        /**
         * Creates a new announce node
         */
        AnnounceNode(int rank) {
            this.next = new AtomicReference<>(null);
            this.rank = rank;
            this.o = null;
        }
    }

    /**
     * Definition of a linearization node
     * @param <R> The return type of the last operation used to create the linearization node
     */
    private class LinearizationNode <R> {
        final S state;
        final R result;
        final OperationNode<R> o;

        /**
         * Creates a new Linearization node by executing an operation on a state
         */
        LinearizationNode (OperationNode<R> o, S previousState) {
            this.o = o;
            this.state = previousState.copy();
            this.result = o.operation.apply(state);
        }

        /**
         * Creates a new linearization node that containes the initial state of the state machine
         * @param initialState the initial state of the state machine
         */
        LinearizationNode (S initialState) {
            this.o = new OperationNode<>();
            this.state = initialState;
            this.result = null;
        }

        /**
         * Reports the return value from the linearization node to the operation node
         * Remark: this method is necessary for type safety, as the type R might not be known when reportResult is called
         */
        void reportResult() {
            o.result = result;
            o.done = true;
        }
    }

    final AnnounceNode announces; //read only
    final AtomicReference<LinearizationNode<?>> linearization;
    final AtomicInteger sharedCounter;

    /**
     * Creates a wait-free, linearizable simulator of an automaton
     * @param initialState the initial state of the automaton
     */
    public FAACasUniversal(S initialState) {
        this.announces = new AnnounceNode(0);
        this.linearization = new AtomicReference<>(new LinearizationNode<>(initialState));
        this.sharedCounter = new AtomicInteger(1);
    }

    /**
     * Performs the given operation in a wait-free linearizable manner
     * @param operation the operation that must be performed
     * @return a result of the operation, so that it is linearizable
     */
    @Override
    public <R> R invoke(Operation<S, R> operation)  {
        OperationNode<R> newOp = new OperationNode<R>(operation);
        AnnounceNode localHead = announces;
        int rank = sharedCounter.getAndIncrement();

        /**
         * Insertion
         */
        int positionRank = localHead.rank;
        AnnounceNode positionNode = localHead;
        while (positionRank < rank) {
            if (positionNode.next.get() == null) {
                AnnounceNode nextAnnouncement = new AnnounceNode(positionRank + 1);
                positionNode.next.compareAndSet(null, nextAnnouncement);
            }
            positionRank = positionNode.next.get().rank;
            positionNode = positionNode.next.get();
        }
        positionNode.o = newOp;

        /**
         * Execution/Deletion
         */
        AtomicReference<AnnounceNode> start = new AtomicReference<>(announces);
        AnnounceNode prev = null;
        boolean isSelfDeleted = false;
        while (!isSelfDeleted) {
            AnnounceNode curr = start.get();
            isSelfDeleted = curr.rank >= rank;
            AnnounceNode next = curr.next.get();

            if (curr.o == null) {
                start.set(next);
                prev = curr;
                continue;
            }
            help(curr);
            if (next != null) {
                if (prev != null) {
                    prev.next.compareAndSet(curr, next);
                } else {
                    prev = curr;
                }
                start.set(next);
            }
        }
        return newOp.result;
    }

    /**
     * Tries to linearizes the operation stored into the operation node accessible from the announce node
     */
    private void help(AnnounceNode announce)  {
        while (true) {
            LinearizationNode<?> localLin = linearization.get();
            localLin.reportResult();
            if (announce.o.done) {
                return;
            }
            LinearizationNode<?> newLin = new LinearizationNode<>(announce.o,localLin.state);
            linearization.compareAndSet(localLin, newLin);
        }
    }

}


