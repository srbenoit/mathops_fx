package dev.mathops.fx.coursebuilder;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Font;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The content of a "Lesson" tab.
 *
 * <p>
 * This tab presents the contents of a single lesson directory, typically with a name of the form "11_lesson_1".
 *
 * <p>
 * The set of recognized files in such a directory includes:
 * <ul>
 *     <li>{@code metadata.json}</li>
 *     <li>{@code slides.pptx} and {@code slides.pdf}</li>
 *     <li>{@code screen.mp4} and {@code screen.wav}</li>
 *     <li>{@code camera.mp4} and {@code camera.wav}</li>
 *     <li>{@code image_##.jpg}, {@code image_##.jpeg}, {@code image_##.png}, and {@code image_##.xcf}</li>
 *     <li>{@code drawing_##.svg} and {@code drawing_##.xml}</li>
 *     <li>{@code graph_##.xml}</li>
 *     <li>{@code video_##.mp4}</li>
 *     <li>{@code final.prproj}, {@code final.mp4}, {@code final.txt}, and {@code final.vtt}</li>
 * </ul>
 *
 * <p>
 * The metadata JSON file will have the following structure:
 * <pre>
 * {
 *   "title":        "A short title for the example.",
 *   "description":  "A description of the example and its target competencies.",
 *   "authors":      "The author(s)",
 *   "attributions":
 *   [
 *     {
 *       "resource":    "A filename of an image, drawing, graph, video; one per asset file.",
 *       "author":      "The author of the asset file.",
 *       "source":      "The source of the asset file, like 'pexels.com (SomeImage.png)'.",
 *       "license":     "The license under which the asset is used."
 *     }
 *   ]
 *   "notes":
 *   [
 *     {
 *       "resource":    "A filename to which to attach notes.",
 *       "author":      "The author of the note.",
 *       "note":        "The note, like 'Extraneous noise in recording at 5:22'."
 *     }
 *   ]
 * }
 * </pre>
 * <p>
 * The panel shows the list of files/assets on the left-hand side, and has a preview pane on the right.  When one
 * of the files is selected, the file is displayed in the preview pane.  Above the preview pane is a label to
 * identify the currently displayed preview, and below the preview pane are fields for file information, attributions,
 * and notes.
 */
final class LessonTabContent extends BorderPane implements EventHandler<MouseEvent> {

    /** The owning application. */
    private final CourseBuilder owner;

    /** The title text field. */
    private final TextField title;

    /** The description text field. */
    private final TextArea description;

    /** The authors text field. */
    private final TextField authors;

    /** The preview pane area. */
    private final BorderPane previewPane;

    /** The currently displayed preview. */
    private Node currentPreview;

    /** The list of "Open" buttons. */
    private final List<Button> openButtons;

    /** The list of links to preview documents. */
    private final List<Hyperlink> previewLinks;

    /** The number of warnings found (unexpected or missing files). */
    private int numWarnings = 0;

    /**
     * Constructs a new {@code LessonTabContent}.
     *
     * @param theOwner      the owning application
     * @param theExampleDir the directory with example files
     */
    LessonTabContent(final CourseBuilder theOwner, final File theExampleDir) {

        super();

        this.owner = theOwner;

        final JSONObject metadata = loadMetadata(theExampleDir);

        this.title = new TextField();
        this.title.setPrefColumnCount(40);
        this.title.setTooltip(new Tooltip(" short title for the example."));
        this.description = new TextArea();
        this.description.setPrefRowCount(3);
        this.description.setPrefColumnCount(40);
        this.description.setTooltip(new Tooltip("A description of the example and its target competencies."));
        this.authors = new TextField();
        this.authors.setPrefColumnCount(40);
        this.authors.setTooltip(new Tooltip("Comma-separated list of authors, like 'John Doe, Jane Doe'."));

        this.openButtons = new ArrayList<>(10);
        this.previewLinks = new ArrayList<>(10);

        setPadding(AppConstants.PADDING);

        final VBox left = buildLeftSide(theExampleDir, metadata);
        setLeft(left);

        this.previewPane = new BorderPane();
        setCenter(this.previewPane);
    }

    /**
     * Attempts to load the metadata file from the example directory.  If there is no metadata file, or it cannot be
     * read, an empty JSON object is returned.
     *
     * @param theExampleDir the directory with example files
     * @return the loaded (or empty) metadata JSON object
     */
    private JSONObject loadMetadata(final File theExampleDir) {

        final File metadataFile = new File(theExampleDir, AppConstants.METADATA_FILE);
        final String content = FileLoader.loadFileAsString(metadataFile, false);

        JSONObject result;

        if (content == null) {
            result = new JSONObject();
        } else {
            try {
                final Object obj = JSONParser.parseJSON(content);
                if (obj instanceof final JSONObject json) {
                    result = json;
                } else {
                    Log.warning("Result of parsing metadata file is not JSON object.");
                    result = new JSONObject();
                    ++this.numWarnings;
                }
            } catch (final ParsingException ex) {
                Log.warning("Unable to parse metadata file", ex);
                result = new JSONObject();
                ++this.numWarnings;
            }
        }

        // TODO: Communicate these warnings to the user...

        return result;
    }

    /**
     * Builds the left side content.
     *
     * @param theExampleDir the directory with example files
     * @param metadata      the loaded metadata object
     * @return the left-side box
     */
    private VBox buildLeftSide(final File theExampleDir, final JSONObject metadata) {

        final VBox left = new VBox();
        setLeft(left);

        final GridPane metadataPane = new GridPane();
        metadataPane.setHgap(10);
        metadataPane.setVgap(4);

        final Label titleLabel = new Label("Title:");
        GridPane.setConstraints(titleLabel, 0, 0);
        GridPane.setConstraints(this.title, 1, 0);

        final String titleStr = metadata.getStringProperty(AppConstants.TITLE_PROPERTY);
        if (Objects.nonNull(titleStr)) {
            this.title.setText(titleStr);
        }

        final Label descriptionLabel = new Label("Description:");
        GridPane.setConstraints(descriptionLabel, 0, 1);
        GridPane.setValignment(descriptionLabel, VPos.TOP);
        GridPane.setConstraints(this.description, 1, 1);

        final String descriptionStr = metadata.getStringProperty(AppConstants.DESCRIPTION_PROPERTY);
        if (Objects.nonNull(descriptionStr)) {
            this.description.setText(descriptionStr);
        }

        final Label authorsLabel = new Label("Author(s):");
        GridPane.setConstraints(authorsLabel, 0, 2);
        GridPane.setConstraints(this.authors, 1, 2);

        final String authorsStr = metadata.getStringProperty(AppConstants.AUTHORS_PROPERTY);
        if (Objects.nonNull(authorsStr)) {
            this.authors.setText(authorsStr);
        }

        final ObservableList<Node> metadataChildren = metadataPane.getChildren();
        metadataChildren.addAll(titleLabel, this.title, descriptionLabel, this.description, authorsLabel,
                this.authors);

        final Label heading1 = new Label("Assets:");
        heading1.setPadding(new Insets(20.0, 0.0, 0.0, 0.0));
        final Font defaultFont = heading1.getFont();
        final String defaultFontName = defaultFont.getName();
        final double defaultFontSize = defaultFont.getSize();
        final Font headingFont = new Font(defaultFontName, defaultFontSize * 1.1);
        heading1.setFont(headingFont);

        final Label heading2 = new Label("Video:");
        heading2.setFont(headingFont);

        final Label heading3 = new Label("Lesson Notes:");
        heading3.setFont(headingFont);

        final Label heading4 = new Label("Unexpected Files:");
        heading4.setFont(headingFont);

        final Label heading5 = new Label("Missing (expected) Files:");
        heading5.setFont(headingFont);

        final GridPane assetsPane = new GridPane();
        assetsPane.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
        assetsPane.setHgap(10);
        assetsPane.setVgap(4);
        final ObservableList<Node> assetsChildren = assetsPane.getChildren();

        final GridPane finalPane = new GridPane();
        finalPane.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
        finalPane.setHgap(10);
        finalPane.setVgap(4);
        final ObservableList<Node> finalChildren = finalPane.getChildren();

        final GridPane notesPane = new GridPane();
        notesPane.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
        notesPane.setHgap(10);
        notesPane.setVgap(4);
        final ObservableList<Node> notesChildren = notesPane.getChildren();

        final GridPane unexpectedPane = new GridPane();
        unexpectedPane.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
        unexpectedPane.setHgap(10);
        unexpectedPane.setVgap(4);
        final ObservableList<Node> unexpectedChildren = unexpectedPane.getChildren();

        final GridPane missingPane = new GridPane();
        missingPane.setPadding(new Insets(0.0, 0.0, 0.0, 20.0));
        missingPane.setHgap(10);
        missingPane.setVgap(4);
        final ObservableList<Node> missingChildren = missingPane.getChildren();

        final List<File> fileList = new ArrayList<>(20);
        final List<File> missingFiles = new ArrayList<>(5);

        final File[] files = theExampleDir.listFiles();
        if (Objects.nonNull(files)) {

            for (final File file : files) {
                final String name = file.getName();
                if (!AppConstants.METADATA_FILE.equals(name)) {
                    fileList.add(file);
                }
            }
            fileList.sort(null);

            // We sweep the file list for assets and video files and present those in the UI, and remove all
            // processed files as we go.  In the end, any files remaining are presented as "unexpected" (potential
            // typos in file names, extraneous files, or files we should add as "recognized" assets or documents).

            int assetRow = 0;

            // Add all drawings first (file list is sorted, so these should be in index order);
            final Iterator<File> drawingIter = fileList.iterator();
            while (drawingIter.hasNext()) {
                final File file = drawingIter.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.DRAWING_PREFIX)) {
                    if (name.endsWith(AppConstants.SVG_EXT)) {
                        addDocumentRow(file, AppConstants.SVG_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        drawingIter.remove();
                    } else if (name.endsWith(AppConstants.XML_EXT)) {
                        addDocumentRow(file, AppConstants.XML_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        drawingIter.remove();
                    } else if (name.endsWith(AppConstants.PNG_EXT)) {
                        addDocumentRow(file, AppConstants.PNG_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        drawingIter.remove();
                    } else if (name.endsWith(AppConstants.JPG_EXT) || name.endsWith(AppConstants.JPEG_EXT)) {
                        addDocumentRow(file, AppConstants.JPG_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        drawingIter.remove();
                    }
                }
            }

            // Add all images next (file list is sorted, so these should be in index order);
            final Iterator<File> imageIter = fileList.iterator();
            while (imageIter.hasNext()) {
                final File file = imageIter.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.IMAGE_PREFIX)) {
                    if (name.endsWith(AppConstants.SVG_EXT)) {
                        addDocumentRow(file, AppConstants.SVG_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        imageIter.remove();
                    } else if (name.endsWith(AppConstants.PNG_EXT)) {
                        addDocumentRow(file, AppConstants.PNG_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        imageIter.remove();
                    } else if (name.endsWith(AppConstants.JPG_EXT) || name.endsWith(AppConstants.JPEG_EXT)) {
                        addDocumentRow(file, AppConstants.JPG_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        imageIter.remove();
                    } else if (name.endsWith(AppConstants.XCF_EXT)) {
                        addDocumentRow(file, AppConstants.XCF_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        imageIter.remove();
                    }
                }
            }

            // Add slides next (PowerPoint first, PDF second)
            final Iterator<File> slidesIter1 = fileList.iterator();
            boolean seekingSlides = true;
            while (slidesIter1.hasNext()) {
                final File file = slidesIter1.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.SLIDES_PREFIX)) {
                    if (name.endsWith(AppConstants.PPTX_EXT)) {
                        addDocumentRow(file, AppConstants.POWERPOINT_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        slidesIter1.remove();
                        seekingSlides = false;
                    }
                }
            }
            if (seekingSlides) {
                missingFiles.add(new File(theExampleDir, "slides.pptx"));
            }
            final Iterator<File> slidesIter2 = fileList.iterator();
            while (slidesIter2.hasNext()) {
                final File file = slidesIter2.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.SLIDES_PREFIX)) {
                    if (name.endsWith(AppConstants.PDF_EXT)) {
                        addDocumentRow(file, AppConstants.PDF_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        slidesIter2.remove();
                    }
                }
            }

            // Add all video next (file list is sorted, so these should be in index order);
            final Iterator<File> videoIter = fileList.iterator();
            while (videoIter.hasNext()) {
                final File file = videoIter.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.CAMERA_PREFIX)) {
                    if (name.endsWith(AppConstants.MP4_EXT)) {
                        addDocumentRow(file, AppConstants.MP4_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        videoIter.remove();
                    } else if (name.endsWith(AppConstants.WAV_EXT)) {
                        addDocumentRow(file, AppConstants.WAV_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        videoIter.remove();
                    }
                } else if (name.startsWith(AppConstants.SCREEN_PREFIX)) {
                    if (name.endsWith(AppConstants.MP4_EXT)) {
                        addDocumentRow(file, AppConstants.MP4_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        videoIter.remove();
                    } else if (name.endsWith(AppConstants.WAV_EXT)) {
                        addDocumentRow(file, AppConstants.WAV_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        videoIter.remove();
                    }
                } else if (name.startsWith(AppConstants.VIDEO_PREFIX)) {
                    if (name.endsWith(AppConstants.MP4_EXT)) {
                        addDocumentRow(file, AppConstants.MP4_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        videoIter.remove();
                    } else if (name.endsWith(AppConstants.WAV_EXT)) {
                        addDocumentRow(file, AppConstants.WAV_ICON, name, assetsChildren, assetRow, false);
                        ++assetRow;
                        videoIter.remove();
                    }
                }
            }

            final Iterator<File> dataIter = fileList.iterator();
            while (dataIter.hasNext()) {
                final File file = dataIter.next();
                final String name = file.getName();
                if ("data.xlsx".equals(name)) {
                    addDocumentRow(file, AppConstants.EXCEL_ICON, name, assetsChildren, assetRow, false);
                    ++assetRow;
                    dataIter.remove();
                }
            }

            int finalRow = 0;

            // Add the final video product (video first, then VTT/TXT files, then Premiere project)
            final Iterator<File> finalIter1 = fileList.iterator();
            boolean seekingMp4 = true;
            while (finalIter1.hasNext()) {
                final File file = finalIter1.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.FINAL_PREFIX)) {
                    if (name.endsWith(AppConstants.MP4_EXT)) {
                        addDocumentRow(file, AppConstants.MP4_ICON, name + " (for student)", finalChildren, finalRow,
                                false);
                        ++finalRow;
                        finalIter1.remove();
                        seekingMp4 = false;
                    }
                }
            }
            if (seekingMp4) {
                missingFiles.add(new File(theExampleDir, "final.mp4"));
            }

            final Iterator<File> finalIter2 = fileList.iterator();
            boolean seekingVtt = true;
            while (finalIter2.hasNext()) {
                final File file = finalIter2.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.FINAL_PREFIX)) {
                    if (name.endsWith(AppConstants.VTT_EXT)) {
                        addDocumentRow(file, AppConstants.VTT_ICON, name + " (closed-captions)", finalChildren,
                                finalRow, false);
                        ++finalRow;
                        finalIter2.remove();
                        seekingVtt = false;
                    }
                }
            }
            if (seekingVtt) {
                missingFiles.add(new File(theExampleDir, "final.vtt"));
            }

            final Iterator<File> finalIter3 = fileList.iterator();
            boolean seekingTxt = true;
            while (finalIter3.hasNext()) {
                final File file = finalIter3.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.FINAL_PREFIX)) {
                    if (name.endsWith(AppConstants.TXT_EXT)) {
                        addDocumentRow(file, AppConstants.TXT_ICON, name + " (text transcript)", finalChildren,
                                finalRow, false);
                        ++finalRow;
                        finalIter3.remove();
                        seekingTxt = false;
                    }
                }
            }
            if (seekingTxt) {
                missingFiles.add(new File(theExampleDir, "final.txt"));
            }

            final Iterator<File> finalIter4 = fileList.iterator();
            while (finalIter4.hasNext()) {
                final File file = finalIter4.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.FINAL_PREFIX)) {
                    if (name.endsWith(AppConstants.PRPROJ_EXT)) {
                        addDocumentRow(file, AppConstants.PREMIERE_ICON, name + " (for author)", finalChildren,
                                finalRow, false);
                        ++finalRow;
                        finalIter4.remove();
                    }
                }
            }

            int notesRow = 0;

            // Add lecture notes
            final Iterator<File> notesIter = fileList.iterator();
            while (notesIter.hasNext()) {
                final File file = notesIter.next();
                final String name = file.getName();
                if (name.startsWith(AppConstants.NOTES_PREFIX)) {
                    if (name.endsWith(AppConstants.PDF_EXT)) {
                        addDocumentRow(file, AppConstants.PDF_ICON, name + " (for student)", notesChildren, notesRow,
                                false);
                        ++finalRow;
                        notesIter.remove();
                    } else if (name.endsWith(AppConstants.DOCX_EXT)) {
                        addDocumentRow(file, AppConstants.WORD_ICON, name + " (for author)", notesChildren, notesRow,
                                false);
                        ++finalRow;
                        notesIter.remove();
                    } else if (name.endsWith(AppConstants.PPTX_EXT)) {
                        addDocumentRow(file, AppConstants.POWERPOINT_ICON, name + " (for author)", notesChildren,
                                notesRow, false);
                        ++finalRow;
                        notesIter.remove();
                    }
                }
            }

            // Present any unexpected files that remain
            int unexpectedRow = 0;
            for (final File file : fileList) {
                final String name = file.getName();
                addDocumentRow(file, AppConstants.UNEXPECTED_ICON, name, unexpectedChildren, unexpectedRow, true);
                ++unexpectedRow;
                ++this.numWarnings;
            }

            // Present any missing files
            int missingRow = 0;
            for (final File file : missingFiles) {
                final String name = file.getName();
                addDocumentRow(file, AppConstants.UNEXPECTED_ICON, name, missingChildren, missingRow, true);
                ++missingRow;
                ++this.numWarnings;
            }
        }

        final ObservableList<Node> leftChildren = left.getChildren();
        leftChildren.addAll(metadataPane, heading1, assetsPane, heading2, finalPane, heading3, notesPane);

        if (!fileList.isEmpty()) {
            leftChildren.addAll(heading4, unexpectedPane);
        }
        if (!missingFiles.isEmpty()) {
            leftChildren.addAll(heading5, missingPane);
        }

        return left;
    }

    /**
     * Adds a row of controls for a single document to a grid pane.
     *
     * @param file         the file
     * @param iconFilename the icon filename
     * @param labelText    the label text
     * @param nodes        the node list to which to add controls
     * @param row          the row to which to assign controls
     * @param red          true to present the row label in a red font to indicate a warning
     */
    private void addDocumentRow(final File file, final String iconFilename, final String labelText,
                                final Collection<? super Node> nodes, final int row, final boolean red) {

        final ImageView icon = AppUtils.loadIcon(iconFilename);
        if (icon != null) {
            GridPane.setConstraints(icon, 0, row);
            nodes.add(icon);
        }

        final Hyperlink label = new Hyperlink(labelText);
        if (label.getPrefWidth() < 160.0) {
            label.setPrefWidth(160.0);
        }
        label.setUserData(file);
        if (red) {
            label.setTextFill(AppConstants.RED);
        }
        this.previewLinks.add(label);
        GridPane.setConstraints(label, 1, row);
        nodes.add(label);

        final Button button = new Button("Open");
        button.setUserData(file);
        this.openButtons.add(button);
        GridPane.setConstraints(button, 2, row);
        nodes.add(button);
    }

    /**
     * Initializes this panel.  Called after the constructor completes since this method uses "this" to add itself as a
     * listener.
     */
    void init() {

        final EventHandler<ActionEvent> actionHandler = new ActionHandler(this.owner);
        for (final Button button : this.openButtons) {
            button.setOnAction(actionHandler);
        }

        for (final Hyperlink link : this.previewLinks) {
            link.setOnMouseClicked(this);
        }
    }

    /**
     * Gets the number of warnings encountered in the content.
     *
     * @return the number of warnings
     */
    public int getNumWarnings() {

        return this.numWarnings;
    }

    /**
     * Called when an action is invoked.
     *
     * @param actionEvent the action event
     */
    @Override
    public void handle(final MouseEvent actionEvent) {

        final Object source = actionEvent.getSource();

        if (source instanceof final Hyperlink link) {
            final Object userData = link.getUserData();

            // We use "this" as a sentinel object to indicate no preview is currently being shown - this could be
            // replaced by a node with a message like "click an object to preview it here..."

            if (this.currentPreview != this) {
                this.previewPane.setCenter(null);
                this.currentPreview = this;
            }

            if (userData instanceof final File userFile) {
                previewFile(userFile);
            }
        }
    }

    /**
     * Attempts to display a preview of a file.
     *
     * @param file the user file
     */
    private void previewFile(final File file) {
        final String name = file.getName();

        Node previewNode = null;

        if (name.endsWith(AppConstants.TXT_EXT) || name.endsWith(AppConstants.VTT_EXT)) {
            final String text = FileLoader.loadFileAsString(file, false);
            final TextArea area = new TextArea(text);
            area.setWrapText(true);
            previewNode = area;
        } else if (name.endsWith(AppConstants.PNG_EXT) || name.endsWith(AppConstants.JPG_EXT) || name.endsWith(
                AppConstants.JPEG_EXT)) {
            final BufferedImage image = FileLoader.loadFileAsImage(file, false);
            if (image != null) {
                final int w = image.getWidth();
                final int h = image.getHeight();
                final WritableImage writable = new WritableImage(w, h);
                final WritableImage fxImage = SwingFXUtils.toFXImage(image, writable);
                previewNode = new ImageView(fxImage);
            }
        } else if (name.endsWith(AppConstants.MP4_EXT)) {
            final URI uri = file.toURI();
            final String urlStr = uri.toString();
            final Media media = new Media(urlStr);

            final MediaPlayer player = new MediaPlayer(media);
            player.setAutoPlay(true);

            final MediaView viewer = new MediaView(player);

            viewer.setFitWidth(640.0);
            viewer.setPreserveRatio(true);

            previewNode = viewer;
        }

        if (previewNode != null) {
            this.previewPane.setCenter(previewNode);
            this.currentPreview = previewNode;
        }
    }

    /**
     * A handler for action events that attempts to open a file passed as user data in the activating control.
     */
    static class ActionHandler implements EventHandler<ActionEvent> {

        /** The owning application. */
        private final CourseBuilder owner;

        /**
         * Constructs a new {@code ActionHandler}.
         *
         * @param theOwner the owning application
         */
        ActionHandler(final CourseBuilder theOwner) {
            this.owner = theOwner;
        }

        /**
         * Called when an action is invoked.
         *
         * @param actionEvent the action event
         */
        @Override
        public void handle(final ActionEvent actionEvent) {

            final Object source = actionEvent.getSource();

            if (source instanceof final Button button) {
                final Object userData = button.getUserData();

                if (userData instanceof final File userFile) {
                    final URI uri = userFile.toURI();
                    final String uriString = uri.toString();
                    final HostServices hostServices = this.owner.getHostServices();
                    hostServices.showDocument(uriString);
                }
            }
        }
    }
}
