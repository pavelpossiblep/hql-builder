package org.adaptms.hqlbuilder.expression.set;

import lombok.AccessLevel;
import lombok.Getter;
import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.ExpressionType;
import org.adaptms.hqlbuilder.expression.where.CommonWhereExpression;

import java.text.MessageFormat;

/**
 * @author ppolyakov at 24.03.2022 23:33
 */
public class SetExpression extends CommonWhereExpression {

    @Getter private final String path;
    @Getter( AccessLevel.PROTECTED ) private final Object value;
    @Getter( AccessLevel.PROTECTED ) private String variableName;

    public SetExpression( String path, Object value ) {
        super( ExpressionType.SET );

        this.path = path;
        this.value = value;
    }

    @Override
    public void init( HQLBuilder builder ) {
        this.variableName = processVariable( getValue(), builder );
    }

    @Override
    public String build() {
        return MessageFormat.format( getType().getTemplate(), path, variableName );
    }
}
