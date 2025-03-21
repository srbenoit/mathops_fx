package dev.mathops.fx.coursebuilder;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.io.File;

/**
 * The content of an "Items" tab.
 */
final class ItemsTabContent extends BorderPane {

    /** The number of items. */
    private final int numItems;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code ItemsTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory
     * @param locationPrefix   the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot
     *                         and the filename of the containing directory)
     */
    ItemsTabContent(final CourseBuilder theOwner, final File theContainingDir, final String locationPrefix) {

        super();

        setPadding(AppConstants.PADDING);

        if (theContainingDir.exists() && theContainingDir.isDirectory()) {
            // Top pane shows the location of the file we're viewing (omit if the directory does not exist)
            final String locationString = locationPrefix + "." + theContainingDir.getName();
            final FlowPane top = AppUtils.makeLocationFlow(theOwner, "Location:  ", locationString, theContainingDir);
            setTop(top);
        }

        // TODO:

        this.numItems = 0;
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

    }

    /**
     * Gets the number of items found.
     *
     * @return the number of items
     */
    int getNumItems() {

        return this.numItems;
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
