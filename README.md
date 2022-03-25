# HQL Builder

This library is designed to help you make your [HQL](https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#hql) 
queries more comfortable to create.

**The key features of this project:**
* Create a domain-specific representation of your entities to make sure 
no typos will ruin your query
* Build a query using the given builder and expressions
* Execute expression your favourite way - it's just a built HQL query and 
a map of parameters in the end!

## Installation

Download the sources of this project and include them into your project. 
Or put the attached artefact (folder `1.0.0`) in your `.m2/repository/org/adaptms/hql-builder/` folder. It will be published in 
Maven repository later.

## Usage

### Domain-specific language (DSL)

To enable domain-specific paths support, implement the following structure your entities:

    package org.example.testhql;    

    import org.adaptms.hqlbuilder.property.EntityPath;

    @Entity // make sure to have this annotation on your entity class!
    public class MyTestEntity {

        public static DSL root() { return new DSL( null ); } // this will be used to build queries without aliases (update and delete) 

        public static DSL alias( String alias ) { return new DSL( alias ); } // this will be used for select queries

        // this is an access class for domain-specific paths building. Create an entry for each field in your entity
        public static class DSL extends EntityPath {
            private DSL( String previousPath ) { super( previousPath ); } // required constructor
            public EntityPath fieldOne() { return addToPrevious( "fieldOne" ); } // simple property descriptior
            public EntityPath fieldTwo() { return addToPrevious( "fieldTwo" ); }
            public AnotherEntity.DSL referencedEntity() { return AnotherEntity.alias( step( "referencedEntity" ) ); } // reference descriptor
        }

        private String fieldOne;
        private String fieldTwo;
        private AnotherEntity referencedEntity;

        // getters, setters, etc
    }

This is not required, but may make development easier in perspective.

Let's look into the example above more carefully:

First there has to be annotation `javax.persistence.Entity` on your entity class. 
Otherwise, the builder will throw an exception.

`root()` and `alias( String )` static methods will be your entry point to 
the property path chain. First one is used in cases when alias is not required
(i.e. delete or update queries), second allows you to specify the alias for this 
entity (or previous path, as you will see further on). Technically, you may only 
specify `alias( String )` method and pass `null` when the alias is not needed, 
but but we find it more semantically appropriate to use different methods.

`DSL` class will be a step point in your domain-specific property path chain. 
It allows you to specify both simple properties of the object (e.g. ints, Strings, booleans 
and other objects that are stored as fields in your DB table) and references to other 
entities.

* For entity properties you should use `org.adaptms.hqlbuilder.property.EntityPath`, which 
is a base of a path chain step. Because we need to pass all the previous steps, 
you should call protected method `EntityPath#addToPrevious( String )` which basically 
creates a new step with info of previous chain elements.
* For references you need to provide a `DSL` class of a referenced entity. 
`alias( String )` method of that entity is a perfect way to do so, that's why we added a 
protected method `EntityPath#step( String )`, which allows you to automatically 
form previous path for this step.

In both cases, as a parameter you need to path the exact name of your java 
entity object field which then will be translated by Hibernate automatically into SQL.

## HQLBuilder

HQLBuilder is a class that allows you to create HQL queries using a builder pattern. 
You no longer need to write long lines of HQL statements or huge CriteriaBuilders 
hoping that you didn't miss a typo or some property path step, or forgot to add 
a variable... HQLBuilder builds the query for you and keeps track of all the variables 
that need to get to final query, while DSL support allows you to never go wrong way with 
your property paths.

#### Creating a query

To start creating a query, choose the type of query and call the corresponding 
static method on `org.adaptms.hqlbuilder.builder.HQLBuilder`. From there it's all 
just a classic builder:

    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" );

This will create a simple select query. First argument in `select()` is a class 
of entity that represents a target table in the database, second is the alias 
that you will be using in all the expressions further on when addressing this 
root entity. Please note that this entity must have the @Entity annotation. 

The code above will produce such HQL statement:

    select mte from org.example.testhql.MyTestEntity mte

#### Specifying columns

You may customize columns using `column()` builder method. It 
allows you to specify an `EntityPath` to property that you would like to see in your 
output and a type of column (default (column itself), count, distinct or count distinct - 
optionally). If you use a shorthand method it will add a default column.

    // default columns
    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .column( MyTestEntity.alias( "mte" ).fieldOne() )
            .column( MyTestEntity.alias( "mte" ).referencedEntity().someField() );

    // count column
    HQLBuilder countBuilder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .countColumn( MyTestEntity.alias( "mte" ), ColumnExpressionType.COUNT );

#### Adding filtering conditions

If you need to add a filter to your query, use `where()` builder method. It expects a 
`CommonWhereExpression` that will be applied to query. In order to make it easier, we made 
a class `org.adaptms.hqlbuilder.expression.Expressions` which holds all available WHERE 
filters - just call a corresponding static method. Each method has from 1 to 3 arguments, 
they can all be either `EntityPath`s, `HQLBuilder`s or any other objects, depending on the 
type the behaviour will be changed accordingly.

General usage is like this:

    String filter = "Some test string";

    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .where( Expressions.eq( MyTestEntity.alias( "mte" ).fieldOne(), filter ) );

In this example, a restriction will be added that translates into:
    
    select mte from org.example.testhql.MyTestEntity mte where mte.fieldOne = :var_mte0

where `:var_mte0` is a name of variable that will be automatically created by HQLBuilder 
and its value will be stored.

The expressions also allow comparing properties:

    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .where( Expressions.eq( MyTestEntity.alias( "mte" ).fieldOne(), MyTestEntity.alias( "mte" ).referencedEntity().someField() ) );

and combining of `EntityPath`s and objects:

    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .where( 
                Expressions.in( MyTestEntity.alias( "mte" ).fieldOne(), 
                HQLBuilder.select( AnotherEntity.class, "ae" )
                        .column( AnotherEntity.alias( "ae" ).someField() )
                        .where( Expressions.gt( AnotherEntity.alias( "ae" ).id(), 123 ) )
            ) );

This will be translated into the following:

    select mte from org.example.testhql.MyTestEntity mte where mte.fieldOne in (select ae.someField from org.example.testhql.AnotherEntity ae where ae.id > :var_ae0)

Currently, the following expressions are available:
* `exists` - 1 argument
* `notExists` - 1 argument
* `isNull` - 1 argument
* `isNotNull` - 1 argument
* `eq` - equals, 2 arguments
* `notEq` - not equals, 2 arguments
* `gt` - greater than, 2 arguments
* `lt` - less than, 2 arguments
* `ge` - greater or equals, 2 arguments
* `le` - less or equals, 2 arguments
* `in` - 2 arguments
* `notIn` - 2 arguments
* `like` - 2 arguments
* `notLike` - 2 arguments
* `between` - 3 arguments

For `like` and `notLike`, there exists a service method `Expressions#wrapLike( String, boolean, boolean )`. 
It wraps the first String argument with `%` for `like` expression. From which 
sides to wrap the input control 2nd and 3rd arguments - `start` and `end`. Its shorthand 
without boolean arguments means both of them are treated as `true`.
    
    String test1 = Expressions.wrap( "Test 1" ); // -> "%Test 1%"
    String test2 = Expressions.wrap( "Test 2", true, true ); // -> "%Test 2%"
    String test3 = Expressions.wrap( "Test 3", true, false ); // -> "%Test 3"
    String test4 = Expressions.wrap( "Test 4", false, true ); // -> "Test 4%"

`and` and `or` expressions are also available:

    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .where( Expressions.or(
                    Expressions.eq( EntityPath.fromString( "mte.fieldOne" ), "one" ),
                    Expressions.and(
                            Expressions.like( EntityPath.fromString( "mte.fieldTwo" ), Expressions.wrapLike( "test wrap" ) ),
                            Expressions.eq( EntityPath.fromString( "mte.referencedEntity.someField" ), "three" )
                    )
            ) );

This example also shows how you can use manual path input instead of DSL chains. 
This could be useful if your project is way too huge to incorporate `EntityPath`s 
into all the entities. Just create an `EntityPath` using its static method `fromString()`.

#### Using joins

In order to join some other entity, use `join()` method. You will have to specify 
the type of join (LEFT, RIGHT, INNER or FULL), target entity class (again, the presence 
of @Entity annotation will be checked), alias for it and a `CommonWhereExpression` 
that will be used to form a `with` statement to join both tables. For example:

    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .join( JoinType.LEFT, AnotherEntity.class, "ae", Expressions.eq( AnotherEntity.alias( "ae" ).someField(), MyTestEntity.alias( "mte" ).fieldOne() ) )
            .join( JoinType.INNER, YetAnotherEntity.class, "yae", Expressions.eq( YetAnotherEntity.alias( "yae" ).one(), MyTestEntity.alias( "mte" ).fieldOne() ) )
            .where( Expressions.like( AnotherEntity.alias( "ae" ).someField(), Expressions.wrapLike( "test", false, true ) ) );

#### Using order

To add order, use `HQLBuilder#orderBy()` method. Method with one argument (`EntityPath`) automatically adds an `ASC` order for 
provided path, method with two arguments expects a `QueryOrderDirection` enum value (ASC/DESC):

    HQLBuilder builder = HQLBuilder.select( MyTestEntity.class, "mte" )
            .where( Expressions.like( MyTestEntity.alias( "mte" ).fieldTwo(), Expressions.wrapLike( "test" ) ) )
            .orderBy( MyTestEntity.alias( "mte" ).fieldTwo() )
            .orderBy( MyTestEntity.alias( "mte" ).fieldOne(), QueryOrderDirection.DESC );

#### Using grouping

To add order, use `HQLBuilder#groupBy()` method. The only argument is an `EntityPath` that will be used to group. Remeber, that 
if you use `distinct` columns you need to provide a `groupBy` for this column.

#### Other types of queries

You can use `delete` and `update` queries. Unfortunately, `insert` is not fully supported by HQL, that's why it is 
not implemented in HQLBuilder - you should use Hibernate's amazing ORM features instead!

To use `delete` or `update` queries you will have to call an appropriate static method on the `HQLBuilder`. There types 
of queries do not allow aliases, that's why you only specify the entity class at the start and use `root()` instead of 
`alias( String )` in the path chains further on.

    HQLBuilder builder = HQLBuilder.delete( MyTestEntity.class )
                .where( Expressions.isNull( MyTestEntity.root().reference() ) );

`Update` queries also introduce a specific `set` expression. Just call `set()` on builder and provide property path from root 
and new value:

    HQLBuilder builder = HQLBuilder.update( MyTestEntity.class )
                .set( MyTestEntity.root().fieldOne(), "test new" )
                .where( Expressions.eq( MyTestEntity.root().fieldOne(), "test" ) );

### Building the query

In order to use this builder, you will have to generate the HQL from it using the builder's `build()` method and get 
all the variables using the `getVariables()` getter. Here's an example of implementation of getting a list by a select 
builder in a DAO:

    // org.hibernate.query.Query
    protected <T> Query<T> createQuery( HQLBuilder builder ) {
        Query<T> query = sessionFactory.getCurrentSession().createQuery( builder.build() );
        if ( builder.getVariables() != null ) {
            for ( String name : builder.getVariables().keySet() ) {
                query.setParameter( name, builder.getVariables().get( name ) );
            }
        }
        return query;
    }
