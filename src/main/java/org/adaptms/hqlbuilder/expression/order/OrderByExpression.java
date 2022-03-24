package org.adaptms.hqlbuilder.expression.order;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

import java.util.Locale;

/**
 * @author ppolyakov at 24.03.2022 23:00
 */
public class OrderByExpression extends AbstractExpression {

    @Getter( AccessLevel.PROTECTED ) private final String path;
    @Getter( AccessLevel.PROTECTED ) private final QueryOrderDirection direction;

    public OrderByExpression( @NonNull String path, @NonNull QueryOrderDirection direction ) {
        super( ExpressionType.ORDER_BY );

        this.path = path;
        this.direction = direction;
    }

    @Override
    public String build() {
        return getPath() + " " + getDirection().name().toLowerCase( Locale.ROOT );
    }
}
