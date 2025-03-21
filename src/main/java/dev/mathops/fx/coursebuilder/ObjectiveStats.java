package dev.mathops.fx.coursebuilder;

/**
 * A container for statistics for single objective.
 */
class ObjectiveStats {

    /** The number of lessons. */
    int numLessons = 0;

    /** The number of examples. */
    int numExamples = 0;

    /** The number of explorations. */
    int numExplorations = 0;

    /** The number of lessons within explorations. */
    int numExplorationLessons = 0;

    /** The number of examples within explorations. */
    int numExplorationExamples = 0;

    /** The number of applications. */
    int numApplications = 0;

    /** The number of lessons within applications. */
    int numApplicationLessons = 0;

    /** The number of examples within applications. */
    int numApplicationExamples = 0;

    /** The number of handouts. */
    int numHandouts = 0;

    /** The number of items (including explorations and applications). */
    int numItems = 0;

    /**
     * Constructs a new {@code ObjectiveStats}.
     */
    ObjectiveStats() {

        // No action
    }
}
