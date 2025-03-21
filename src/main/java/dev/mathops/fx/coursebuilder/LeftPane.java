package dev.mathops.fx.coursebuilder;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.File;
import java.util.Objects;

/**
 * A pane  that presents the list of subject directories and (when a subject directory is selected) the list of topic
 * module directories within that subject.  When the user selects a topic module directory, that selection is
 * communicated to the owning application.
 */
class LeftPane extends BorderPane implements ChangeListener<String>, EventHandler<ActionEvent> {

    /** The preferred width of the left pane. */
    static final double PREF_WIDTH = 220.0;

    /** The portion of the left window that is used for subjects. */
    private static final double SUBJECT_PORTION = 0.35;

    /** A character in numbered directory names. */
    private static final char UNDERSCORE = '_';

    /** A character in numbered directory names. */
    private static final char ZERO = '0';

    /** A character in numbered directory names. */
    private static final char NINE = '9';

    /** The course media directory. */
    private final File courseMediaDir;

    /** The owning application. */
    private final CourseBuilder owner;

    /** The list showing subject directories. */
    private final ListView<String> subjectList;

    /** The list showing topic modules for the selected subject. */
    private final ListView<String> topicList;

    /** The "refresh" button. */
    private final Button refreshButton;

    /** Flag indicating a selection listeners are active (false during a refresh). */
    private boolean selectionListenersActive = true;

