package dev.mathops.fx.expr;

/**
 * Possible outcomes of an attempt to add a symbol.
 */
public enum EAddSymbolOutcome {

    /** The symbol was accepted and added to the node. */
    ACCEPTED,

    /** The symbol was rejected and not added to the node. */
    REJECTED,

    /**
     * The symbol was rejected but the target node should be converted to a {@code NodeInteger} and the symbol added
     * to the new node.
     */
    CONVERT_TO_INTEGER,

    /**
     * The symbol was rejected but the target node should be converted to a {@code NodeRealDecimal} and the symbol added
     * to the new node.
     */
    CONVERT_TO_REAL_DECIMAL,

    /**
     * The symbol was rejected but the target node should be converted to a {@code NodeRational} and the symbol added
     * to the new node.
     */
    CONVERT_TO_RATIONAL,
}
