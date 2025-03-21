package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.File;

/**
 * The content of a "Lessons" tab.  This panel presents the list of lessons available within a given directory that
 * match some filename pattern.
 */
final class LessonsTabContent extends BorderPane {

    /** The containing directory. */
    private final File containingDir;

    /** The number of lessons found. */
    private final int numLessons;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code LessonsTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory
     * @param firstNumber      the first possible numeric prefix for lesson subdirectories
     * @param lastNumber       the last possible numeric prefix for lesson subdirectories
     * @param label            the label for lesson subdirectories (like "intro", "lesson", "conclusion", etc.)
     */
    LessonsTabContent(final CourseBuilder theOwner, final File theContainingDir, final int firstNumber,
                      final int lastNumber, final String label) {

        super();

        this.containingDir = theContainingDir;

        setPadding(AppConstants.PADDING);

        // Center is a nested tab pane with all the lessons in the containing directory

        final TabPane lessonTabs = createLessonTabs();

        int numFound = 0;
        final ObservableList<Tab> tabs = lessonTabs.getTabs();
        int index = 1;
        for (int i = firstNumber; i <= lastNumber; ++i) {
            final String subdirectoryName = makeSubdirectoryName(i, label, index);
            ++index;

            final File subdir = new File(this.containingDir, subdirectoryName);
            if (subdir.exists() && subdir.isDirectory()) {
                final LessonTabContent content = new LessonTabContent(theOwner, subdir);
                content.init();
                final String subdirName = subdir.getName();
                final int contentWarnings = content.getNumWarnings();
                final Tab lessonTab = AppUtils.makeTab(subdirName, content, "lesson.png", contentWarnings);
                this.numWarnings += contentWarnings;
                tabs.add(lessonTab);
                ++numFound;
            }
        }

        if (numFound == 0) {
            final VBox centerBox = new VBox();
            centerBox.setPadding(AppConstants.PADDING);
            final Label lbl = new Label("(No lesson directories found)");
            centerBox.getChildren().add(lbl);
            setCenter(centerBox);
        } else {
            setCenter(lessonTabs);
        }

        this.numLessons = numFound;
    }

    /**
     * Creates the nested tab pane that will hold one tab for each lesson.
     *
     * @return the tab pane
     */
    private static TabPane createLessonTabs() {

        final TabPane lessonTabs = new TabPane();

        final Border border = AppUtils.makeStrokeBorder();
        lessonTabs.setBorder(border);

        return lessonTabs;
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

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
     * Gets the number of warnings found.
     *
     * @return the number of warnings
     */
    int getNumWarnings() {

        return this.numWarnings;
    }

    /**
     * Generates a lesson subdirectory name, of the form "21_label_11".
     *
     * @param prefix the prefix number (this will be represented with 2 digits)
     * @param label  the label
     * @param suffix the suffix number
     * @return the subdirectory name
     */
    private static String makeSubdirectoryName(final int prefix, final String label, final int suffix) {

        final int labelLen = label.length();
        final StringBuilder builder = new StringBuilder(labelLen + 5);

        if (prefix < 10) {
            builder.append("0");
        }
        builder.append(prefix);
        builder.append("_");
        builder.append(label);
        builder.append("_");
        builder.append(suffix);

        return builder.toString();
    }
}
