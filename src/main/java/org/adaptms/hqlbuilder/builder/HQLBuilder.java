package org.adaptms.hqlbuilder.builder;

import lombok.*;
import org.adaptms.hqlbuilder.IBuildable;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.column.ColumnExpression;
import org.adaptms.hqlbuilder.expression.column.ColumnExpressionType;
import org.adaptms.hqlbuilder.expression.group.GroupByExpression;
import org.adaptms.hqlbuilder.expression.join.JoinExpression;
import org.adaptms.hqlbuilder.expression.join.JoinType;
import org.adaptms.hqlbuilder.expression.order.OrderByExpression;
import org.adaptms.hqlbuilder.expression.order.QueryOrderDirection;
import org.adaptms.hqlbuilder.expression.set.SetExpression;
import org.adaptms.hqlbuilder.expression.where.CommonWhereExpression;
import org.adaptms.hqlbuilder.expression.where.WhereExpression;
import org.adaptms.hqlbuilder.property.EntityPath;

import javax.persistence.Entity;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ppolyakov at 24.03.2022 16:07
 */
@NoArgsConstructor
public class HQLBuilder implements IBuildable {
    private static final String CLASS_ERROR_MESSAGE_START = "Class \"";

    @Getter @Setter( AccessLevel.PROTECTED ) private String internalUUID; // if query without alias

    @Getter @Setter( AccessLevel.PROTECTED ) private String rootEntityClass;
    @Getter @Setter( AccessLevel.PROTECTED ) private String rootEntityAlias;
    @Getter @Setter( AccessLevel.PROTECTED ) private BuilderMode mode;

    @Getter( AccessLevel.PROTECTED ) @Setter( AccessLevel.PROTECTED ) private Set<String> uniqueColumns;
    @Getter( AccessLevel.PROTECTED ) @Setter( AccessLevel.PROTECTED ) private List<ColumnExpression> columns;
    @Getter( AccessLevel.PROTECTED ) @Setter( AccessLevel.PROTECTED ) private List<JoinExpression> joinExpressions;
    @Getter( AccessLevel.PROTECTED ) @Setter( AccessLevel.PROTECTED ) private List<SetExpression> setExpressions;
    @Getter( AccessLevel.PROTECTED ) @Setter( AccessLevel.PROTECTED ) private List<WhereExpression> whereExpressions;
    @Getter( AccessLevel.PROTECTED ) @Setter( AccessLevel.PROTECTED ) private List<OrderByExpression> orderByExpressions;
    @Getter( AccessLevel.PROTECTED ) @Setter( AccessLevel.PROTECTED ) private List<GroupByExpression> groupByExpressions;

    @Getter @Setter( AccessLevel.PROTECTED ) private Map<String, Object> variables = new HashMap<>();

    public HQLBuilder( @NonNull Class<?> rootEntityClass, String rootEntityAlias, @NonNull BuilderMode mode ) {
        init( rootEntityClass, rootEntityAlias, mode );
    }

    public HQLBuilder( @NonNull String rootEntityClass, String rootEntityAlias, @NonNull BuilderMode mode ) {
        try {
            init( Class.forName( rootEntityClass ), rootEntityAlias, mode );
        } catch ( ClassNotFoundException cnfe ) {
            throw new IllegalStateException( CLASS_ERROR_MESSAGE_START + rootEntityClass + "\" doesn't exist." );
        }
    }

    private void init( @NonNull Class<?> rootEntityClass, String rootEntityAlias, @NonNull BuilderMode mode ) {
        if ( !rootEntityClass.isAnnotationPresent( Entity.class ) )
            throw new IllegalStateException( CLASS_ERROR_MESSAGE_START + rootEntityClass.getCanonicalName() + "\" is not an Entity. You need to annotate it with javax.persistence.Entity." );

        this.rootEntityClass = rootEntityClass.getCanonicalName();

        if ( mode.isNeedsAlias() && ( rootEntityAlias == null || rootEntityAlias.isEmpty() ) )
            throw new IllegalStateException( mode.name() + " query required alias. Specify one." );

        this.rootEntityAlias = rootEntityAlias;
        this.mode = mode;

        if ( !this.mode.isNeedsAlias() ) internalUUID = UUID.randomUUID().toString();
    }

    /**
     * Add plain column to select list
     * @param property QueryProperty object for property
     * @return current builder
     */
    public HQLBuilder column( EntityPath property ) {
        return column( property, ColumnExpressionType.DEFAULT );
    }

    /**
     * Add plain column to select list
     * @param property full property path as string
     * @return current builder
     */
    public HQLBuilder column( String property ) {
        return column( property, ColumnExpressionType.DEFAULT );
    }

