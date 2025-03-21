package dev.mathops.fx.expr;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TestHarness extends Application {

    /**
     * Constructs a new {@code TestHarness}.
     */
    public TestHarness() {

        super();
    }

    /**
     * Starts the application.
     *
     * @param theStage the main stage
     */
    @Override
    public void start(final Stage theStage) {

        final BorderPane root = new BorderPane();
        final Scene scene = new Scene(root);

        scene.getStylesheets().add("/dev/mathops/fx/expr/expr.css");

        final ExpressionEditor editor = new ExpressionEditor();
        root.setCenter(editor);

        theStage.setScene(scene);
        theStage.sizeToScene();
        theStage.show();
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
