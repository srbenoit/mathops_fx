package dev.mathops.fx.coursebuilder;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A test application.
 */
public class CourseBuilder extends Application implements EventHandler<ActionEvent> {

    /** A user data object to indicate a "Deploy to Media Server" action. */
    private static final String DEPLOY_MEDIA_CMD = "DEPLOY_MEDIA";

    /** A user data object to indicate a "Deploy to Web Server" action. */
    private static final String DEPLOY_WEB_CMD = "DEPLOY_WEB";

    /** The default name of a OneDrive share containing media files. */
    private static final String ONEDRIVE_DIR = "OneDrive - Colostate";

    /** The default name of a folder within the OneDrive share containing media files. */
    private static final String MEDIA_DIR = "Precalculus";

    /** A map from topic module directory to the pane that presents that topic. */
    private final Map<File, TopicModulePane> topicModulePanes;

    /** The stage. */
    private Stage stage;

    /** The course media directory. */
    private File courseMediaDir;

    /** The root pane. */
    private BorderPane root = null;

    /** The current topic module pane. */
    private TopicModulePane currentTopicModule = null;

    /** A progress bar. */
    private ProgressBar progressBar;

    /** A progress status label. */
    private Label progressStatusLabel;

    /**
     * Constructs a new {@code CourseBuilder}.
     */
    public CourseBuilder() {

        super();

        this.topicModulePanes = new HashMap<>(20);
    }

    /**
     * Starts the application.
     *
     * @param theStage the main stage
     */
    @Override
    public void start(final Stage theStage) {

        this.stage = theStage;

        addIcon("icon24.png", theStage);
        addIcon("icon32.png", theStage);
        addIcon("icon48.png", theStage);

        File dir = null;

        final Screen primary = Screen.getPrimary();
        final Rectangle2D bounds = primary.getVisualBounds();
        final double screenWidth = bounds.getWidth();
        final double screenHeight = bounds.getHeight();
        final double myWidth = Math.min(screenWidth * 0.8, 2000.0);
        final double myHeight = screenHeight * 0.8;

        this.root = new BorderPane();
        final Scene scene = new Scene(this.root, myWidth, myHeight);

        theStage.setScene(scene);
        theStage.sizeToScene();

        final File dir1 = new File("D:/");
        final File dir2 = new File("F:/");
        final File dir3 = new File("C:/Users/benoit/");
        final String homePath = System.getProperty("user.home");
        final File home = new File(homePath);

        final File[] defaultDirectories = {dir1, dir2, dir3, home};

        for (final File testDir : defaultDirectories) {
            final File oneDrive = new File(testDir, ONEDRIVE_DIR);
            if (oneDrive.exists() && oneDrive.isDirectory()) {
                final File media = new File(oneDrive, MEDIA_DIR);
                if (media.exists() && media.isDirectory()) {
                    dir = media;
                    break;
                }
            }
        }
        if (dir == null) {
            dir = home;
        }

        final DirectoryChooser dirChooser = new DirectoryChooser();
        final String selectPrompt = Res.get(Res.SELECT_MEDIA_DIR);
        dirChooser.setTitle(selectPrompt);
        dirChooser.setInitialDirectory(dir);
        this.courseMediaDir = dirChooser.showDialog(theStage);

        if (this.courseMediaDir != null) {
            final Border border = AppUtils.makeStrokeTopBorder();
            this.root.setBorder(border);

            final Menu fileMenu = new Menu("File");
            final ObservableList<MenuItem> fileMenuItems = fileMenu.getItems();

            final MenuItem item1 = new MenuItem("Deploy to Media Server...");
            item1.setOnAction(this);
            item1.setUserData(DEPLOY_MEDIA_CMD);
            final MenuItem item2 = new MenuItem("Deploy to Web Server...");
            item2.setOnAction(this);
            item2.setUserData(DEPLOY_WEB_CMD);
            fileMenuItems.addAll(item1, item2);

            final MenuBar menuBar = new MenuBar(fileMenu);
            this.root.setTop(menuBar);

            final String courseDirPath = this.courseMediaDir.getAbsolutePath();
            final String windowTitle = Res.fmt(Res.TITLE, courseDirPath);
            theStage.setTitle(windowTitle);

            final LeftPane left = new LeftPane(this, this.courseMediaDir, myHeight);
            left.init();
            this.root.setLeft(left);

            final FlowPane bottom = new FlowPane();
            bottom.setPadding(AppConstants.PADDING);
            final ObservableList<Node> bottomChildren = bottom.getChildren();

            this.progressBar = new ProgressBar(0.0);
            final ReadOnlyDoubleProperty rootWidth = this.root.widthProperty();
            final DoubleBinding progressWidth = rootWidth.subtract(LeftPane.PREF_WIDTH + AppConstants.H_GAP);
            this.progressBar.prefWidthProperty().bind(progressWidth);

            this.progressStatusLabel = new Label(CoreConstants.EMPTY);
            this.progressStatusLabel.setPrefWidth(LeftPane.PREF_WIDTH);

            bottomChildren.addAll(this.progressStatusLabel, this.progressBar);
            bottom.setBorder(border);
            this.root.setBottom(bottom);

            theStage.show();

            final TickerService ticker = new TickerService(this);
            ticker.start();
        }
    }

    /**
     * Clears any cached topic module panels so those panels will get rebuilt on next use.
     */
    void clearCache() {

        this.topicModulePanes.clear();
    }

