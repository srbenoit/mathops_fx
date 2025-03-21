package dev.mathops.fx.coursebuilder;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

/**
 * Utilities to perform common tasks.
 */
public enum AppUtils {
    ;

    /**
     * Constructs a flow pane with "Location: " followed by a location string.
     *
     * @param locationString the location string
     * @param label          the label to display before the link
     * @param target         the target file to be opened when the link is clicked
     * @param preNodes       zero or nodes to add at the start of the flow
     * @return the flow pane
     */
    static FlowPane makeLocationFlow(final Application app, final String label, final String locationString,
                                     final File target, final Node... preNodes) {

        final Platform.Preferences preferences = Platform.getPreferences();
        final ColorScheme scheme = preferences.getColorScheme();
        final Color dataColor = scheme == ColorScheme.DARK ? Color.LIGHTBLUE : Color.BLUE;

        final FlowPane top = new FlowPane();
        final ObservableList<Node> topChildren = top.getChildren();

        final Label headingLabel = new Label(label);
        headingLabel.setPrefWidth(AppConstants.LABEL_WIDTH);
        headingLabel.setAlignment(Pos.BASELINE_RIGHT);
        final Hyperlink locationLink = new Hyperlink(locationString);
        locationLink.setOnAction(new ActionEventEventHandler(target, app));
        locationLink.setTextFill(dataColor);

        if (preNodes != null) {
            topChildren.addAll(preNodes);
        }
        topChildren.addAll(headingLabel, locationLink);

        return top;
    }

    /**
     * Loads an icon image
     *
     * @param filename the icon filename
     * @return the resulting image; null if the icon could not be loaded
     */
    static ImageView loadIcon(final String filename) {

        ImageView icon = null;

        final byte[] imgBytes = FileLoader.loadFileAsBytes(CourseBuilder.class, filename, true);
        if (imgBytes != null) {
            final Image image = new Image(new ByteArrayInputStream(imgBytes));
            icon = new ImageView(image);
        }

        return icon;
    }

    /**
     * Creates a tab.
     *
     * @param title       the tab title
     * @param content     the content node
     * @param icon        the icon filename
     * @param numWarnings the number of warnings (if nonzero, an annotation is added to the tab graphic)
     * @return the constructed {@code Tab}
     */
    static Tab makeTab(final String title, final Node content, final String icon, final int numWarnings) {

        final Tab tab = new Tab(title, content);

        final byte[] imgBytes = icon == null ? null : FileLoader.loadFileAsBytes(CourseBuilder.class, icon, true);

        if (imgBytes == null) {
            if (numWarnings > 0) {
                final Node annotation = makeWarningAnnotation(numWarnings);
                tab.setGraphic(annotation);
            }
        } else {
            final Image image = new Image(new ByteArrayInputStream(imgBytes));
            final ImageView view = new ImageView(image);
            if (numWarnings > 0) {
                final HBox box = new HBox();
                box.setSpacing(2.0);
                box.setFillHeight(false);
                box.setAlignment(Pos.BASELINE_LEFT);

                final Node annotation = makeWarningAnnotation(numWarnings);
                final ObservableList<Node> boxChildren = box.getChildren();
                boxChildren.addAll(view, annotation);
                tab.setGraphic(box);
            } else {
                tab.setGraphic(view);
            }
        }

        tab.setClosable(false);

        return tab;
    }

    /**
     * Determines a color to use for borders, based on the current scheme.
     *
     * @return the border color
     */
    public static Color getBorderColor() {

        final Platform.Preferences preferences = Platform.getPreferences();
        final ColorScheme scheme = preferences.getColorScheme();

        return scheme == ColorScheme.DARK ? Color.DARKGRAY : Color.SILVER;
    }

    /**
     * Determines a color to use for borders, based on the current scheme.
     *
     * @return the border color
     */
    public static Color getFocusedBorderColor() {

        final Platform.Preferences preferences = Platform.getPreferences();
        final ColorScheme scheme = preferences.getColorScheme();

        return scheme == ColorScheme.DARK ? Color.STEELBLUE : Color.SKYBLUE;
    }

