package org.adaptms.hqlbuilder.expression;

import lombok.Getter;
import org.adaptms.hqlbuilder.IBuildable;

/**
 * Parent for all expressions
 *
 * @author ppolyakov at 24.03.2022 16:31
 */
public abstract class AbstractExpression implements IBuildable {

    @Getter private final ExpressionType type;

    /**
     * Main constructor
     * @param type specifies the type of expression
     */
    protected AbstractExpression( ExpressionType type ) {
        this.type = type;
    }

    protected void validateArgs( Object... args ) {
        if ( getType().getNumberOfArguments() != 0 && args.length != getType().getNumberOfArguments() )
            throw new IllegalArgumentException( "Wrong number of arguments, expected: " + getType().getNumberOfArguments() + ", got: " + args.length );
    }
}