    /**
     * Called when a topic is selected from the left-hand pane.
     *
     * @param topicDir the topic directory; {@code null} if none is selected
     */
    void topicSelected(final File topicDir) {

        if (topicDir == null) {
            this.currentTopicModule = null;
        } else {
            this.currentTopicModule = this.topicModulePanes.computeIfAbsent(topicDir,
                    x -> new TopicModulePane(this, topicDir));
        }

        this.root.setCenter(this.currentTopicModule);
    }

    /**
     * Adds an icon to the stage.
     *
     * @param name  the icon filename
     * @param stage the stage
     */
    private static void addIcon(final String name, final Stage stage) {

        final byte[] imgBytes = FileLoader.loadFileAsBytes(CourseBuilder.class, name, true);

        if (imgBytes != null) {
            final Image image = new Image(new ByteArrayInputStream(imgBytes));
            final ObservableList<Image> icons = stage.getIcons();
            icons.add(image);
        }
    }

    /**
     * Calls "tick" on the current topic module panel, if any.
     */
    void tick() {

        if (this.currentTopicModule != null) {
            this.currentTopicModule.tick();
        }
    }

    /**
     * Called when a menu item is activated.
     *
     * @param actionEvent the action event
     */
    @Override
    public void handle(final ActionEvent actionEvent) {

        final Object source = actionEvent.getSource();
        if (source instanceof final MenuItem menuItem) {
            final Object user = menuItem.getUserData();
            if (DEPLOY_MEDIA_CMD.equals(user)) {
                doDeployMedia();
            } else if (DEPLOY_WEB_CMD.equals(user)) {
                doDeployWeb();
            }
        }
    }

    /**
     * Deploys final video and PDF objects to a directory that can then be copied to the media server.  This copies the
     * following files:
     * <ul>
     *     <li>All "final.mp4", "final.vtt", "final.txt", and "example.pdf" files for all examples.</li>
     *     <li>All "final.mp4", "final.vtt", "final.txt", and "notes.pdf" files for all lessons.</li>
     *     <li>All "final.mp4", "final.vtt", "final.txt", and "context_##.pdf", "worksheet_##.pdf, "practice_##.pdf,
     *     "answers_##.pdf, and "solutions_##.pdf files for all handouts.</li>
     * </ul>
     * The destination directory structure will match the source directory structure exactly (except that any
     * directories that do not contain one of the above files will not be copied).
     */
    private void doDeployMedia() {

        final DirectoryChooser dirChooser = new DirectoryChooser();
        final String selectPrompt = Res.get(Res.SELECT_DEPLOY_DIR);
        dirChooser.setTitle(selectPrompt);

        final String homeDirStr = System.getProperty("user.home");
        if (homeDirStr != null) {
            final File homeDir = new File(homeDirStr);
            dirChooser.setInitialDirectory(homeDir);
        }

        final File deployTargetDir = dirChooser.showDialog(this.stage);

        if (deployTargetDir != null && (deployTargetDir.exists() || deployTargetDir.mkdirs())
            && deployTargetDir.isDirectory()) {

            final String sourcePath = this.courseMediaDir.getAbsolutePath();
            final String targetPath = deployTargetDir.getAbsolutePath();
            if (targetPath.startsWith(sourcePath)) {
                // TODO: Show an error: "Target path may not be a subdirectory of source path"
            } else {
                final HostServices hostServices = getHostServices();
                final Runnable task = new DeployMediaTask(this.progressBar, this.progressStatusLabel,
                        this.courseMediaDir, deployTargetDir, hostServices);
                final Thread taskThread = new Thread(task);
                taskThread.start();
            }
        }
    }

    /**
     * Deploys final video and PDF objects to a directory that can then be copied to the media server.  This copies the
     * following files:
     * <ul>
     *     <li>All "final.vtt" files for all layers in the directory structure.</li>
     *     <li>All "metadata.json" files for all layers in the directory structure.</li>
     * </ul>
     * The destination directory structure will match the source directory structure exactly (except that any
     * directories that do not contain one of the above files will not be copied).
     */
    private void doDeployWeb() {

        final DirectoryChooser dirChooser = new DirectoryChooser();
        final String selectPrompt = Res.get(Res.SELECT_DEPLOY_DIR);
        dirChooser.setTitle(selectPrompt);

        final String homeDirStr = System.getProperty("user.home");
        if (homeDirStr != null) {
            final File homeDir = new File(homeDirStr);
            dirChooser.setInitialDirectory(homeDir);
        }

        final File deployTargetDir = dirChooser.showDialog(this.stage);

        if (deployTargetDir != null && (deployTargetDir.exists() || deployTargetDir.mkdirs())
            && deployTargetDir.isDirectory()) {

            final String sourcePath = this.courseMediaDir.getAbsolutePath();
            final String targetPath = deployTargetDir.getAbsolutePath();
            if (targetPath.startsWith(sourcePath)) {
                // TODO: Show an error: "Target path may not be a subdirectory of source path"
            } else {
                final HostServices hostServices = getHostServices();
                final Runnable task = new DeployWebTask(this.progressBar, this.progressStatusLabel,
                        this.courseMediaDir, deployTargetDir, hostServices);
                final Thread taskThread = new Thread(task);
                taskThread.start();
            }
        }
    }

    /**
     * Runs the main application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        launch();
    }
}