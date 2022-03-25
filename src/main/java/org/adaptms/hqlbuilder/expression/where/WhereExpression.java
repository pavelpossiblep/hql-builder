package org.adaptms.hqlbuilder.expression.where;

import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

/**
 * @author ppolyakov at 24.03.2022 17:25
 */
public class WhereExpression extends AbstractExpression {

    private final CommonWhereExpression expression;

    public WhereExpression( CommonWhereExpression expression, HQLBuilder builder ) {
        super( ExpressionType.WHERE );

        if ( null == expression ) throw new IllegalArgumentException( "CommonWhereExpression may not be null." );
        if ( null == builder ) throw new IllegalArgumentException( "HQLBuilder may not be null." );

        this.expression = expression;
        this.expression.init( builder );
    }

    @Override
    public String build() {
        return getExpression().build();
    }

    public CommonWhereExpression getExpression() {
        return expression;
    }
}
