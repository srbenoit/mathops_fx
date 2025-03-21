package dev.mathops.fx.coursebuilder;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

/**
 * A scheduled service that creates a ticker tasks and repeats its invocation once per second.
 */
class TickerService extends ScheduledService<Integer> {

    /** The owning course builder. */
    private final CourseBuilder owner;

    /**
     * Constructs a new {@code TickerService}
     *
     * @param theOwner the owning course builder
     */
    TickerService(final CourseBuilder theOwner) {

        super();

        this.owner = theOwner;

        final Duration oneSecond = Duration.seconds(1.0);
        setPeriod(oneSecond);
    }

    /**
     * Creates a ticker task.
     *
     * @return the task
     */
    protected Task<Integer> createTask() {

        return new TickerTask(this.owner);
    }
}
