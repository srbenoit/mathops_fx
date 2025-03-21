package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A pane that presents the contents of a topic module directory.
 */
class TopicModulePane extends BorderPane {

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code TopicModulePane}.
     *
     * @param owner          the owning application
     * @param topicModuleDir the topic module directory
     */
    TopicModulePane(final CourseBuilder owner, final File topicModuleDir) {

        super();

        final String locationPrefix = topicModuleDir.getParentFile().getName();
        final String topicPrefix = locationPrefix + "." + topicModuleDir.getName();
        final String locationString = locationPrefix + "." + topicModuleDir.getName();

        final TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(AppConstants.FLOATING_CLASS);
        setCenter(tabPane);

        final ObservableList<Tab> tabs = tabPane.getTabs();

        final File skillsReviewDir = new File(topicModuleDir, AppConstants.SKILLS_REVIEW_DIR);
        final File explorationsDir = new File(topicModuleDir, AppConstants.EXPLORATIONS_DIR);
        final File applicationsDir = new File(topicModuleDir, AppConstants.APPLICATIONS_DIR);
        final File handoutsDir = new File(topicModuleDir, AppConstants.HANDOUTS_DIR);

        final LessonsTabContent intro = new LessonsTabContent(owner, topicModuleDir, 1, 9, "intro");
        final SkillsReviewTabContent review = new SkillsReviewTabContent(owner, skillsReviewDir, topicPrefix);
        final List<StandardStats> standardStatsList = new ArrayList<>(3);
        final StandardsTabContent standards = new StandardsTabContent(owner, topicModuleDir, 11, 29,
                standardStatsList);
        final ExplorationsTabContent explorations = new ExplorationsTabContent(owner, explorationsDir, topicPrefix);
        final ApplicationsTabContent apps = new ApplicationsTabContent(owner, applicationsDir, topicPrefix);
        final HandoutsTabContent handouts = new HandoutsTabContent(owner, handoutsDir, topicPrefix);
        final LessonsTabContent summary = new LessonsTabContent(owner, topicModuleDir, 91, 99, "conclusion");

        intro.init();
        review.init();
        standards.init();
        explorations.init();
        apps.init();
        handouts.init();
        summary.init();

        final InformationTabContent info = new InformationTabContent(owner, topicModuleDir, "Outline.docx");
        info.init();

        final int infoWarnings = info.getNumWarnings();
        final Tab moduleInfoTab = AppUtils.makeTab("Information", info, AppConstants.INFO_ICON, infoWarnings);

        final int introWarnings = intro.getNumWarnings();
        final String introTitle = "Introduction (" + intro.getNumLessons() + ")";
        final Tab introTab = AppUtils.makeTab(introTitle, intro, AppConstants.LESSONS_ICON, introWarnings);

        final int reviewWarnings = review.getNumWarnings();
        final String reviewTitle = "Skills Review (" + review.getNumObjectives() + ")";
        final Tab reviewTab = AppUtils.makeTab(reviewTitle, review, AppConstants.SKILLS_REVIEW_ICON,
                reviewWarnings);

        final int stdsWarnings = standards.getNumWarnings();
        final String stdTitle = "Standards (" + standards.getNumStandards() + ")";
        final Tab stdTab = AppUtils.makeTab(stdTitle, standards, AppConstants.STANDARDS_ICON, stdsWarnings);

        final int expWarnings = explorations.getNumWarnings();
        final String expTitle = "Explorations (" + explorations.getNumExplorations() + ")";
        final Tab expTab = AppUtils.makeTab(expTitle, explorations, AppConstants.EXPLORATIONS_ICON, expWarnings);

        final int appWarnings = apps.getNumWarnings();
        final String appsTitle = "Applications (" + apps.getNumApplications() + ")";
        final Tab appsTab = AppUtils.makeTab(appsTitle, apps, AppConstants.APPLICATIONS_ICON, appWarnings);

        final int handoutWarnings = handouts.getNumWarnings();
        final String handoutsTitle = "Handouts (" + handouts.getNumHandouts() + ")";
        final Tab handoutsTab = AppUtils.makeTab(handoutsTitle, handouts, AppConstants.HANDOUTS_ICON,
                handoutWarnings);

        final int summaryWarnings = summary.getNumWarnings();
        final String summaryTitle = "Summary (" + summary.getNumLessons() + ")";
        final Tab summaryTab = AppUtils.makeTab(summaryTitle, summary, AppConstants.LESSONS_ICON, summaryWarnings);

        this.numWarnings = infoWarnings + introWarnings + reviewWarnings + stdsWarnings + expWarnings + appWarnings
                           + handoutWarnings + summaryWarnings;

        tabs.addAll(moduleInfoTab, introTab, reviewTab, stdTab, expTab, appsTab, handoutsTab, summaryTab);

        // Show extra files found
        final Node unexpected = buildUnexpectedFilesList(topicModuleDir);
        if (unexpected != null) {
            setBottom(unexpected);
        }

        // Build the "top" panel last since we want to display an error count
        final FlowPane row0;
        if (this.numWarnings == 0) {
            row0 = AppUtils.makeLocationFlow(owner, "Topic Module: ", locationString, topicModuleDir);
        } else {
            final Node annotation = AppUtils.makeWarningAnnotation(this.numWarnings);
            final BorderPane box = new BorderPane();
            box.setPadding(AppConstants.PADDING);
            box.setBottom(annotation);

            row0 = AppUtils.makeLocationFlow(owner, "Topic Module: ", locationString, topicModuleDir, box);
        }
        setTop(row0);
    }

    /**
     * Creates a node that displays a list of unexpected files found.
     *
     * @param topicModuleDir the topic module directory
     * @return the node (null if there were no unexpected files)
     */
    private Node buildUnexpectedFilesList(final File topicModuleDir) {

        final File[] allFiles = topicModuleDir.listFiles();
        Node result = null;

        if (allFiles != null) {
            final Collection<File> unexpected = new ArrayList<>(allFiles.length);
            for (final File file : allFiles) {
                final String name = file.getName();
                if (file.isDirectory()) {
                    if (AppConstants.SKILLS_REVIEW_DIR.equals(name) || AppConstants.EXPLORATIONS_DIR.equals(name)
                        || AppConstants.APPLICATIONS_DIR.equals(name) || AppConstants.HANDOUTS_DIR.equals(name)
                        || AppUtils.isIntroDir(name) || AppUtils.isStandardDir(name)
                        || AppUtils.isConclusionDir(name)) {
                        continue;
                    }
                } else if (name.equals(AppConstants.METADATA_FILE) || "Outline.docx".equals(name)
                           || "thumb.png".equals(name) || "thumb.jpg".equals(name) || "thumb.jpeg".equals(name)) {
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
                final VBox unexpectedBox = new VBox();
                setBottom(unexpectedBox);

                final Label heading = new Label("Unexpected Files:");
                final Font defaultFont = heading.getFont();
                final String defaultFontName = defaultFont.getName();
                final double defaultFontSize = defaultFont.getSize();
                final Font headingFont = new Font(defaultFontName, defaultFontSize * 1.1);
                heading.setFont(headingFont);

                final GridPane unexpectedGrid = new GridPane();
                unexpectedGrid.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
                unexpectedGrid.setHgap(10.0);
                unexpectedGrid.setVgap(4.0);

                final ObservableList<Node> unexpectedChildren = unexpectedGrid.getChildren();

                int unexpectedRow = 0;
                for (final File file : unexpected) {
                    final String name = file.getName();
                    AppUtils.addSimpleDocumentRow(file, name, unexpectedChildren, unexpectedRow);
                    ++unexpectedRow;
                }

                final ObservableList<Node> boxChildren = unexpectedBox.getChildren();
                boxChildren.addAll(heading, unexpectedGrid);
                result = unexpectedBox;
            }
        }

        return result;
    }

    /**
     * Called on the JavaFX
     */
    void tick() {

        // TODO:
    }
}
