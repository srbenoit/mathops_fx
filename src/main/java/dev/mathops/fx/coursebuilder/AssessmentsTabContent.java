package dev.mathops.fx.coursebuilder;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.io.File;

/**
 * The content of an "Assessments" tab.
 */
final class AssessmentsTabContent extends BorderPane {

    /** The owning application. */
    private final CourseBuilder owner;

    /** The containing directory. */
    private final File containingDir;

    /** The number of assessments found. */
    private final int numAssessments;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code AssessmentsTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory (which may not exist)
     * @param locationPrefix   the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot
     *                         and the filename of the containing directory)
     */
    AssessmentsTabContent(final CourseBuilder theOwner, final File theContainingDir, final String locationPrefix) {

        super();

        this.owner = theOwner;
        this.containingDir = theContainingDir;

        setPadding(AppConstants.PADDING);

        if (theContainingDir.exists() && theContainingDir.isDirectory()) {
            // Top pane shows the location of the file we're viewing (omit if the directory does not exist)
            final String locationString = locationPrefix + "." + theContainingDir.getName();
            final FlowPane top = AppUtils.makeLocationFlow(theOwner, "Location:  ", locationString, theContainingDir);
            setTop(top);
        }

        // TODO: scan for assessments

        this.numAssessments = 0;
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

    }

    /**
     * Gets the number of assessments found.
     *
     * @return the number of assessments
     */
    int getNumAssessments() {

        return this.numAssessments;
    }

    /**
     * Gets the number of warnings found.
     *
     * @return the number of warnings
     */
    int getNumWarnings() {

        return this.numWarnings;
    }
}
