package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.Collection;

/**
 * The content of a "Standards" tab.
 */
final class StandardsTabContent extends BorderPane {

    /** The number of standards found. */
    private final int numStandards;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code StandardsTabContent}.
     *
     * @param theOwner          the owning application
     * @param theTopicModuleDir the topic module directory
     * @param firstNumber       the first possible numeric prefix for lesson subdirectories
     * @param lastNumber        the last possible numeric prefix for lesson subdirectories
     * @param standardStats     a collection ot which to add all generated standard statistics objects
     */
    StandardsTabContent(final CourseBuilder theOwner, final File theTopicModuleDir, final int firstNumber,
                        final int lastNumber, final Collection<StandardStats> standardStats) {

        super();

        setPadding(AppConstants.BUTTON_ROW_PADDING);

        // Center is a nested tab pane with all the lessons in the containing directory
        final TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(AppConstants.FLOATING_CLASS);
        setCenter(tabPane);

        final ObservableList<Tab> tabs = tabPane.getTabs();
        int index = 1;
        int numFound = 0;
        for (int i = firstNumber; i <= lastNumber; ++i) {
            final String subdirectoryName = makeSubdirectoryName(i, index);

            final File stsandardDir = new File(theTopicModuleDir, subdirectoryName);
            if (stsandardDir.exists() && stsandardDir.isDirectory()) {
                final String locationString =
                        theTopicModuleDir.getParentFile().getName() + "." + theTopicModuleDir.getName();
                final StandardStats stats = new StandardStats();
                final StandardTabContent content = new StandardTabContent(theOwner, stsandardDir, locationString,
                        stats);
                standardStats.add(stats);
                final int contentWarnings = content.getNumWarnings();
                final String dirName = stsandardDir.getName();
                final Tab lessonTab = AppUtils.makeTab(dirName, content, "standard.png", contentWarnings);
                tabs.add(lessonTab);
                this.numWarnings += contentWarnings;
                ++numFound;
            }

            ++index;
        }

        this.numStandards = numFound;
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
        builder.append("_standard_");
        builder.append(suffix);

        return builder.toString();
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

    }

    /**
     * Gets the number of standards found.
     *
     * @return the number of standards
     */
    int getNumStandards() {

        return this.numStandards;
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
