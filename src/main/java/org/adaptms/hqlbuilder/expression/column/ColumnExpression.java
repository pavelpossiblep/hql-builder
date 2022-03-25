package org.adaptms.hqlbuilder.expression.column;

import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

import java.text.MessageFormat;

/**
 * @author ppolyakov at 24.03.2022 16:46
 */
public class ColumnExpression extends AbstractExpression {

    private final String path;
    private final ColumnExpressionType columnExpressionType;

    public ColumnExpression( String path, ColumnExpressionType type ) {
        super( ExpressionType.COLUMN );

        if ( null == path || path.isEmpty() ) throw new IllegalArgumentException( "Path may not be empty." );
        if ( null == type ) throw new IllegalArgumentException( "ColumnExpressionType may not be empty." );

        this.path = path;
        this.columnExpressionType = type;
    }

    @Override
    public String build() {
        return MessageFormat.format( getColumnExpressionType().getQuery(), getPath() );
    }

    public String getPath() {
        return path;
    }

    public ColumnExpressionType getColumnExpressionType() {
        return columnExpressionType;
    }
}