    /**
     * Add any type of column to select list
     * @param property QueryProperty object for property
     * @param expressionType type of expression
     * @return current builder
     */
    public HQLBuilder column( EntityPath property, ColumnExpressionType expressionType ) {
        return column( property.getPath(), expressionType );
    }

    /**
     * Add plain column to select list
     * @param property full property path as string
     * @param expressionType type of expression
     * @return current builder
     */
    public HQLBuilder column( String property, ColumnExpressionType expressionType ) {
        if ( uniqueColumns == null ) uniqueColumns = new HashSet<>();
        if ( columns == null ) columns = new ArrayList<>();
        uniqueColumns.add( property );
        columns.add( new ColumnExpression( property, expressionType ) );
        return this;
    }

    /**
     * Add join with full control
     * @param type type of join
     * @param joinEntityClass class of entity to join
     * @param joinEntityAlias alias of joined entity
     * @param withExpression expression to join on
     * @return current builder
     */
    public HQLBuilder join( @NonNull JoinType type, @NonNull Class<?> joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
        addJoin( type, joinEntityClass, joinEntityAlias, withExpression );
        return this;
    }

    /**
     * Add join with full control
     * @param type type of join
     * @param joinEntityClass class canonical name of entity to join
     * @param joinEntityAlias alias of joined entity
     * @param withExpression expression to join on
     * @return current builder
     * @throws IllegalStateException if class doesn't exist
     */
    public HQLBuilder join( @NonNull JoinType type, @NonNull String joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
        try {
            addJoin( type, Class.forName( joinEntityClass ), joinEntityAlias, withExpression );
        } catch ( ClassNotFoundException cnfe ) {
            throw new IllegalStateException( CLASS_ERROR_MESSAGE_START + rootEntityClass + "\" in join doesn't exist." );
        }
        return this;
    }

    /**
     * Add join (internal method)
     * @param type type of join
     * @param joinEntityClass class canonical name of entity to join
     * @param joinEntityAlias alias of joined entity
     * @param withExpression expression to join on
     */
    protected void addJoin( @NonNull JoinType type, @NonNull Class<?> joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
        if ( joinExpressions == null ) joinExpressions = new ArrayList<>();
        JoinExpression expression = new JoinExpression( type, joinEntityClass.getCanonicalName(), joinEntityAlias, withExpression );
        expression.init( this );
        joinExpressions.add( expression );
    }

    /**
     * Add SET expression for update or insert query
     * @param path property path (without alias)
     * @param value value to set
     * @return current builder
     */
    public HQLBuilder set( EntityPath path, Object value ) {
        if ( setExpressions == null ) setExpressions = new ArrayList<>();
        SetExpression expression = new SetExpression( path.getPath(), value );
        expression.init( this );
        setExpressions.add( expression );
        return this;
    }

    /**
     * Add WHERE clause
     * @param expression WITH expression
     * @return current builder
     */
    public HQLBuilder where( CommonWhereExpression expression ) {
        if ( whereExpressions == null ) whereExpressions = new ArrayList<>();
        whereExpressions.add( new WhereExpression( expression, this ) );
        return this;
    }

    /**
     * Add a GROUP BY clause
     * @param path path to prop
     * @return current builder
     */
    public HQLBuilder groupBy( EntityPath path ) {
        if ( groupByExpressions == null ) groupByExpressions = new ArrayList<>();
        String pathStr = path.getPath();
        if ( !uniqueColumns.contains( pathStr ) )
            throw new IllegalArgumentException( "You must add a column for a \"" + pathStr + "\" GROUP BY clause." );
        groupByExpressions.add( new GroupByExpression( pathStr ) );
        return this;
    }

    /**
     * Add ORDER BY clause (shorthand for ASC)
     * @param path property for order
     * @return current builder
     */
    public HQLBuilder orderBy( EntityPath path ) {
        return orderBy( path, QueryOrderDirection.ASC );
    }

    /**
     * Add ORDER BY clause
     * @param path property for order
     * @param direction order direction
     * @return current builder
     */
    public HQLBuilder orderBy( EntityPath path, QueryOrderDirection direction ) {
        if ( orderByExpressions == null ) orderByExpressions = new ArrayList<>();
        orderByExpressions.add( new OrderByExpression( path.getPath(), direction ) );
        return this;
    }

    /**
     * Add variable to this query and assign identifier
     * @param variable object to add as variable
     * @return assigned identifier
     */
    public String addVariable( Object variable ) {
        String varName = "var_" + ( getMode().isNeedsAlias() ? getRootEntityAlias() : internalUUID ) + getVariables().size();
        getVariables().put( varName, variable );
        return varName;
    }

