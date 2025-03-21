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
import java.util.List;

/**
 * The content of a "Standard" tab.
 */
final class StandardTabContent extends BorderPane {

    /** The number of objectives in the standard. */
    private final int numObjectives;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code StandardTabContent}.
     *
     * @param owner          the owning application
     * @param standardDir    the standard directory
     * @param locationPrefix the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot and
     *                       the filename of the containing directory)
     * @param stats          statistics to update
     */
    StandardTabContent(final CourseBuilder owner, final File standardDir, final String locationPrefix,
                       final StandardStats stats) {

        super();

        setPadding(AppConstants.BUTTON_ROW_PADDING);

        // Top pane shows the location of the file we're viewing
        final String locationString = locationPrefix + "." + standardDir.getName();
        final FlowPane top = AppUtils.makeLocationFlow(owner, "Location:  ", locationString, standardDir);
        setTop(top);

        final TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(AppConstants.FLOATING_CLASS);
        setCenter(tabPane);

        final ObservableList<Tab> tabs = tabPane.getTabs();

        final File examplesDir = new File(standardDir, AppConstants.EXAMPLES_DIR);
        final File explorationsDir = new File(standardDir, AppConstants.EXPLORATIONS_DIR);
        final File applicationsDir = new File(standardDir, AppConstants.APPLICATIONS_DIR);
        final File handoutsDir = new File(standardDir, AppConstants.HANDOUTS_DIR);
        final File itemsDir = new File(standardDir, AppConstants.ITEMS_DIR);
        final File assessmentsDir = new File(standardDir, AppConstants.ASSESSMENTS_DIR);

        final LessonsTabContent intro = new LessonsTabContent(owner, standardDir, 1, 9, "intro");
        final int numIntroLessons = intro.getNumLessons();
        stats.numIntroLessons += numIntroLessons;

        final List<ObjectiveStats> objectiveStatsList = new ArrayList<>(10);
        final ObjectivesTabContent objectives = new ObjectivesTabContent(owner, standardDir, 11, 29, locationPrefix,
                objectiveStatsList);
        stats.numObjectives = objectiveStatsList.size();
        for (final ObjectiveStats objStats : objectiveStatsList) {
            stats.numObjectiveLessons += objStats.numLessons + objStats.numExplorationLessons
                                         + objStats.numApplicationLessons;
            stats.numObjectiveExamples += objStats.numExamples + objStats.numExplorationExamples
                                          + objStats.numApplicationExamples;
            stats.numHandouts += objStats.numHandouts;
            stats.numItems += objStats.numItems;
        }

        final ExamplesTabContent examples = new ExamplesTabContent(owner, examplesDir, locationPrefix);
        final int numExamples = examples.getNumExamples();

        final ExplorationsTabContent explorations = new ExplorationsTabContent(owner, explorationsDir, locationPrefix);
        final int numExplorations = explorations.getNumExplorations();
        stats.numExplorations += numExplorations;
        stats.numExplorationLessons = explorations.getNumLessons();
        stats.numExplorationExamples = explorations.getNumExamples();

        final ApplicationsTabContent applications = new ApplicationsTabContent(owner, applicationsDir, locationPrefix);
        final int numApplications = applications.getNumApplications();
        stats.numApplications += numApplications;
        stats.numApplicationLessons = applications.getNumLessons();
        stats.numApplicationExamples = applications.getNumExamples();

        final HandoutsTabContent handouts = new HandoutsTabContent(owner, handoutsDir, locationPrefix);
        final int numHandouts = handouts.getNumHandouts();
        stats.numHandouts += numHandouts;

        final ItemsTabContent items = new ItemsTabContent(owner, itemsDir, locationPrefix);
        final int numItems = items.getNumItems();
        stats.numItems += numItems + explorations.getNumItems() + applications.getNumItems();

        final AssessmentsTabContent assessments = new AssessmentsTabContent(owner, assessmentsDir, locationPrefix);
        final int numAssessments = assessments.getNumAssessments();
        stats.numAssessments += numAssessments;

        final LessonsTabContent summary = new LessonsTabContent(owner, standardDir, 91, 99, "conclusion");
        final int numSummaryLessons = summary.getNumLessons();
        stats.numSummaryLessons += numSummaryLessons;

        intro.init();
        objectives.init();
        examples.init();
        explorations.init();
        applications.init();
        handouts.init();
        items.init();
        assessments.init();
        summary.init();

        // Build the information pane last so it can see the populated standard statistics
        final InformationTabContent info = new InformationTabContent(owner, standardDir, null, stats);
        info.init();

        final int infoWarnings = info.getNumWarnings();
        final Tab standardInfoTab = AppUtils.makeTab("Information", info, AppConstants.INFO_ICON, infoWarnings);

        final int introWarnings = intro.getNumWarnings();
        final String introductionTitle = "Introduction (" + numIntroLessons + ")";
        final Tab introTab = AppUtils.makeTab(introductionTitle, intro, AppConstants.LESSONS_ICON, introWarnings);

        this.numObjectives = objectives.getNumObjectives();
        final int objectiveWarnings = objectives.getNumWarnings();
        final String objectivesTitle = "Objectives (" + this.numObjectives + ")";
        final Tab objectivesTab = AppUtils.makeTab(objectivesTitle, objectives, AppConstants.OBJECTIVES_ICON,
                objectiveWarnings);

        final int exampleWarnings = examples.getNumWarnings();
        final String examplesTitle = "Examples (" + numExamples + ")";
        final Tab examplesTab = AppUtils.makeTab(examplesTitle, examples, AppConstants.EXAMPLES_ICON, exampleWarnings);

        final int expWarnings = explorations.getNumWarnings();
        final String expTitle = "Explorations (" + numExplorations + ")";
        final Tab expTab = AppUtils.makeTab(expTitle, explorations, AppConstants.EXPLORATIONS_ICON, expWarnings);

        final int appWarnings = applications.getNumWarnings();
        final String appsTitle = "Applications (" + numApplications + ")";
        final Tab appsTab = AppUtils.makeTab(appsTitle, applications, AppConstants.APPLICATIONS_ICON, appWarnings);

        final int handoutWarnings = applications.getNumWarnings();
        final String handoutsTitle = "Handouts (" + numHandouts + ")";
        final Tab handoutsTab = AppUtils.makeTab(handoutsTitle, handouts, AppConstants.HANDOUTS_ICON, handoutWarnings);

        final int itemWarnings = applications.getNumWarnings();
        final String itemsTitle = "Items (" + numItems + ")";
        final Tab itemsTab = AppUtils.makeTab(itemsTitle, items, AppConstants.ITEMS_ICON, itemWarnings);

        final int assessmentWarnings = assessments.getNumWarnings();
        final String assessmentsTitle = "Assessments (" + numAssessments + ")";
        final Tab assessmentsTab = AppUtils.makeTab(assessmentsTitle, assessments, AppConstants.ASSESSMENTS_ICON,
                assessmentWarnings);

        final int summaryWarnings = assessments.getNumWarnings();
        final String summaryTitle = "Summary (" + numSummaryLessons + ")";
        final Tab summaryTab = AppUtils.makeTab(summaryTitle, summary, AppConstants.LESSONS_ICON, summaryWarnings);

        this.numWarnings = infoWarnings + introWarnings + objectiveWarnings + exampleWarnings + expWarnings
                           + appWarnings + handoutWarnings + itemWarnings + assessmentWarnings + summaryWarnings;

        tabs.addAll(standardInfoTab, introTab, objectivesTab, examplesTab, expTab, appsTab, handoutsTab, itemsTab,
                assessmentsTab, summaryTab);

        // Present any unexpected files that remain
        final Collection<File> extraFiles = new ArrayList<>(10);

        if (standardDir.exists()) {
            final File[] allFiles = standardDir.listFiles();
            if (allFiles != null) {
                for (final File file : allFiles) {
                    final String name = file.getName();
                    if (file.isDirectory()) {
                        if (AppConstants.EXAMPLES_DIR.equals(name) || AppConstants.EXPLORATIONS_DIR.equals(name)
                            || AppConstants.APPLICATIONS_DIR.equals(name) || AppConstants.HANDOUTS_DIR.equals(name)
                            || AppConstants.ITEMS_DIR.equals(name) || AppConstants.ASSESSMENTS_DIR.equals(name)
                            || AppUtils.isIntroDir(name) || AppUtils.isObjectiveDir(name)
                            || AppUtils.isConclusionDir(name)) {
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
            unexpectedPane.setHgap(10);
            unexpectedPane.setVgap(4);
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
     * Gets the number of objectives in the standard.
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
