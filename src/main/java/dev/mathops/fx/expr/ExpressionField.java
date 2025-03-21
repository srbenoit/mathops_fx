package dev.mathops.fx.expr;

import dev.mathops.commons.log.Log;
import dev.mathops.fx.coursebuilder.AppUtils;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

/**
 * A UI representation of an expression.  An expression is presented as a linear flow with "large" constructions like
 * IF-THEN-ELSE, SWITCH, and SPAN presented in popups that can be expanded/collapsed.
 */
public final class ExpressionField extends FlowPane implements EventHandler<KeyEvent> {

    /** The root node in the expression tree (never null). */
    private AbstractExpressionNode root;

    /**
     * A two-int array with the cursor position (in [0]) and selection anchor position (in [1]).  The selection anchor
     * is -1 if there is no selection.
     */
    private final int[] cursor;

    /**
     * Constructs a new {@code ExpressionField}.
     */
    ExpressionField() {

        super();

        setMinHeight(24.0);
        setMinWidth(200.0);

        getStyleClass().add("exprfield");

        final Border border = AppUtils.makeStrokeBorder();
        setBorder(border);

        final Platform.Preferences preferences = Platform.getPreferences();
        final ColorScheme scheme = preferences.getColorScheme();

        final Color bgColor = scheme == ColorScheme.DARK ? Color.BLACK : Color.WHITE;
        final BackgroundFill bgFill = new BackgroundFill(bgColor, null, null);
        final Background bg = new Background(bgFill);
        setBackground(bg);

        setFocusTraversable(true);

        this.root = new NodeEmpty();
        this.cursor = new int[]{0, -1};

        this.root.setCaretPosition(0);

        final Node rootNode = this.root.getNode();
        final ObservableList<Node> children = getChildren();
        children.add(rootNode);

        setOnKeyTyped(this);
    }

    /**
     * Called when a key is pressed.
     *
     * @param keyEvent the key event
     */
    @Override
    public void handle(final KeyEvent keyEvent) {

        final String charStr = keyEvent.getCharacter();
        if (charStr != null && !charStr.isEmpty()) {
            final int codePoint = (int) charStr.charAt(0);

            // if (this.cursor[1] != -1) {
            //    how to delete the selection if the key code is one that would replace content?
            // }

            EAddSymbolOutcome outcome = this.root.handleKey(codePoint, this.cursor[0]);

            Log.info("Outcome is ", outcome);

            if (outcome == EAddSymbolOutcome.CONVERT_TO_INTEGER) {
                // The only node that will ask to switch to Integer on a key is an empty node, so just make a new
                // Integer node with no content
                final Node oldRootNode = this.root.getNode();
                final ObservableList<Node> children = getChildren();
                children.remove(oldRootNode);
                this.root = new NodeInteger();
                final Node newRootNode = this.root.getNode();
                children.add(newRootNode);
                outcome = this.root.handleKey(codePoint, this.cursor[0]);
            } else if (outcome == EAddSymbolOutcome.CONVERT_TO_REAL_DECIMAL) {



            } else if (outcome == EAddSymbolOutcome.CONVERT_TO_RATIONAL) {

            }

            // TODO: Handle outcomes


            if (outcome == EAddSymbolOutcome.ACCEPTED) {
                ++this.cursor[0];
                this.root.setCaretPosition(this.cursor[0]);
            }
        }
    }
}