    /**
     * Creates a width 1.0 solid border whose color is based on the current scheme.
     *
     * @return the border
     */
    public static Border makeStrokeBorder() {

        final Color borderColor = getBorderColor();

        final BorderStroke rightBorderStroke = new BorderStroke(borderColor, BorderStrokeStyle.SOLID, null,
                AppConstants.ONE_PIX_BORDER);

        return new Border(rightBorderStroke);
    }

    /**
     * Creates a width 1.0 solid border along the top edge only whose color is based on the current scheme.
     *
     * @return the border
     */
    static Border makeStrokeTopBorder() {

        final Color borderColor = getBorderColor();

        final BorderStroke rightBorderStroke = new BorderStroke(borderColor, BorderStrokeStyle.SOLID, null,
                AppConstants.ONE_PIX_TOP_BORDER);

        return new Border(rightBorderStroke);
    }

    /**
     * Creates a width 1.0 solid border along the top edge only whose color is based on the current scheme.
     *
     * @return the border
     */
    static Border makeStrokeRightBorder() {

        final Color borderColor = getBorderColor();

        final BorderStroke rightBorderStroke = new BorderStroke(borderColor, BorderStrokeStyle.SOLID, null,
                AppConstants.ONE_PIX_RIGHT_BORDER);

        return new Border(rightBorderStroke);
    }

    /**
     * Creates a node that can be used as a graphic in a Tab to show the number of warnings in that tab or its
     * descendants.
     *
     * @param numWarnings the number of warnings
     * @return the node
     */
    static Node makeWarningAnnotation(final int numWarnings) {

        final HBox box = new HBox();

        final CornerRadii radii = new CornerRadii(5.0);
        final BackgroundFill redFill = new BackgroundFill(AppConstants.ORANGE, radii, null);
        final Background redBackground = new Background(redFill);
        box.setBackground(redBackground);
        box.setPadding(new Insets(0.0, 2.0, 0.0, 2.0));

        final String str = Integer.toString(numWarnings);

        final Label label = new Label(str);
        label.setTextFill(AppConstants.WHITE);
        final Font font = Font.font("System", FontWeight.BOLD, 10.0);
        label.setFont(font);

        final ObservableList<Node> children = box.getChildren();
        children.add(label);

        return box;
    }

    /**
     * Adds a row of controls for a single documen t (without link or "open" button) to a grid pane.
     *
     * @param file      the file
     * @param labelText the label text
     * @param nodes     the node list to which to add controls
     * @param row       the row to which to assign controls
     */
    static void addSimpleDocumentRow(final File file, final String labelText, final Collection<? super Node> nodes,
                                     final int row) {

        final ImageView icon = loadIcon(AppConstants.UNEXPECTED_ICON);
        if (icon != null) {
            GridPane.setConstraints(icon, 0, row);
            nodes.add(icon);
        }

        final Hyperlink label = new Hyperlink(labelText);
        if (label.getPrefWidth() < 180.0) {
            label.setPrefWidth(180.0);
        }
        label.setUserData(file);
        label.setTextFill(AppConstants.RED);
        GridPane.setConstraints(label, 1, row);
        nodes.add(label);
    }

    /**
     * Tests whether a code point is an ASCII digit.
     *
     * @param codePoint the code point
     * @return true if the code point is an ASCII digit
     */
    static boolean isAsciiDigit(final int codePoint) {

        return codePoint >= AppConstants.DIGIT_0 && codePoint <= AppConstants.DIGIT_9;
    }

    /**
     * Tests whether a code point is an ASCII uppercase letter.
     *
     * @param codePoint the code point
     * @return true if the code point is an ASCII uppercase letter
     */
    static boolean isAsciiLetter(final int codePoint) {

        return codePoint >= AppConstants.LETTER_A && codePoint <= AppConstants.LETTER_Z;
    }

    /**
     * Tests whether a directory name is valid for an introductory lesson.
     *
     * @param name the directory name
     * @return true if it is a valid introductory lesson directory
     */
    static boolean isIntroDir(final String name) {

        boolean result = false;

        if (name.length() == 10 && name.indexOf("_intro_") == 2) {
            final int c0 = (int) name.charAt(0);
            final int c1 = (int) name.charAt(1);
            final int c9 = (int) name.charAt(9);

            result = c0 == AppConstants.DIGIT_0 && isAsciiDigit(c1) && isAsciiDigit(c9);
        }

        return result;
    }

