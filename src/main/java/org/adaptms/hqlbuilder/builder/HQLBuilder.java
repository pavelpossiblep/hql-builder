package org.adaptms.hqlbuilder.builder;

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
public class HQLBuilder implements IBuildable {
    private static final String CLASS_ERROR_MESSAGE_START = "Class \"";

    private String internalUUID; // if query without alias

    private String rootEntityClass;
    private String rootEntityAlias;
    private BuilderMode mode;

    private List<ColumnExpression> columns;
    private List<JoinExpression> joinExpressions;
    private List<SetExpression> setExpressions;
    private List<WhereExpression> whereExpressions;
    private List<OrderByExpression> orderByExpressions;
    private List<GroupByExpression> groupByExpressions;

    private Map<String, Object> variables = new HashMap<>();

    public HQLBuilder() {
    }

    public HQLBuilder( Class<?> rootEntityClass, String rootEntityAlias, BuilderMode mode ) {
        init( rootEntityClass, rootEntityAlias, mode );
    }

    public HQLBuilder( String rootEntityClass, String rootEntityAlias, BuilderMode mode ) {
        try {
            init( Class.forName( rootEntityClass ), rootEntityAlias, mode );
        } catch ( ClassNotFoundException cnfe ) {
            throw new IllegalStateException( CLASS_ERROR_MESSAGE_START + rootEntityClass + "\" doesn't exist." );
        }
    }

    private void init( Class<?> rootEntityClass, String rootEntityAlias, BuilderMode mode ) {
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
        if ( columns == null ) columns = new ArrayList<>();
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
    public HQLBuilder join( JoinType type, Class<?> joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
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
    public HQLBuilder join( JoinType type, String joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
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
    protected void addJoin( JoinType type, Class<?> joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
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
        groupByExpressions.add( new GroupByExpression( path.getPath() ) );
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
        columns = null;
    }

    public static HQLBuilder select( Class<?> rootEntityClass, String rootEntityAlias ) {
        return new HQLBuilder( rootEntityClass, rootEntityAlias, BuilderMode.SELECT );
    }

    public static HQLBuilder select( String rootEntityClass, String rootEntityAlias ) {
        return new HQLBuilder( rootEntityClass, rootEntityAlias, BuilderMode.SELECT );
    }

    public static HQLBuilder insert( Class<?> rootEntityClass ) {
        throw new UnsupportedOperationException( "Due to HQL limitations, INSERT queries are not implemented." );
    }

    public static HQLBuilder insert( String rootEntityClass ) {
        throw new UnsupportedOperationException( "Due to HQL limitations, INSERT queries are not implemented." );
    }

    public static HQLBuilder update( Class<?> rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.UPDATE );
    }

    public static HQLBuilder update( String rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.UPDATE );
    }

    public static HQLBuilder delete( Class<?> rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.DELETE );
    }

    public static HQLBuilder delete( String rootEntityClass ) {
        return new HQLBuilder( rootEntityClass, null, BuilderMode.DELETE );
    }

    public static HQLBuilder clone( HQLBuilder that ) {
        HQLBuilder builder = new HQLBuilder();
        builder.setMode( that.getMode() );
        builder.setRootEntityClass( that.getRootEntityClass() );
        builder.setRootEntityAlias( that.getRootEntityAlias() );

        builder.setColumns( that.getColumns() );
        builder.setJoinExpressions( that.getJoinExpressions() );
        builder.setSetExpressions( that.getSetExpressions() );
        builder.setWhereExpressions( that.getWhereExpressions() );
        builder.setOrderByExpressions( that.getOrderByExpressions() );
        builder.setGroupByExpressions( that.getGroupByExpressions() );

        builder.setVariables( that.getVariables() );

        return builder;
    }

    public String getInternalUUID() {
        return internalUUID;
    }

    public void setInternalUUID( String internalUUID ) {
        this.internalUUID = internalUUID;
    }

    public String getRootEntityClass() {
        return rootEntityClass;
    }

    public void setRootEntityClass( String rootEntityClass ) {
        this.rootEntityClass = rootEntityClass;
    }

    public String getRootEntityAlias() {
        return rootEntityAlias;
    }

    public void setRootEntityAlias( String rootEntityAlias ) {
        this.rootEntityAlias = rootEntityAlias;
    }

    public BuilderMode getMode() {
        return mode;
    }

    public void setMode( BuilderMode mode ) {
        this.mode = mode;
    }

    public List<ColumnExpression> getColumns() {
        return columns;
    }

    public void setColumns( List<ColumnExpression> columns ) {
        this.columns = columns;
    }

    public List<JoinExpression> getJoinExpressions() {
        return joinExpressions;
    }

    public void setJoinExpressions( List<JoinExpression> joinExpressions ) {
        this.joinExpressions = joinExpressions;
    }

    public List<SetExpression> getSetExpressions() {
        return setExpressions;
    }

    public void setSetExpressions( List<SetExpression> setExpressions ) {
        this.setExpressions = setExpressions;
    }

    public List<WhereExpression> getWhereExpressions() {
        return whereExpressions;
    }

    public void setWhereExpressions( List<WhereExpression> whereExpressions ) {
        this.whereExpressions = whereExpressions;
    }

    public List<OrderByExpression> getOrderByExpressions() {
        return orderByExpressions;
    }

    public void setOrderByExpressions( List<OrderByExpression> orderByExpressions ) {
        this.orderByExpressions = orderByExpressions;
    }

    public List<GroupByExpression> getGroupByExpressions() {
        return groupByExpressions;
    }

    public void setGroupByExpressions( List<GroupByExpression> groupByExpressions ) {
        this.groupByExpressions = groupByExpressions;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables( Map<String, Object> variables ) {
        this.variables = variables;
    }
}
