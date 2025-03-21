package dev.mathops.fx.coursebuilder;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The content of an "Examples" tab.
 */
final class ExamplesTabContent extends BorderPane {

    /** The number of examples. */
    private final int numExamples;

    /**
     * The number of warnings found (unexpected or missing files), including all warnings found by included example
     * tabs.
     */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code ExamplesTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory
     * @param locationPrefix   the prefix for the location, like "02_subject.01_topic" (to this will be appended a dot
     *                         and the filename of the containing directory)
     */
    ExamplesTabContent(final CourseBuilder theOwner, final File theContainingDir, final String locationPrefix) {

        super();

        setPadding(AppConstants.PADDING);

        final String locationString = locationPrefix + "." + theContainingDir.getName();

        // Top pane shows the location of the file we're viewing
        if (theContainingDir.exists() && theContainingDir.isDirectory()) {
            final FlowPane top = AppUtils.makeLocationFlow(theOwner, "Location:  ", locationString, theContainingDir);
            setTop(top);
        }

        // Center is either a message indicating there are no examples, or a tabbed-pane of the examples
        final File[] list = theContainingDir.listFiles();
        final Collection<File> unexpected = new ArrayList<>(10);
        int numFound = 0;
        if (list != null) {
            for (final File file : list) {
                if (file.isDirectory()) {
                    ++numFound;
                } else {
                    unexpected.add(file);
                }
            }
        }

        this.numExamples = numFound;

        if (numFound == 0) {
            final VBox center = new VBox();
            setCenter(center);
            final Label noExamples = new Label("There are no examples.");
            center.getChildren().add(noExamples);
        } else {
            final TabPane tabPane = new TabPane();
            tabPane.getStyleClass().add(AppConstants.FLOATING_CLASS);
            setCenter(tabPane);

            final List<Tab> children = tabPane.getTabs();

            for (final File file : list) {
                if (file.isDirectory()) {
                    final String filename = file.getName();
                    final ExampleTabContent example = new ExampleTabContent(theOwner, file, locationString);
                    example.init();
                    final int exampleWarnings = example.getNumWarnings();
                    final Tab tab = AppUtils.makeTab(filename, example, null, exampleWarnings);
                    children.add(tab);
                    this.numWarnings += exampleWarnings;
                }
            }
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
                addDocumentRow(file, name, unexpectedChildren, unexpectedRow);
                ++unexpectedRow;

                if (file.isDirectory()) {
                    this.numWarnings += AppUtils.countFiles(file);
                } else {
                    ++this.numWarnings;
                }
            }
        }
    }

    /**
     * Adds a row of controls for a single document to a grid pane.
     *
     * @param file      the file
     * @param labelText the label text
     * @param nodes     the node list to which to add controls
     * @param row       the row to which to assign controls
     */
    private static void addDocumentRow(final File file, final String labelText,
                                       final Collection<? super Node> nodes, final int row) {

        final ImageView icon = AppUtils.loadIcon("unexpected24.png");
        if (icon != null) {
            GridPane.setConstraints(icon, 0, row);
            nodes.add(icon);
        }

        final Hyperlink label = new Hyperlink(labelText);
        if (label.getPrefWidth() < 160.0) {
            label.setPrefWidth(160.0);
        }
        label.setUserData(file);
        label.setTextFill(AppConstants.RED);
        GridPane.setConstraints(label, 1, row);
        nodes.add(label);
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

    }

    /**
     * Gets the number of examples.
     *
     * @return the number of examples
     */
    int getNumExamples() {

        return this.numExamples;
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
