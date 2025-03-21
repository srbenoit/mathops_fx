package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The content of an "Explorations" tab.
 */
final class ExplorationsTabContent extends BorderPane {

    /** The number of explorations. */
    private final int numExplorations;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /** The number of lessons found within explorations. */
    private int numLessons = 0;

    /** The number of examples found within explorations. */
    private int numExamples = 0;

    /** The number of handouts found within explorations. */
    private int numHandouts = 0;

    /** The number of items found within explorations. */
    private int numItems = 0;

    /**
     * Constructs a new {@code ExplorationsTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory (which may not exist)
     * @param locationPrefix   the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot
     *                         and the filename of the containing directory)
     */
    ExplorationsTabContent(final CourseBuilder theOwner, final File theContainingDir, final String locationPrefix) {

        super();

        setPadding(AppConstants.PADDING);

        final String locationString = locationPrefix + "." + theContainingDir.getName();
        if (theContainingDir.exists() && theContainingDir.isDirectory()) {
            // Top pane shows the location of the file we're viewing (omit if the directory does not exist)
            final FlowPane top = AppUtils.makeLocationFlow(theOwner, "Location:  ", locationString, theContainingDir);
            setTop(top);
        }

        // Center is a nested tab pane with all the explorations in the containing directory
        final TabPane tabPane = new TabPane();
        final ObservableList<String> tabPaneStyleClasses = tabPane.getStyleClass();
        tabPaneStyleClasses.add(AppConstants.FLOATING_CLASS);
        setCenter(tabPane);

        int numFound = 0;

        final Collection<File> extraFiles = new ArrayList<>(10);

        if (theContainingDir.exists() && theContainingDir.isDirectory()) {

            final File[] allFiles = theContainingDir.listFiles();
            if (allFiles != null) {
                Collections.addAll(extraFiles, allFiles);
            }

            final ObservableList<Tab> tabs = tabPane.getTabs();
            for (int i = 1; i <= 99; ++i) {
                final String subdirectoryName = makeSubdirectoryName(i);

                final File explorationDir = new File(theContainingDir, subdirectoryName);
                if (explorationDir.exists() && explorationDir.isDirectory()) {
                    extraFiles.remove(explorationDir);
                    final ExplorationTabContent content = new ExplorationTabContent(theOwner, explorationDir,
                            locationString);
                    this.numLessons += content.getNumLessons();
                    this.numExamples += content.getNumExamples();
                    this.numHandouts += content.getNumHandouts();
                    this.numItems += content.getNumItems();
                    content.init();
                    final int contentWarnings = content.getNumWarnings();
                    final String dirName = explorationDir.getName();
                    final Tab tab = AppUtils.makeTab(dirName, content, "exploration.png", contentWarnings);
                    tabs.add(tab);
                    this.numWarnings += contentWarnings;
                    ++numFound;
                }
            }
        }

        this.numExplorations = numFound;

        final VBox bottom = new VBox();
        final ObservableList<Node> bottomChildren = bottom.getChildren();
        setBottom(bottom);

        // Present any unexpected files that remain
        if (!extraFiles.isEmpty()) {
            final GridPane unexpectedPane = new GridPane();
            unexpectedPane.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
            unexpectedPane.setHgap(10.0);
            unexpectedPane.setVgap(4.0);
            final ObservableList<Node> unexpectedChildren = unexpectedPane.getChildren();

            int unexpectedRow = 0;
            for (final File file : extraFiles) {
                final String name = file.getName();
                AppUtils.addSimpleDocumentRow(file, name, unexpectedChildren, unexpectedRow);
                ++unexpectedRow;

                if (file.isDirectory()) {
                    this.numWarnings += AppUtils.countFiles(file);
                } else {
                    ++this.numWarnings;
                }
            }
            bottomChildren.add(unexpectedPane);
        }
    }

    /**
     * Generates an exploration subdirectory name, of the form "01".
     *
     * @param index the index (where 1 is the first exploration)
     * @return the subdirectory name
     */
    private static String makeSubdirectoryName(final int index) {

        final StringBuilder builder = new StringBuilder(13);

        if (index < 10) {
            builder.append("0");
        }
        builder.append(index);

        return builder.toString();
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

    }

    /**
     * Gets the number of explorations found.
     *
     * @return the number of explorations
     */
    int getNumExplorations() {

        return this.numExplorations;
    }

    /**
     * Gets the number of warnings found.
     *
     * @return the number of warnings
     */
    int getNumWarnings() {

        return this.numWarnings;
    }

    /**
     * Gets the number of lessons found.
     *
     * @return the number of lessons
     */
    int getNumLessons() {

        return this.numLessons;
    }

    /**
     * Gets the number of examples found.
     *
     * @return the number of examples
     */
    int getNumExamples() {

        return this.numExamples;
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
     * Gets the number of items found.
     *
     * @return the number of items
     */
    int getNumItems() {

        return this.numItems;
    }
}
