package org.adaptms.hqlbuilder.expression;

import org.adaptms.hqlbuilder.IBuildable;

/**
 * Parent for all expressions
 *
 * @author ppolyakov at 24.03.2022 16:31
 */
public abstract class AbstractExpression implements IBuildable {

    private final ExpressionType type;

    /**
     * Main constructor
     * @param type specifies the type of expression
     */
    protected AbstractExpression( ExpressionType type ) {
        if ( type == null ) throw new NullPointerException( "Expression type may not be null." );
        this.type = type;
    }

    protected void validateArgs( Object... args ) {
        if ( getType().getNumberOfArguments() != 0 && args.length != getType().getNumberOfArguments() )
            throw new IllegalArgumentException( "Wrong number of arguments, expected: " + getType().getNumberOfArguments() + ", got: " + args.length );
    }

    public ExpressionType getType() {
        return type;
    }
}