    public String build() {
        // start of query - select from / update / insert into etc. and entity class name
        StringBuilder builder = new StringBuilder( getMode().name().toLowerCase( Locale.ROOT ) ).append( " " );

        // set columns that we want to select
        if ( getMode().isNeedsAlias() && null != columns && !columns.isEmpty() ) {
                builder.append( columns.stream().map( ColumnExpression::build ).collect( Collectors.joining( ", " ) ) ).append( " " );
        } else if ( getMode().isNeedsAlias() ) {
            builder.append( getRootEntityAlias() ).append( " " );
        }

        // append additional keyword after columns if needed
        if ( getMode().getAdditionalKeyword() != null )
            builder.append( getMode().getAdditionalKeyword() ).append( " " );

        builder.append( getRootEntityClass() ); // full entity name

        // necessary for select query. Otherwise, will not append
        if ( mode.isNeedsAlias() ) builder.append( " " ).append( getRootEntityAlias() );

        // join clauses
        if ( null != joinExpressions && !joinExpressions.isEmpty() ) {
            builder
                    .append( " " )
                    .append( joinExpressions.stream().map( AbstractExpression::build ).collect( Collectors.joining( " " ) ) );
        }

        // set expressions
        if ( null != setExpressions && !setExpressions.isEmpty() ) {
            builder
                    .append( " set " )
                    .append( setExpressions.stream().map( AbstractExpression::build ).collect( Collectors.joining( ", " ) ) );
        }

        // where expressions
        if ( null != whereExpressions && !whereExpressions.isEmpty() ) {
            builder
                    .append( " where " )
                    .append( whereExpressions.stream().map( AbstractExpression::build ).collect( Collectors.joining( " and " ) ) );
        }

        // group by
        if ( null != groupByExpressions && !groupByExpressions.isEmpty() ) {
            builder
                    .append( " group by " )
                    .append( groupByExpressions.stream().map( AbstractExpression::build ).collect( Collectors.joining( ", " ) ) );
        }

        // order
        if ( null != orderByExpressions && !orderByExpressions.isEmpty() ) {
            builder
                    .append( " order by " )
                    .append( orderByExpressions.stream().map( AbstractExpression::build ).collect( Collectors.joining( ", " ) ) );
        }

        return builder.toString().trim();
    }

    /**
     * Remove all column definitions (e.g. for counting)
     */
    public void clearColumns() {
        uniqueColumns = null;
        columns = null;
    }

    public static HQLBuilder select( @NonNull Class<?> rootEntityClass, @NonNull String rootEntityAlias ) {
        return new HQLBuilder( rootEntityClass, rootEntityAlias, BuilderMode.SELECT );
    }

    public static HQLBuilder select( @NonNull String rootEntityClass, @NonNull String rootEntityAlias ) {
        return new HQLBuilder( rootEntityClass, rootEntityAlias, BuilderMode.SELECT );
    }

    public static HQLBuilder insert( @NonNull Class<?> rootEntityClass ) {
        throw new UnsupportedOperationException( "Due to HQL limitations, INSERT queries are not implemented." );
    }

    public static HQLBuilder insert( @NonNull String rootEntityClass ) {
        throw new UnsupportedOperationException( "Due to HQL limitations, INSERT queries are not implemented." );
    }

    public static HQLBuilder update( @NonNull Class<?> rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.UPDATE );
    }

    public static HQLBuilder update( @NonNull String rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.UPDATE );
    }

    public static HQLBuilder delete( @NonNull Class<?> rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.DELETE );
    }

    public static HQLBuilder delete( @NonNull String rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.DELETE );
    }

    public static HQLBuilder clone( HQLBuilder that ) {
        HQLBuilder builder = new HQLBuilder();
        builder.setMode( that.getMode() );
        builder.setRootEntityClass( that.getRootEntityClass() );
        builder.setRootEntityAlias( that.getRootEntityAlias() );

        builder.setUniqueColumns( that.getUniqueColumns() );
        builder.setColumns( that.getColumns() );
        builder.setJoinExpressions( that.getJoinExpressions() );
        builder.setSetExpressions( that.getSetExpressions() );
        builder.setWhereExpressions( that.getWhereExpressions() );
        builder.setOrderByExpressions( that.getOrderByExpressions() );
        builder.setGroupByExpressions( that.getGroupByExpressions() );

        builder.setVariables( that.getVariables() );

        return builder;
    }
}
