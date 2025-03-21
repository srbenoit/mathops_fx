package dev.mathops.fx.expr.node;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.fx.expr.AbstractExpressionNode;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;

public class NodeTestHarness extends Application {

    /**
     * Constructs a new {@code TestHarness}.
     */
    public NodeTestHarness() {

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
        root.setPadding(new Insets(10.0));
        final Background bg = Background.fill(Color.WHITE);
        root.setBackground(bg);

        final Scene scene = new Scene(root);

        final AnchorPane anchor = new AnchorPane();
        anchor.setPrefSize(200.0, 200.0);
        root.setCenter(anchor);

        final ExpressionRenderingContext context = new ExpressionRenderingContext();
        context.fontSize = 30.0;
        context.lineWidth = 1.0;
        context.decorationPaint = Color.BLACK;
        context.functionNamePaint = Color.BLACK;
        context.cursorWidth = 2.0;

        final byte[] romanFontBytes = FileLoader.loadFileAsBytes(AbstractExpressionNode.class,
                "STIXTwoText-Regular.otf", true);
        if (romanFontBytes == null) {
            Log.info("Failed to load file 'lmmath.otf'");
            context.romanFont = Font.font("System", FontPosture.REGULAR, 30.0);
        } else {
            context.romanFont = Font.loadFont(new ByteArrayInputStream(romanFontBytes), 30.0);
            if (context.romanFont == null) {
                Log.info("Failed to load create font from 'lmmath.otf'");
                context.romanFont = Font.font("System", FontPosture.REGULAR, 30.0);
            }
        }

        final byte[] italicFontBytes = FileLoader.loadFileAsBytes(AbstractExpressionNode.class,
                "STIXTwoText-Italic.otf", true);
        if (italicFontBytes == null) {
            Log.info("Failed to load file 'lmmath.otf'");
            context.italicFont = Font.font("System", FontPosture.ITALIC, 30.0);
        } else {
            context.italicFont = Font.loadFont(new ByteArrayInputStream(italicFontBytes), 30.0);
            if (context.italicFont == null) {
                Log.info("Failed to load create font from 'lmmath.otf'");
                context.italicFont = Font.font("System", FontPosture.ITALIC, 30.0);
            }
        }

        final EmptyNode empty = new EmptyNode(context);
        final FunctionNode sine = new FunctionNode(context, "sin", empty);
//        final FunctionNode cosine = new FunctionNode(context, "cos", empty);
//        final RootNode radical = new RootNode(context, sine, cosine);

        sine.setCursorPosition(2);

        AnchorPane.setLeftAnchor(sine, 0.0);
        AnchorPane.setTopAnchor(sine, 0.0);
        final ObservableList<Node> anchorChildren = anchor.getChildren();
        anchorChildren.add(sine);

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
