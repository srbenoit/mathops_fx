package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.Collection;

/**
 * The content of an "Objectives" tab.
 */
final class ObjectivesTabContent extends BorderPane {

    /** Characters used as suffixes for objective subdirectories. */
    private static final String SUFFIXES = "-ABCDEFGHIJKLMNOPQRST";

    /** The owning application. */
    private final CourseBuilder owner;

    /** The containing directory. */
    private final File containingDir;

    /** The number of objectives found. */
    private final int numObjectives;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code ObjectivesTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory
     * @param firstNumber      the first possible numeric prefix for lesson subdirectories
     * @param lastNumber       the last possible numeric prefix for lesson subdirectories
     * @param locationPrefix   the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot
     *                         and the filename of the containing directory)
     * @param objectiveStats   a collection ot which to add all generated objective statistics objects
     */
    ObjectivesTabContent(final CourseBuilder theOwner, final File theContainingDir, final int firstNumber,
                         final int lastNumber, final String locationPrefix,
                         final Collection<ObjectiveStats> objectiveStats) {

        super();

        this.owner = theOwner;
        this.containingDir = theContainingDir;

        setPadding(AppConstants.BUTTON_ROW_PADDING);

        // Objectives panel has no "Location" display since it always appears in some other panel that shows the
        // location (and objectives are individual directories in that location).

        // Center is a nested tab pane with all the objectives in the containing directory

        final TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(AppConstants.FLOATING_CLASS);
        setCenter(tabPane);

        final String locationString = locationPrefix + "." + theContainingDir.getName();

        final ObservableList<Tab> tabs = tabPane.getTabs();
        int index = 1;

        int numFound = 0;
        for (int i = firstNumber; i <= lastNumber; ++i) {
            final String subdirectoryName = makeSubdirectoryName(i, index);
            ++index;

            final File subdir = new File(this.containingDir, subdirectoryName);
            if (subdir.exists() && subdir.isDirectory()) {
                final ObjectiveStats stats = new ObjectiveStats();
                final ObjectiveTabContent content = new ObjectiveTabContent(theOwner, subdir, locationString, stats);
                objectiveStats.add(stats);
                final int contentWarnings = content.getNumWarnings();
                final String subdirName = subdir.getName();
                final Tab lessonTab = AppUtils.makeTab(subdirName, content, "objective.png", contentWarnings);
                tabs.add(lessonTab);
                this.numWarnings += contentWarnings;
                ++numFound;
            }
        }

        this.numObjectives = numFound;
    }

    /**
     * Generates a lesson subdirectory name, of the form "11_standard_1".
     *
     * @param prefix the prefix number (this will be represented with 2 digits)
     * @param suffix the suffix number
     * @return the subdirectory name
     */
    private static String makeSubdirectoryName(final int prefix, final int suffix) {

        final StringBuilder builder = new StringBuilder(13);

        if (prefix < 10) {
            builder.append("0");
        }
        builder.append(prefix);
        builder.append("_objective_");
        final char suffixChar = SUFFIXES.charAt(suffix);
        builder.append(suffixChar);

        return builder.toString();
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

    }

    /**
     * Gets the number of objectives found.
     *
     * @return the number of objectives
     */
    int getNumObjectives() {

        return this.numObjectives;
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