    /**
     * Constructs a new {@code SubjectDirectoriesList}.
     *
     * @param theOwner          the owning application
     * @param theCourseMediaDir the course media directory
     * @param stageHeight       the stage height
     */
    LeftPane(final CourseBuilder theOwner, final File theCourseMediaDir, final double stageHeight) {

        super();

        setPrefWidth(PREF_WIDTH);

        this.courseMediaDir = theCourseMediaDir;
        this.owner = theOwner;

        setPadding(new Insets(AppConstants.V_PAD, AppConstants.H_PAD, AppConstants.V_PAD, AppConstants.H_PAD));

        final Border border = AppUtils.makeStrokeRightBorder();
        setBorder(border);

        // Top is a list of subject directories in the course media directory
        final VBox top = new VBox(AppConstants.V_PAD);
        setTop(top);

        final ObservableList<Node> topChildren = top.getChildren();

        final Font headingFont = new Font(15.0);

        final Label subjectDirLabel = new Label("Subject Directories:");
        subjectDirLabel.setFont(headingFont);
        topChildren.add(subjectDirLabel);

        this.subjectList = new ListView<>();
        this.subjectList.setPrefHeight(stageHeight * SUBJECT_PORTION);

        final ObservableList<String> subjectItems = FXCollections.observableArrayList();
        final File[] files = this.courseMediaDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    final String name = file.getName();
                    if (isNumberedName(name)) {
                        subjectItems.add(name);
                    }
                }
            }
        }
        this.subjectList.setItems(subjectItems);
        topChildren.add(this.subjectList);

        // Center is a list of topic module directories in the selected subject directory
        final BorderPane center = new BorderPane();
        setCenter(center);

        final Label topicDirLabel = new Label("Topic Module Directories:");
        topicDirLabel.setPadding(new Insets(AppConstants.V_GAP, 0.0, AppConstants.V_PAD, 0.0));
        topicDirLabel.setFont(headingFont);
        center.setTop(topicDirLabel);

        this.topicList = new ListView<>();
        center.setCenter(this.topicList);

        // Bottom is a refresh button to rescan directories
        final FlowPane bottom = new FlowPane();
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(AppConstants.BUTTON_ROW_PADDING);
        setBottom(bottom);

        this.refreshButton = new Button("Refresh");
        final ObservableList<Node> bottomChildren = bottom.getChildren();
        bottomChildren.add(this.refreshButton);
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

        final MultipleSelectionModel<String> subjectSelectionModel = this.subjectList.getSelectionModel();
        final MultipleSelectionModel<String> topicSelectionModel = this.topicList.getSelectionModel();

        final ReadOnlyObjectProperty<String> selectedSubject = subjectSelectionModel.selectedItemProperty();
        final ReadOnlyObjectProperty<String> selectedTopic = topicSelectionModel.selectedItemProperty();

        selectedSubject.addListener(this);
        selectedTopic.addListener(this);

        this.refreshButton.setOnAction(this);
        refresh();
    }

    /**
     * Tests whether a name starts with two numbers then an underscore.
     *
     * @param name the name
     * @return true if the name is a "numbered" name
     */
    private static boolean isNumberedName(final CharSequence name) {

        final boolean numbered;

        if (name.length() >= 3) {
            final int ch0 = (int) name.charAt(0);
            final int ch1 = (int) name.charAt(1);
            final int ch2 = (int) name.charAt(2);

            numbered = ch2 == (int) UNDERSCORE && isDigit(ch0) && isDigit(ch1);
        } else {
            numbered = false;
        }

        return numbered;
    }

    /**
     * Tests whether a character is an ASCII digit.
     *
     * @param code the character code
     * @return true if the character code lies in the range '0' through '9' (inclusive)
     */

    private static boolean isDigit(final int code) {

        return code >= (int) ZERO && code <= (int) NINE;
    }

    /**
     * Called when an item is selected in a list.
     *
     * @param observableValue the observable value
     * @param oldValue        the old value
     * @param newValue        the new value
     */
    @Override
    public void changed(final ObservableValue<? extends String> observableValue, final String oldValue,
                        final String newValue) {

        if (this.selectionListenersActive) {
            final MultipleSelectionModel<String> subjectSelectionModel = this.subjectList.getSelectionModel();

            if (observableValue == subjectSelectionModel.selectedItemProperty()) {

                final ObservableList<String> topicListItems = this.topicList.getItems();

                if (newValue == null) {
                    topicListItems.clear();
                    this.owner.topicSelected(null);
                } else {
                    final File subjectDir = new File(this.courseMediaDir, newValue);
                    if (subjectDir.exists() && subjectDir.isDirectory()) {

                        if (!newValue.equals(oldValue)) {
                            topicListItems.clear();
                            final File[] files = subjectDir.listFiles();
                            if (files != null) {
                                for (final File file : files) {
                                    if (file.isDirectory()) {
                                        final String name = file.getName();
                                        if (isNumberedName(name)) {
                                            topicListItems.add(name);
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        topicListItems.clear();
                        this.owner.topicSelected(null);
                    }
                }
            } else {
                final MultipleSelectionModel<String> topicSelectionModel = this.topicList.getSelectionModel();

                if (observableValue == topicSelectionModel.selectedItemProperty()) {

                    if (newValue == null) {
                        this.owner.topicSelected(null);
                    } else {
                        final String selectedSubject = subjectSelectionModel.getSelectedItem();
                        if (selectedSubject != null) {
                            final File subjectDir = new File(this.courseMediaDir, selectedSubject);
                            final File topicDir = new File(subjectDir, newValue);
                            if (topicDir.exists() && topicDir.isDirectory()) {
                                this.owner.topicSelected(topicDir);
                            } else {
                                this.owner.topicSelected(null);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when a button invokes an action.
     *
     * @param actionEvent the action event
     */
    @Override
    public void handle(final ActionEvent actionEvent) {

        refresh();
    }

    /**
     * Refreshes the window.  This attempts to preserve the user's selection if there was a selection, and it still
     * exists.
     */
    private void refresh() {

        this.selectionListenersActive = false;

        final MultipleSelectionModel<String> subjectSelectionModel = this.subjectList.getSelectionModel();
        final MultipleSelectionModel<String> topicSelectionModel = this.topicList.getSelectionModel();

        final String selectedSubject = subjectSelectionModel.getSelectedItem();
        final String selectedTopic = topicSelectionModel.getSelectedItem();

        final ObservableList<String> subjectItems = this.subjectList.getItems();
        subjectItems.clear();

        final File[] files = this.courseMediaDir.listFiles();
        if (Objects.nonNull(files)) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    final String name = file.getName();
                    if (isNumberedName(name)) {
                        subjectItems.add(name);
                    }
                }
            }
        }

        final ObservableList<String> topicItems = this.topicList.getItems();
        topicItems.clear();

        if (Objects.nonNull(selectedSubject) && subjectItems.contains(selectedSubject)) {
            subjectSelectionModel.select(selectedSubject);

            final File subjectDir = new File(this.courseMediaDir, selectedSubject);
            if (subjectDir.exists() && subjectDir.isDirectory()) {

                final File[] topicFiles = subjectDir.listFiles();
                if (topicFiles != null) {
                    for (final File file : topicFiles) {
                        if (file.isDirectory()) {
                            final String name = file.getName();
                            if (isNumberedName(name)) {
                                topicItems.add(name);
                            }
                        }
                    }
                }
            }

            if (Objects.nonNull(selectedTopic) && topicItems.contains(selectedTopic)) {
                topicSelectionModel.select(selectedTopic);

                final File topicDir = new File(subjectDir, selectedTopic);
                this.owner.clearCache();
                this.owner.topicSelected(topicDir);
            } else {
                this.owner.topicSelected(null);
            }
        } else {
            this.owner.topicSelected(null);
        }

        this.selectionListenersActive = true;
    }
}
