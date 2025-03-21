package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * The content of an "Exploration" tab.
 */
final class ExplorationTabContent extends BorderPane {

    /** The number of warnings found. */
    private int numWarnings = 0;

    /** The number of lessons in the exploration. */
    private int numLessons = 0;

    /** The number of examples in the exploration. */
    private int numExamples = 0;

    /** The number of handouts in the exploration. */
    private int numHandouts = 0;

    /** The number of items in the exploration. */
    private int numItems = 0;

    /**
     * Constructs a new {@code ExplorationTabContent}.
     *
     * @param owner          the owning application
     * @param explorationDir the containing directory
     * @param locationPrefix the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot and
     *                       the filename of the containing directory)
     */
    ExplorationTabContent(final CourseBuilder owner, final File explorationDir, final String locationPrefix) {

        super();

        final TabPane tabPane = new TabPane();
        setCenter(tabPane);

        final ObservableList<Tab> tabs = tabPane.getTabs();

        final File examplesDir = new File(explorationDir, AppConstants.EXAMPLES_DIR);
        final File handoutsDir = new File(explorationDir, AppConstants.HANDOUTS_DIR);
        final File itemsDir = new File(explorationDir, AppConstants.ITEMS_DIR);

        final InformationTabContent info = new InformationTabContent(owner, explorationDir, null);

        final LessonsTabContent lessons = new LessonsTabContent(owner, explorationDir, 1, 9, "lesson");
        this.numLessons = lessons.getNumLessons();

        final ExamplesTabContent examples = new ExamplesTabContent(owner, examplesDir, locationPrefix);
        this.numExamples = examples.getNumExamples();

        final HandoutsTabContent handouts = new HandoutsTabContent(owner, handoutsDir, locationPrefix);
        this.numHandouts = handouts.getNumHandouts();

        final ItemsTabContent items = new ItemsTabContent(owner, itemsDir, locationPrefix);
        this.numItems = items.getNumItems();

        info.init();
        lessons.init();
        examples.init();
        handouts.init();
        items.init();

        // TODO: Do we want to create the Info tab last and pass it statistics to display?

        final int infoWarnings = info.getNumWarnings();
        final Tab infoTab = AppUtils.makeTab("Information", info, AppConstants.INFO_ICON, infoWarnings);

        final int lessonsWarnings = lessons.getNumWarnings();
        final String lessonsTitle = "Content Lessons (" + this.numLessons + ")";
        final Tab lessonsTab = AppUtils.makeTab(lessonsTitle, lessons, AppConstants.LESSONS_ICON, lessonsWarnings);

        final int examplesWarnings = examples.getNumWarnings();
        final String examplesTitle = "Examples (" + this.numExamples + ")";
        final Tab examplesTab = AppUtils.makeTab(examplesTitle, examples, AppConstants.EXAMPLES_ICON, examplesWarnings);

        final int handoutsWarnings = handouts.getNumWarnings();
        final String handoutsTitle = "Handouts (" + this.numHandouts + ")";
        final Tab handoutsTab = AppUtils.makeTab(handoutsTitle, handouts, AppConstants.HANDOUTS_ICON, handoutsWarnings);

        final int itemsWarnings = items.getNumWarnings();
        final String itemsTitle = "Items (" + this.numItems + ")";
        final Tab itemsTab = AppUtils.makeTab(itemsTitle, items, AppConstants.ITEMS_ICON, itemsWarnings);

        this.numWarnings = infoWarnings + lessonsWarnings + examplesWarnings + handoutsWarnings + itemsWarnings;

        tabs.addAll(infoTab, lessonsTab, examplesTab, handoutsTab, itemsTab);

        // Present any unexpected files that remain
        final Collection<File> extraFiles = new ArrayList<>(10);

        if (explorationDir.exists()) {
            final File[] allFiles = explorationDir.listFiles();
            if (allFiles != null) {
                for (final File file : allFiles) {
                    final String name = file.getName();
                    if (file.isDirectory()) {
                        if (name.contains("_lesson_") || AppConstants.EXAMPLES_DIR.equals(name) ||
                            AppConstants.HANDOUTS_DIR.equals(name) || AppConstants.ITEMS_DIR.equals(name)) {
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