    /**
     * Tests whether a directory name is valid for a lesson.
     *
     * @param name the directory name
     * @return true if it is a valid lesson directory
     */
    static boolean isLessonDir(final String name) {

        boolean result = false;

        if (name.length() == 11 && name.indexOf("_lesson_") == 2) {
            final int c0 = (int) name.charAt(0);
            final int c1 = (int) name.charAt(1);
            final int c10 = (int) name.charAt(10);

            result = (c0 == AppConstants.DIGIT_1 || c0 == AppConstants.DIGIT_2) && isAsciiDigit(c1)
                     && isAsciiDigit(c10);
        }

        return result;
    }

    /**
     * Tests whether a directory name is valid for a standard.
     *
     * @param name the directory name
     * @return true if it is a valid standard directory
     */
    static boolean isStandardDir(final String name) {

        boolean result = false;

        if (name.length() == 13 && name.indexOf("_standard_") == 2) {
            final int c0 = (int) name.charAt(0);
            final int c1 = (int) name.charAt(1);
            final int c12 = (int) name.charAt(12);

            result = (c0 == AppConstants.DIGIT_1 || c0 == AppConstants.DIGIT_2) && isAsciiDigit(c1)
                     && isAsciiDigit(c12);
        }

        return result;
    }

    /**
     * Tests whether a directory name is valid for a conclusion lesson.
     *
     * @param name the directory name
     * @return true if it is a valid conclusion lesson directory
     */
    static boolean isConclusionDir(final String name) {

        boolean result = false;

        if (name.length() == 15 && name.indexOf("_conclusion_") == 2) {
            final int c0 = (int) name.charAt(0);
            final int c1 = (int) name.charAt(1);
            final int c14 = (int) name.charAt(14);

            result = c0 == AppConstants.DIGIT_9 && isAsciiDigit(c1) && isAsciiDigit(c14);
        }

        return result;
    }

    /**
     * Tests whether a directory name is valid for an objective.
     *
     * @param name the directory name
     * @return true if it is a valid objective directory
     */
    static boolean isObjectiveDir(final String name) {

        boolean result = false;

        if (name.length() == 14 && name.indexOf("_objective_") == 2) {
            final int c0 = (int) name.charAt(0);
            final int c1 = (int) name.charAt(1);
            final int c13 = (int) name.charAt(13);

            result = (c0 == AppConstants.DIGIT_1 || c0 == AppConstants.DIGIT_2) && isAsciiDigit(c1)
                     && isAsciiLetter(c13);
        }

        return result;
    }

    /**
     * A handler for actions generated by clicking on a location file link.
     */
    private record ActionEventEventHandler(File target, Application app) implements EventHandler<ActionEvent> {

        /**
         * Called when a button invokes an action.
         *
         * @param actionEvent the action event
         */
        @Override
        public void handle(final ActionEvent actionEvent) {

            final HostServices hostServices = this.app.getHostServices();

            final URI dirUri = this.target.toURI();
            final String uriStr = dirUri.toString();

            Log.info("Show document: ", uriStr);
            hostServices.showDocument(uriStr);
        }
    }

    /**
     * Recursively counts the files in a directory.
     *
     * @param dir the directory
     * @return the number of files, including the directory itself
     */
    static int countFiles(final File dir) {

        int count = 1;

        final File[] list = dir.listFiles();
        if (list != null) {
            for (final File file : list) {
                if (file.isDirectory()) {
                    count += countFiles(file);
                } else {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * Copies a file.
     *
     * @param source the source file
     * @param target the target file
     */
     static boolean copyFile(final File source, final File target) {

        boolean success = false;

        if (source.exists()) {
            final Path sourcePath = source.toPath();
            final Path targetPath = target.toPath();
            try {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                success = true;
            } catch (final IOException ex) {
                Log.warning(ex);
                // TODO: Show error: failed to create directory
            }
        }

        return success;
    }
}
