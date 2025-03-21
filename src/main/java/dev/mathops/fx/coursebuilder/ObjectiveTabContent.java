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

/**
 * The content of an "Objective" tab.
 */
final class ObjectiveTabContent extends BorderPane {

    /** The number of warnings found. */
    private int numWarnings;

    /**
     * Constructs a new {@code ObjectiveTabContent}.
     *
     * @param owner          the owning application
     * @param objectiveDir   the containing directory
     * @param locationPrefix the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot and
     *                       the filename of the containing directory)
     * @param stats          statistics to update
     */
    ObjectiveTabContent(final CourseBuilder owner, final File objectiveDir, final String locationPrefix,
                        final ObjectiveStats stats) {

        super();

        setPadding(AppConstants.BUTTON_ROW_PADDING);

        // Top pane shows the location of the file we're viewing
        final String locationString = locationPrefix + "." + objectiveDir.getName();
        final FlowPane top = AppUtils.makeLocationFlow(owner, "Location:  ", locationString, objectiveDir);
        setTop(top);

        final TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(AppConstants.FLOATING_CLASS);
        setCenter(tabPane);

        final ObservableList<Tab> tabs = tabPane.getTabs();

        final File examplesDir = new File(objectiveDir, AppConstants.EXAMPLES_DIR);
        final File explorationsDir = new File(objectiveDir, AppConstants.EXPLORATIONS_DIR);
        final File applicationsDir = new File(objectiveDir, AppConstants.APPLICATIONS_DIR);
        final File handoutsDir = new File(objectiveDir, AppConstants.HANDOUTS_DIR);
        final File itemsDir = new File(objectiveDir, AppConstants.ITEMS_DIR);

        final LessonsTabContent lessons = new LessonsTabContent(owner, objectiveDir, 11, 19, "lesson");
        final int numLessons = lessons.getNumLessons();
        stats.numLessons += numLessons;

        final ExamplesTabContent examples = new ExamplesTabContent(owner, examplesDir, locationString);
        final int numExamples = examples.getNumExamples();
        stats.numExamples += examples.getNumExamples();

        final ExplorationsTabContent explorations = new ExplorationsTabContent(owner, explorationsDir, locationString);
        final int numExplorations = explorations.getNumExplorations();
        stats.numExplorations += numExplorations;
        stats.numExplorationLessons += explorations.getNumLessons();
        stats.numExplorationExamples += explorations.getNumExamples();
        stats.numHandouts += explorations.getNumHandouts();
        stats.numItems += explorations.getNumItems();

        final ApplicationsTabContent applications = new ApplicationsTabContent(owner, applicationsDir, locationString);
        final int numApplications = applications.getNumApplications();
        stats.numApplications += numApplications;
        stats.numApplicationLessons += applications.getNumLessons();
        stats.numApplicationExamples += applications.getNumExamples();
        stats.numHandouts += applications.getNumHandouts();
        stats.numItems += applications.getNumItems();

        final HandoutsTabContent handouts = new HandoutsTabContent(owner, handoutsDir, locationString);
        final int numHandouts = handouts.getNumHandouts();
        stats.numHandouts += numHandouts;

        final ItemsTabContent items = new ItemsTabContent(owner, itemsDir, locationString);
        final int numItems = items.getNumItems();
        stats.numItems += numItems;

        lessons.init();
        examples.init();
        explorations.init();
        applications.init();
        handouts.init();
        items.init();

        // Build the information pane last so it can see the populated objective statistics
        final InformationTabContent info = new InformationTabContent(owner, objectiveDir, null, stats);
        info.init();

        final int infoWarnings = info.getNumWarnings();
        final Tab objectiveInfoTab = AppUtils.makeTab("Information", info, AppConstants.INFO_ICON, infoWarnings);

        final int lessonsWarnings = lessons.getNumWarnings();
        final String lessonsTitle = "Content Lessons (" + numLessons + ")";
        final Tab introTab = AppUtils.makeTab(lessonsTitle, lessons, AppConstants.LESSONS_ICON, lessonsWarnings);

        final int exampleWarnings = examples.getNumWarnings();
        final String examplesTitle = "Examples (" + numExamples + ")";
        final Tab examplesTab = AppUtils.makeTab(examplesTitle, examples, AppConstants.EXAMPLES_ICON, exampleWarnings);

        final int expWarnings = explorations.getNumWarnings();
        final String expTitle = "Explorations (" + numExplorations + ")";
        final Tab expTab = AppUtils.makeTab(expTitle, explorations, AppConstants.EXPLORATIONS_ICON, expWarnings);

        final int appsWarnings = applications.getNumWarnings();
        final String appsTitle = "Applications (" + numApplications + ")";
        final Tab appsTab = AppUtils.makeTab(appsTitle, applications, AppConstants.APPLICATIONS_ICON, appsWarnings);

        final int handoutsWarnings = handouts.getNumWarnings();
        final String handoutsTitle = "Handouts (" + numHandouts + ")";
        final Tab handoutsTab = AppUtils.makeTab(handoutsTitle, handouts, AppConstants.HANDOUTS_ICON, handoutsWarnings);

        final int itemsWarnings = items.getNumWarnings();
        final String itemsTitle = "Items (" + numItems + ")";
        final Tab itemsTab = AppUtils.makeTab(itemsTitle, items, AppConstants.ITEMS_ICON, itemsWarnings);

        this.numWarnings = infoWarnings + lessonsWarnings + exampleWarnings + expWarnings + appsWarnings
                           + handoutsWarnings + itemsWarnings;

        tabs.addAll(objectiveInfoTab, introTab, examplesTab, expTab, appsTab, handoutsTab, itemsTab);

        // Present any unexpected files that remain
        final Collection<File> extraFiles = new ArrayList<>(10);

        if (objectiveDir.exists()) {
            final File[] allFiles = objectiveDir.listFiles();
            if (allFiles != null) {
                for (final File file : allFiles) {
                    final String name = file.getName();
                    if (file.isDirectory()) {
                        if (AppConstants.EXAMPLES_DIR.equals(name) || AppConstants.EXPLORATIONS_DIR.equals(name)
                            || AppConstants.APPLICATIONS_DIR.equals(name) || AppConstants.HANDOUTS_DIR.equals(name)
                            || AppConstants.ITEMS_DIR.equals(name) || AppUtils.isLessonDir(name)) {
                            continue;
                        }
                    } else if (AppConstants.METADATA_FILE.equals(name)) {
                        continue;
                    }

                    if (file.isDirectory()) {
                        this.numWarnings += AppUtils.countFiles(file);
                    } else {
                        ++this.numWarnings;
                    }

                    extraFiles.add(file);
                }
            }
        }

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
            }
            bottomChildren.add(unexpectedPane);
        }
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

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
