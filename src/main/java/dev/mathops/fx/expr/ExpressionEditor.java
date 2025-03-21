package dev.mathops.fx.expr;

import dev.mathops.fx.coursebuilder.AppConstants;
import dev.mathops.fx.coursebuilder.AppUtils;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.Set;

/**
 * An expression editor pane.
 */
public class ExpressionEditor extends BorderPane {

    /**
     * Constructs a new {@code ExpressionEditor}.
     */
    public ExpressionEditor() {

        super();

        setPadding(AppConstants.PADDING);
        setTop(new ExpressionField());

        final FlowPane tiles = new FlowPane();
        tiles.setPadding(AppConstants.BUTTON_ROW_PADDING);
        final ObservableList<Node> gridChildren = tiles.getChildren();
        setCenter(tiles);

        final Button b1 = new Button("TRUE");
        final Button b2 = new Button("FALSE");
        final Button b3 = new Button("Integer");
        final Button b4 = new Button("Real");
        final Button b5 = new Button("Integer Vector");
        final Button b6 = new Button("Real Vector");
        final Button b7 = new Button("String");
        final Button b8 = new Button("Span");

        final Button b11 = new Button("Variable");
        final Button b12 = new Button("Unary Operator");
        final Button b13 = new Button("Binary Operator");
        final Button b14 = new Button("Function");
        final Button b15 = new Button("IF THEN ELSE");
        final Button b16 = new Button("SWITCH");
        final Button b17 = new Button("IS EXACT");
        final Button b18 = new Button("Expression");

        gridChildren.addAll(b1, b2, b3, b4, b5, b6, b7, b8, b11, b12, b13, b14, b15, b16, b17, b18);

        //
        //
        //

        final WebView web = new WebView();
        web.setMinHeight(50.0);
        web.setMaxHeight(50.0);
        web.setMinWidth(200.0);
        web.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
            @Override
            public void onChanged(Change<? extends Node> change) {
                Set<Node> scrolls = web.lookupAll(".scroll-bar");
                for (Node scroll : scrolls) {
                    scroll.setVisible(false);
                }
            }
        });

        final BorderPane bottom = new BorderPane();
        final Border border = AppUtils.makeStrokeBorder();
        bottom.setBorder(border);
        bottom.setCenter(web);

        setBottom(bottom);

        final WebEngine engine = web.getEngine();
        engine.loadContent("""
                <math display="block">
                   <mrow>
                      <mi>x</mi>
                      <mo>=</mo>
                      <mfrac>
                         <mrow>
                            <mo>−</mo>
                            <mi>b </mi>
                            <mo>±</mo>
                            <msqrt>
                               <mrow>
                                  <msup>
                                     <mi>b</mi>
                                     <mn>2</mn>
                                  </msup>
                                  <mo>−</mo>
                                  <mn>4</mn>
                                  <mi>a</mi>
                                  <mi>c</mi>
                               </mrow>
                            </msqrt>
                         </mrow>
                         <mrow>
                            <mn>2</mn>
                            <mi>a</mi>
                         </mrow>
                      </mfrac>
                   </mrow>
                </math>""");

    }

    // TODO: Check this: https://java-no-makanaikata.blogspot.com/2012/10/javafx-webview-size-trick.html

}
