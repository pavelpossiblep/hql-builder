package org.adaptms.hqlbuilder.expression.order;

import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

import java.util.Locale;

/**
 * @author ppolyakov at 24.03.2022 23:00
 */
public class OrderByExpression extends AbstractExpression {

    private final String path;
    private final QueryOrderDirection direction;

    public OrderByExpression( String path, QueryOrderDirection direction ) {
        super( ExpressionType.ORDER_BY );

        if ( null == path || path.isEmpty() ) throw new IllegalArgumentException( "Path may not be empty." );
        if ( null == direction ) throw new IllegalArgumentException( "QueryOrderDirection may not be null." );

        this.path = path;
        this.direction = direction;
    }

    @Override
    public String build() {
        return getPath() + " " + getDirection().name().toLowerCase( Locale.ROOT );
    }

    protected String getPath() {
        return path;
    }

    protected QueryOrderDirection getDirection() {
        return direction;
    }
}
