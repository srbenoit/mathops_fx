package dev.mathops.fx.coursebuilder;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The content of an "Information" tab.  This panel shows information from the "metadata.json" file, and provides
 * buttons to open the enclosing directory and (optionally) an Outline document.
 * <p>
 * TODO: Allow for this information to be updated from here.
 */
final class InformationTabContent extends VBox {

    /** The width for aligned labels. */
    private static final double LABEL_WIDTH = 90.0;

    /** The width for aligned labels. */
    private static final double LABEL_WIDTH2 = 180.0;

    /** The width for title and author fields. */
    private static final double FIELD_WIDTH = 350.0;

    /** The file containing metadata. */
    private final File jsonFile;

    /** The file containing the outline. */
    private File outlineFile = null;

    /** A field in which to edit the title. */
    private final TextField title;

    /** A field in which to edit the list of authors. */
    private final TextField authors;

    /** A field in which to edit the description. */
    private final TextArea description;

    /** A field in which to edit the understanding goals. */
    private final TextArea goals;

    /** The number of warnings found. */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code InformationTabContent}.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory
     * @param outlineFilename  the filename of the outline file (null if none)
     */
    InformationTabContent(final CourseBuilder theOwner, final File theContainingDir, final String outlineFilename) {

        super(AppConstants.V_PAD);

        this.jsonFile = new File(theContainingDir, AppConstants.METADATA_FILE);

        final boolean hasOutline = outlineFilename != null;
        if (hasOutline) {
            this.outlineFile = new File(theContainingDir, outlineFilename);
        }

        setPadding(AppConstants.BUTTON_ROW_PADDING);

        final Label[] labels = {new Label("Title:  "), new Label("Author(s):  "), new Label("Description:  "),
                new Label("Goals:  "), new Label("Outline:  "), new Label("Thumbnail:  ")};
        for (final Label lbl : labels) {
            lbl.setPrefWidth(LABEL_WIDTH);
            lbl.setAlignment(Pos.BASELINE_RIGHT);
        }

        this.title = new TextField();
        this.title.setPrefWidth(FIELD_WIDTH);

        this.authors = new TextField();
        this.authors.setPrefWidth(FIELD_WIDTH);

        // Compute width after labels and insets for the text areas
        final ReadOnlyDoubleProperty myWidth = widthProperty();
        final DoubleBinding remainingWidth = Bindings.subtract(myWidth, LABEL_WIDTH + AppConstants.V_GAP);

        this.description = new TextArea();
        this.description.setPrefRowCount(5);
        final DoubleProperty descriptionPrefWidth = this.description.prefWidthProperty();
        descriptionPrefWidth.bind(remainingWidth);
        this.description.setWrapText(true);

        this.goals = new TextArea();
        this.goals.setPrefRowCount(5);
        final DoubleProperty goalsPerfWidth = this.goals.prefWidthProperty();
        goalsPerfWidth.bind(remainingWidth);
        this.goals.setWrapText(true);

        final FlowPane row1 = new FlowPane();
        final ObservableList<Node> row1Children = row1.getChildren();
        row1Children.addAll(labels[0], this.title);

        final FlowPane row2 = new FlowPane();
        final ObservableList<Node> row2Children = row2.getChildren();
        row2Children.addAll(labels[1], this.authors);

        final FlowPane row3 = new FlowPane();
        row3.setRowValignment(VPos.BASELINE);
        final ObservableList<Node> row3Children = row3.getChildren();
        row3Children.addAll(labels[2], this.description);

        final FlowPane row4 = new FlowPane();
        row4.setRowValignment(VPos.BASELINE);
        final ObservableList<Node> row4Children = row4.getChildren();
        row4Children.addAll(labels[3], this.goals);

        final ObservableList<Node> children = getChildren();
        children.addAll(row1, row2, row3, row4);

        if (this.outlineFile != null) {
            if (this.outlineFile.exists()) {
                final FlowPane row5 = AppUtils.makeLocationFlow(theOwner, "Outline:  ",
                        outlineFilename + " (open in Word)",
                        this.outlineFile);
                children.add(row5);
            } else {
                final FlowPane row5 = new FlowPane();
                row5.setRowValignment(VPos.BASELINE);
                final ObservableList<Node> row5Children = row5.getChildren();
                final Label lbl = new Label(outlineFilename + " (file not found)");
                row5Children.addAll(labels[4], lbl);
                children.add(row5);
            }
        }

        final File thumbPng = new File(theContainingDir, "thumb.png");
        final File thumbJpg = new File(theContainingDir, "thumb.jpg");
        final File thumbJpeg = new File(theContainingDir, "thumb.jpeg");
        final File thumb;
        if (thumbPng.exists()) {
            thumb = thumbPng;
        } else if (thumbJpg.exists()) {
            thumb = thumbJpg;
        } else if (thumbJpeg.exists()) {
            thumb = thumbJpeg;
        } else {
            thumb = null;
        }

        if (thumb != null) {
            final FlowPane row6 = new FlowPane();
            final ObservableList<Node> row6Children = row6.getChildren();
            final String filename = thumb.getName();

            final BufferedImage img = FileLoader.loadFileAsImage(thumb, false);
            if (img == null) {
                final Label filenameLabel = new Label(filename + " (unable to load)");
                row6Children.addAll(labels[5], filenameLabel);
            } else {
                final int w = img.getWidth();
                final int h = img.getHeight();
                final WritableImage writable = new WritableImage(w, h);
                final WritableImage fxImage = SwingFXUtils.toFXImage(img, writable);
                final ImageView view = new ImageView(fxImage);

                final Label filenameLabel = new Label(filename + "  ");
                row6Children.addAll(labels[5], filenameLabel, view);
                children.add(row6);
            }
        }
    }

    /**
     * Constructs a new {@code InformationTabContent} that includes Objective statistics.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory
     * @param outlineFilename  the filename of the outline file (null if none)
     * @param stats            statistics to display (null if none)
     */
    InformationTabContent(final CourseBuilder theOwner, final File theContainingDir, final String outlineFilename,
                          final ObjectiveStats stats) {

        this(theOwner, theContainingDir, outlineFilename);

        final Label[] labels = {new Label("Number of lessons:  "), new Label("Number of Examples:  "),
                new Label("Number of Explorations:  "), new Label("Lessons in Explorations:  "),
                new Label("Examples in Explorations:  "), new Label("Number of Applications:  "),
                new Label("Lessons in Applications:  "), new Label("Examples in Applications:  "),
                new Label("Number of Handouts:  "), new Label("Number of Items:  ")};
        for (final Label lbl : labels) {
            lbl.setPrefWidth(LABEL_WIDTH2);
            lbl.setAlignment(Pos.BASELINE_RIGHT);
        }

        final FlowPane row1 = new FlowPane();
        final ObservableList<Node> row1Children = row1.getChildren();
        final String numLessonsStr = Integer.toString(stats.numLessons);
        final Label label1 = new Label(numLessonsStr);
        row1Children.addAll(labels[0], label1);

        final FlowPane row2 = new FlowPane();
        final ObservableList<Node> row2Children = row2.getChildren();
        final String numExamplesStr = Integer.toString(stats.numExamples);
        final Label label2 = new Label(numExamplesStr);
        row2Children.addAll(labels[1], label2);

        final FlowPane row3 = new FlowPane();
        final ObservableList<Node> row3Children = row3.getChildren();
        final String numExplorationsStr = Integer.toString(stats.numExplorations);
        final Label label3 = new Label(numExplorationsStr);
        row3Children.addAll(labels[2], label3);

        final FlowPane row4 = new FlowPane();
        final ObservableList<Node> row4Children = row4.getChildren();
        final String numExplorationLessonsStr = Integer.toString(stats.numExplorationLessons);
        final Label label4 = new Label(numExplorationLessonsStr);
        row4Children.addAll(labels[3], label4);

        final FlowPane row5 = new FlowPane();
        final ObservableList<Node> row5Children = row5.getChildren();
        final String numExplorationExamplesStr = Integer.toString(stats.numExplorationExamples);
        final Label label5 = new Label(numExplorationExamplesStr);
        row5Children.addAll(labels[4], label5);

        final FlowPane row6 = new FlowPane();
        final ObservableList<Node> row6Children = row6.getChildren();
        final String numApplicationsStr = Integer.toString(stats.numApplications);
        final Label label6 = new Label(numApplicationsStr);
        row6Children.addAll(labels[5], label6);

        final FlowPane row7 = new FlowPane();
        final ObservableList<Node> row7Children = row7.getChildren();
        final String numApplicationLessonsStr = Integer.toString(stats.numApplicationLessons);
        final Label label7 = new Label(numApplicationLessonsStr);
        row7Children.addAll(labels[6], label7);

        final FlowPane row8 = new FlowPane();
        final ObservableList<Node> row8Children = row8.getChildren();
        final String numApplicationExamplesStr = Integer.toString(stats.numApplicationExamples);
        final Label label8 = new Label(numApplicationExamplesStr);
        row8Children.addAll(labels[7], label8);

        final FlowPane row9 = new FlowPane();
        final ObservableList<Node> row9Children = row9.getChildren();
        final String numHandoutsStr = Integer.toString(stats.numHandouts);
        final Label label9 = new Label(numHandoutsStr);
        row9Children.addAll(labels[8], label9);

        final FlowPane row10 = new FlowPane();
        final ObservableList<Node> row10Children = row10.getChildren();
        final String numItemsStr = Integer.toString(stats.numItems);
        final Label label10 = new Label(numItemsStr);
        row10Children.addAll(labels[9], label10);

        final ObservableList<Node> children = getChildren();
        children.addAll(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10);
    }

    /**
     * Constructs a new {@code InformationTabContent} that includes Standard statistics.
     *
     * @param theOwner         the owning application
     * @param theContainingDir the containing directory
     * @param outlineFilename  the filename of the outline file (null if none)
     * @param stats            statistics to display (null if none)
     */
    InformationTabContent(final CourseBuilder theOwner, final File theContainingDir, final String outlineFilename,
                          final StandardStats stats) {

        this(theOwner, theContainingDir, outlineFilename);

        final Label[] labels = {new Label("Number of Intro Lessons:  "), new Label("Number of Summary Lessons:  "),
                new Label("Number of Objectives:  "), new Label("Lessons in Objectives:  "),
                new Label("Examples in Objectives:  "), new Label("Number of Examples:  "),
                new Label("Number of Explorations:  "), new Label("Lessons in Explorations:  "),
                new Label("Examples in Explorations:  "), new Label("Number of Applications:  "),
                new Label("Lessons in Applications:  "), new Label("Examples in Applications:  "),
                new Label("Number of Handouts:  "), new Label("Number of Items:  "),
                new Label("Number of Assessments:  ")};
        for (final Label lbl : labels) {
            lbl.setPrefWidth(LABEL_WIDTH2);
            lbl.setAlignment(Pos.BASELINE_RIGHT);
        }

        final FlowPane row1 = new FlowPane();
        final ObservableList<Node> row1Children = row1.getChildren();
        final String numIntroLessonsStr = Integer.toString(stats.numIntroLessons);
        final Label label1 = new Label(numIntroLessonsStr);
        row1Children.addAll(labels[0], label1);

        final FlowPane row2 = new FlowPane();
        final ObservableList<Node> row2Children = row2.getChildren();
        final String numSummaryLessonsStr = Integer.toString(stats.numSummaryLessons);
        final Label label2 = new Label(numSummaryLessonsStr);
        row2Children.addAll(labels[1], label2);

        final FlowPane row3 = new FlowPane();
        final ObservableList<Node> row3Children = row3.getChildren();
        final String numObjectivesStr = Integer.toString(stats.numObjectives);
        final Label label3 = new Label(numObjectivesStr);
        row3Children.addAll(labels[2], label3);

        final FlowPane row4 = new FlowPane();
        final ObservableList<Node> row4Children = row4.getChildren();
        final String numObjectiveLessonsStr = Integer.toString(stats.numObjectiveLessons);
        final Label label4 = new Label(numObjectiveLessonsStr);
        row4Children.addAll(labels[3], label4);

        final FlowPane row5 = new FlowPane();
        final ObservableList<Node> row5Children = row5.getChildren();
        final String numObjectiveExamplesStr = Integer.toString(stats.numObjectiveExamples);
        final Label label5 = new Label(numObjectiveExamplesStr);
        row5Children.addAll(labels[4], label5);

        final FlowPane row6 = new FlowPane();
        final ObservableList<Node> row6Children = row6.getChildren();
        final String numExamplesStr = Integer.toString(stats.numExamples);
        final Label label6 = new Label(numExamplesStr);
        row6Children.addAll(labels[5], label6);

        final FlowPane row7 = new FlowPane();
        final ObservableList<Node> row7Children = row7.getChildren();
        final String numExplorationsStr = Integer.toString(stats.numExplorations);
        final Label label7 = new Label(numExplorationsStr);
        row7Children.addAll(labels[6], label7);

        final FlowPane row8 = new FlowPane();
        final ObservableList<Node> row8Children = row8.getChildren();
        final String numExplorationLessonsStr = Integer.toString(stats.numExplorationLessons);
        final Label label8 = new Label(numExplorationLessonsStr);
        row8Children.addAll(labels[7], label8);

        final FlowPane row9 = new FlowPane();
        final ObservableList<Node> row9Children = row9.getChildren();
        final String numExplorationExamplesStr = Integer.toString(stats.numExplorationExamples);
        final Label label9 = new Label(numExplorationExamplesStr);
        row9Children.addAll(labels[8], label9);

        final FlowPane row10 = new FlowPane();
        final ObservableList<Node> row10Children = row10.getChildren();
        final String numApplicationsStr = Integer.toString(stats.numApplications);
        final Label label10 = new Label(numApplicationsStr);
        row10Children.addAll(labels[9], label10);

        final FlowPane row11 = new FlowPane();
        final ObservableList<Node> row11Children = row11.getChildren();
        final String numApplicationLessonsStr = Integer.toString(stats.numApplicationLessons);
        final Label label11 = new Label(numApplicationLessonsStr);
        row11Children.addAll(labels[10], label11);

        final FlowPane row12 = new FlowPane();
        final ObservableList<Node> row12Children = row12.getChildren();
        final String numApplicationExamplesStr = Integer.toString(stats.numApplicationExamples);
        final Label label12 = new Label(numApplicationExamplesStr);
        row12Children.addAll(labels[11], label12);

        final FlowPane row13 = new FlowPane();
        final ObservableList<Node> row13Children = row13.getChildren();
        final String numHandoutsStr = Integer.toString(stats.numHandouts);
        final Label label13 = new Label(numHandoutsStr);
        row13Children.addAll(labels[12], label13);

        final FlowPane row14 = new FlowPane();
        final ObservableList<Node> row14Children = row14.getChildren();
        final String numItemsStr = Integer.toString(stats.numItems);
        final Label label14 = new Label(numItemsStr);
        row14Children.addAll(labels[13], label14);

        final FlowPane row15 = new FlowPane();
        final ObservableList<Node> row15Children = row15.getChildren();
        final String numAssessmentsStr = Integer.toString(stats.numAssessments);
        final Label label15 = new Label(numAssessmentsStr);
        row15Children.addAll(labels[14], label15);

        final ObservableList<Node> children = getChildren();
        children.addAll(row1, row2, row3, row4, row5, row6, row7, row8, row9, row10, row11, row12, row13, row14, row15);
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

        loadMetadata();
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
     * Attempts to load the metadata file.  If successful the data fields in this pane are updated.
     */
    private void loadMetadata() {

        if (!this.jsonFile.exists()) {
            createBlankMetadataFile();
        }

        if (this.jsonFile.exists()) {
            final String fileData = FileLoader.loadFileAsString(this.jsonFile, false);

            if (fileData == null) {
                // TODO: populate a field with errors.
                Log.warning("There was an error loading the metadata file");
                ++this.numWarnings;
            } else {
                try {
                    final Object parsed = JSONParser.parseJSON(fileData);
                    if (parsed instanceof final JSONObject parsedJson) {
                        populateFields(parsedJson);
                    } else {
                        // TODO: populate a field with errors.
                        Log.warning("There was an error parsing the metadata file");
                        ++this.numWarnings;
                    }
                } catch (final ParsingException e) {
                    // TODO: Populate an errors field with errors.
                    Log.warning("There was an error parsing the metadata file", e);
                    ++this.numWarnings;
                }
            }
        }

        // TODO: Communicate these warnings to the user
    }

    /**
     * Extracts fields from the parsed JSON file and populates the display.
     *
     * @param parsedJson the parsed JSON object
     */
    private void populateFields(final JSONObject parsedJson) {

        final String titleStr = parsedJson.getStringProperty("title");
        final String authorsStr = parsedJson.getStringProperty("authors");
        final String descriptionStr = parsedJson.getStringProperty("description");
        final String goalsStr = parsedJson.getStringProperty("goals");

        this.title.setText(titleStr == null ? CoreConstants.SPC : titleStr);
        this.authors.setText(authorsStr == null ? CoreConstants.SPC : authorsStr);
        this.description.setText(descriptionStr == null ? CoreConstants.EMPTY : descriptionStr);
        this.goals.setText(goalsStr == null ? CoreConstants.EMPTY : goalsStr);
    }

    /**
     * Creates an empty metadata JSON file when one is not found.
     */
    private void createBlankMetadataFile() {

        final String contents = """
                {
                  "title":        "",
                  "description":  "",
                  "authors":      "",
                  "goals":        ""
                }
                """;

        try (final FileWriter writer = new FileWriter(this.jsonFile, StandardCharsets.UTF_8)) {
            writer.write(contents);
        } catch (final IOException ex) {
            Log.warning("Failed to create metadata file.", ex);
        }
    }
}
