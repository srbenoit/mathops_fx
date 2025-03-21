package dev.mathops.fx.coursebuilder;

import javafx.concurrent.Task;

/**
 * A task that invokes a "tick" on its owner.
 */
class TickerTask extends Task<Integer> {

    /** The owning course builder. */
    private final CourseBuilder owner;

    /**
     * Constructs a new {@code TickerTask}
     *
     * @param theOwner the owning course builder
     */
    TickerTask(final CourseBuilder theOwner) {

        super();

        this.owner = theOwner;
    }

    /**
     * Invokes the task.
     *
     * @return the tick count
     */
    @Override
    protected Integer call() {

        this.owner.tick();

        return Integer.valueOf(0);
    }
}
