package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The content of a "Handouts" tab.
 */
final class HandoutsTabContent extends BorderPane {

    /** The owning application. */
    private final CourseBuilder owner;

    /** The containing directory. */
    private final File containingDir;

    /** The number of handouts found. */
    private final int numHandouts;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code HandoutsTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory (which may not exist)
     * @param locationPrefix   the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot
     *                         and the filename of the containing directory)
     */
    HandoutsTabContent(final CourseBuilder theOwner, final File theContainingDir, final String locationPrefix) {

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

        // TODO:

        this.numHandouts = 0;

        // Show extra files found

        final File[] allFiles = theContainingDir.listFiles();
        if (allFiles != null) {
            final Collection<File> unexpected = new ArrayList<>(allFiles.length);
            for (final File file : allFiles) {
                final String name = file.getName();
                if (file.isDirectory()) {
                    if ("10_notes".equals(name) || "20_worksheet".equals(name)
                        || "30_practice".equals(name) || "40_answers".equals(name)) {
                        continue;
                    }
                } else if (name.equals(AppConstants.METADATA_FILE)) {
                    continue;
                }

                if (file.isDirectory()) {
                    this.numWarnings += AppUtils.countFiles(file);
                } else {
                    ++this.numWarnings;
                }

                unexpected.add(file);
            }

            if (!unexpected.isEmpty()) {
                final GridPane unexpectedPane = new GridPane();
                unexpectedPane.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
                unexpectedPane.setHgap(10);
                unexpectedPane.setVgap(4);
                setBottom(unexpectedPane);

                final ObservableList<Node> unexpectedChildren = unexpectedPane.getChildren();

                int unexpectedRow = 0;
                for (final File file : unexpected) {
                    final String name = file.getName();
                    AppUtils.addSimpleDocumentRow(file, name, unexpectedChildren, unexpectedRow);
                    ++unexpectedRow;
                }
            }
        }
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

    }

    /**
     * Gets the number of handouts found.
     *
     * @return the number of handouts
     */
    int getNumHandouts() {

        return this.numHandouts;
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
