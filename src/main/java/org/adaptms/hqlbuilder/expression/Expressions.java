package org.adaptms.hqlbuilder.expression;

import org.adaptms.hqlbuilder.expression.where.AndExpression;
import org.adaptms.hqlbuilder.expression.where.CommonWhereExpression;
import org.adaptms.hqlbuilder.expression.where.OrExpression;

/**
 * @author ppolyakov at 24.03.2022 18:09
 */
public interface Expressions {

    static AndExpression and( CommonWhereExpression... expressions ) {
        return new AndExpression( expressions );
    }

    static OrExpression or( CommonWhereExpression... expressions ) {
        return new OrExpression( expressions );
    }

    static CommonWhereExpression exists( Object first ) {
        return new CommonWhereExpression( ExpressionType.EXISTS, first );
    }

    static CommonWhereExpression notExists( Object first ) {
        return new CommonWhereExpression( ExpressionType.NOT_EXISTS, first );
    }

    static CommonWhereExpression isNull( Object first ) {
        return new CommonWhereExpression( ExpressionType.IS_NULL, first );
    }

    static CommonWhereExpression isNotNull( Object first ) {
        return new CommonWhereExpression( ExpressionType.IS_NOT_NULL, first );
    }

    static CommonWhereExpression eq( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.EQ, first, second );
    }

    static CommonWhereExpression notEq( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.NOT_EQ, first, second );
    }

    static CommonWhereExpression gt( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.GT, first, second );
    }

    static CommonWhereExpression lt( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.LT, first, second );
    }

    static CommonWhereExpression ge( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.GE, first, second );
    }

    static CommonWhereExpression le( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.LE, first, second );
    }

    static CommonWhereExpression in( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.IN, first, second );
    }

    static CommonWhereExpression notIn( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.NOT_IN, first, second );
    }

    static CommonWhereExpression like( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.LIKE, first, second );
    }

    static CommonWhereExpression notLike( Object first, Object second ) {
        return new CommonWhereExpression( ExpressionType.NOT_LIKE, first, second );
    }

    static CommonWhereExpression between( Object first, Object second, Object third ) {
        return new CommonWhereExpression( ExpressionType.BETWEEN, first, second, third );
    }

    /**
     * Wrap string with "%" for like for both sides
     * @param input source string to wrap
     * @return wrapped string
     */
    static String wrapLike( String input ) {
        return wrapLike( input, true, true );
    }

    /**
     * Wrap string with "%" for like
     * @param input source string to wrap
     * @param start do wrap from the beginning ({@code %input})
     * @param end do wrap from the end ({@code input%})
     * @return string wrapped from the directions specified
     */
    static String wrapLike( String input, boolean start, boolean end ) {
        return ( start ? "%" : "" ) + input + ( end ? "%" : "" );
    }
}
