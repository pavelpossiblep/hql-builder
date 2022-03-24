package org.adaptms.hqlbuilder.expression.where;

import lombok.Getter;
import lombok.NonNull;
import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

/**
 * @author ppolyakov at 24.03.2022 17:25
 */
public class WhereExpression extends AbstractExpression {

    @Getter private final CommonWhereExpression expression;

    public WhereExpression( @NonNull CommonWhereExpression expression, @NonNull HQLBuilder builder ) {
        super( ExpressionType.WHERE );
        this.expression = expression;
        this.expression.init( builder );
    }

    @Override
    public String build() {
        return getExpression().build();
    }
}
