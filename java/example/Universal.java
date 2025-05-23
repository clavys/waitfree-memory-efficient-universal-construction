package example;

/**
 * Universal constructions should implement this interface
 * @param <S> The abstract state of the object implemented by an instance of the universal construction
 */
public interface Universal<S extends Universal.State<S>> {
    /**
     * Performs the given operation in a linearizable manner
     * @param operation the operation that must be performed
     * @return a result of the operation, so that it is linearizable
     */
    public <R> R invoke(Operation<S, R> operation);

    /**
     * Interface that represents an operation that must be executed by the universal construction
     *
     * @param <S> Type of the state of the object on which is executed the operation
     * @param <R> Return type of the operation
     */
    public static interface Operation<S extends State<S>, R> {
        /**
         * Execute the operation on a given state
         * @param state State on which the operation will be executed
         * @return some value defined by the implementation
         */
        public R apply(S state);
    }

    public interface State<S> {
        /**
         * Copies the state
         * @return a new copy of the state
         */
        public S copy();
    }


}

