package org.adaptms.hqlbuilder.expression.where;

import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author ppolyakov at 24.03.2022 17:30
 */
public class AndExpression extends CommonWhereExpression {

    private final CommonWhereExpression[] expressions;

    public AndExpression( CommonWhereExpression... expressions ) {
        super( ExpressionType.AND );

        if ( expressions == null || expressions.length < 2 )
            throw new IllegalArgumentException( "AND clause must have at least two arguments." );

        this.expressions = expressions;
    }

    @Override
    public void init( HQLBuilder builder ) {
        for ( CommonWhereExpression expression : this.expressions ) {
            expression.init( builder );
        }
    }

    @Override
    public String build() {
        return "(" + Arrays.stream( getExpressions() ).map( AbstractExpression::build ).collect( Collectors.joining( " and " ) ) + ")";
    }

    protected CommonWhereExpression[] getExpressions() {
        return expressions;
    }
}
