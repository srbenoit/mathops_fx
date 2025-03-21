package dev.mathops.fx.expr;


/**
 * Decorations that can be drawn to indicate errors (invalid nodes or error results) in an expression.
 */
public enum EExpressionErrorDecoration {

    /** Show invalid nodes with a line below the node in the error color. */
    INVALID_NODE_UNDERLINE,

    /** Show invalid nodes with a dotted line below the node in the error color. */
    INVALID_NODE_DOTTED_UNDERLINE,

    /** Show invalid nodes with a wavy line below the node in the error color. */
    INVALID_NODE_WAVY_UNDERLINE,

    /** Show invalid nodes in the error color. */
    INVALID_NODE_ERROR_COLOR,
}