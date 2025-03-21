package dev.mathops.fx.adm;

import dev.mathops.commons.file.FileLoader;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * A JavaFX version of the administrative application.
 */
public class Admin extends Application {

    /** The stage. */
    private Stage stage;

    /** The root pane. */
    private BorderPane root = null;

    /**
     * Constructs a new {@code Admin}.
     */
    public Admin() {

        super();
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

        theStage.show();
    }

    /**
     * Adds an icon to the stage.
     *
     * @param name  the icon filename
     * @param stage the stage
     */
    private static void addIcon(final String name, final Stage stage) {

        final byte[] imgBytes = FileLoader.loadFileAsBytes(Admin.class, name, true);

        if (imgBytes != null) {
            final Image image = new Image(new ByteArrayInputStream(imgBytes));
            final ObservableList<Image> icons = stage.getIcons();
            icons.add(image);
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