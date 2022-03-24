package org.adaptms.hqlbuilder.expression;

/**
 * @author ppolyakov at 24.03.2022 16:31
 */
public enum ExpressionType {
    AND( 0, "" ),
    OR( 0, "" ),
    WHERE( 0, "" ),
    JOIN( 0, "" ),
    ORDER_BY( 0, "" ),
    GROUP_BY( 0, "" ),
    COLUMN( 0, "{0}" ),

    EXISTS( 1, "exists {0}" ),
    NOT_EXISTS( 1, "not exists {0}" ),
    IS_NULL( 1, "{0} is null" ),
    IS_NOT_NULL( 1, "{0} is not null" ),

    SET( 2, "{0} = {1}" ),
    EQ( 2, "{0} = {1}" ),
    NOT_EQ( 2,"{0} != {1}}" ),
    GT( 2, "{0} > {1}" ),
    LT( 2, "{0} < {1}" ),
    GE( 2, "{0} >= {1}" ),
    LE( 2, "{0} <= {1}" ),
    IN( 2, "{0} in {1}" ),
    NOT_IN( 2, "{0} not in {1}" ),
    LIKE( 2, "{0} like {1}" ),
    NOT_LIKE( 2, "{0} not like {1}" ),

    BETWEEN( 3, "({0} between {1} and {2})" );

    private final int numberOfArguments;
    private final String template;

    ExpressionType( int numberOfArguments, String template ) {
        this.numberOfArguments = numberOfArguments;
        this.template = template;
    }

    public int getNumberOfArguments() {
        return numberOfArguments;
    }

    public String getTemplate() {
        return template;
    }
}
