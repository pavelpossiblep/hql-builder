package org.adaptms.hqlbuilder.expression.column;

import lombok.Getter;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

import java.text.MessageFormat;

/**
 * @author ppolyakov at 24.03.2022 16:46
 */
public class ColumnExpression extends AbstractExpression {

    @Getter private final String path;
    @Getter private final ColumnExpressionType columnExpressionType;

    public ColumnExpression( String path, ColumnExpressionType type ) {
        super( ExpressionType.COLUMN );
        this.path = path;
        this.columnExpressionType = type;
    }

    @Override
    public String build() {
        return MessageFormat.format( getColumnExpressionType().getQuery(), getPath() );
    }
}
