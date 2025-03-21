package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The content of a "Skills Review" tab.
 */
final class SkillsReviewTabContent extends BorderPane {

    /** The number of objectives found. */
    private final int numObjectives;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code SkillsReviewTabContent}.
     *
     * @param owner           the owning application
     * @param skillsReviewDir the skills review directory
     * @param locationPrefix  the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot
     *                        and the filename of the containing directory)
     */
    SkillsReviewTabContent(final CourseBuilder owner, final File skillsReviewDir, final String locationPrefix) {

        super();

        setPadding(AppConstants.PADDING);

        // Top pane shows the location of the file we're viewing
        final String locationString = locationPrefix + "." + skillsReviewDir.getName();
        final FlowPane top = AppUtils.makeLocationFlow(owner, "Location:  ", locationString, skillsReviewDir);
        setTop(top);

        final TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(AppConstants.FLOATING_CLASS);
        setCenter(tabPane);

        final ObservableList<Tab> tabs = tabPane.getTabs();

        final File handoutsDir = new File(skillsReviewDir, AppConstants.HANDOUTS_DIR);
        final File assessmentsDir = new File(skillsReviewDir, AppConstants.ASSESSMENTS_DIR);

        final LessonsTabContent intro = new LessonsTabContent(owner, skillsReviewDir, 1, 9, "intro");
        final List<ObjectiveStats> objectiveStatsList = new ArrayList<>(10);
        final ObjectivesTabContent objectives = new ObjectivesTabContent(owner, skillsReviewDir, 11, 29,
                locationPrefix, objectiveStatsList);
        final HandoutsTabContent handouts = new HandoutsTabContent(owner, handoutsDir, locationPrefix);
        final AssessmentsTabContent assessments = new AssessmentsTabContent(owner, assessmentsDir, locationPrefix);

        intro.init();
        objectives.init();
        handouts.init();
        assessments.init();

        this.numObjectives = objectives.getNumObjectives();

        final int introWarnings = intro.getNumWarnings();
        final String introTitle = "Introduction (" + intro.getNumLessons() + ")";
        final Tab introTab = AppUtils.makeTab(introTitle, intro, AppConstants.LESSONS_ICON, introWarnings);

        final int objWarnings = objectives.getNumWarnings();
        final String objTitle = "Objectives (" + objectives.getNumObjectives() + ")";
        final Tab objectivesTab = AppUtils.makeTab(objTitle, objectives, AppConstants.OBJECTIVES_ICON, objWarnings);

        final int handoutsWarnings = handouts.getNumWarnings();
        final String handoutsTitle = "Handouts (" + handouts.getNumHandouts() + ")";
        final Tab handoutsTab = AppUtils.makeTab(handoutsTitle, handouts, AppConstants.HANDOUTS_ICON, handoutsWarnings);

        final int assessmentWarnings = assessments.getNumWarnings();
        final String assessmentsTitle = "Assessments (" + assessments.getNumAssessments() + ")";
        final Tab assessmentsTab = AppUtils.makeTab(assessmentsTitle, assessments, AppConstants.ASSESSMENTS_ICON,
                assessmentWarnings);

        this.numWarnings = introWarnings + objWarnings + handoutsWarnings + assessmentWarnings;

        tabs.addAll(introTab, objectivesTab, handoutsTab, assessmentsTab);

        // Show extra files found

        final File[] allFiles = skillsReviewDir.listFiles();
        if (allFiles != null) {
            final Collection<File> unexpected = new ArrayList<>(allFiles.length);
            for (final File file : allFiles) {
                final String name = file.getName();
                if (file.isDirectory()) {
                    if (AppConstants.HANDOUTS_DIR.equals(name) || AppConstants.ASSESSMENTS_DIR.equals(name)
                        || AppUtils.isIntroDir(name) || AppUtils.isObjectiveDir(name)) {
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
