package org.adaptms.hqlbuilder.builder;

import org.adaptms.hqlbuilder.expression.join.JoinType;
import org.adaptms.hqlbuilder.expression.order.QueryOrderDirection;
import org.junit.jupiter.api.Test;
import org.adaptms.hqlbuilder.expression.Expressions;
import org.adaptms.hqlbuilder.property.EntityPath;

import javax.persistence.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class HQLBuilderTest {

    @Test
    void simpleSelectQuery() {
        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" );
        assert builder.build().equals( "select te from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te" );
    }

    @Test
    void simpleSelectQueryWithColumn() {
        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" )
                .column( TestEntity.alias( "te" ).fieldOne() );
        assert builder.build().equals( "select te.fieldOne from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te" );
    }

    @Test
    void selectQueryWithWhere() {
        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" )
                .where( Expressions.eq( TestEntity.alias( "te" ).fieldOne(), "abc" ) )
                .where( Expressions.eq( 12, TestEntity.alias( "te" ).fieldTwo() ) );

        assert builder.build().equals( "select te from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te where te.fieldOne = :var_te0 and :var_te1 = te.fieldTwo" );
    }

    @Test
    void selectQueryWithWhereSubquery() {
        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" )
                .where( Expressions.eq(
                        TestEntity.alias( "te" ).fieldOne(),
                        HQLBuilder.select( TestEntity.class, "tt" )
                                .where( Expressions.eq( TestEntity.alias( "tt" ).fieldTwo(), 32 ) ) )
                );

        assert builder.build().equals( "select te from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te where te.fieldOne = (select tt from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity tt where tt.fieldTwo = :var_tt0)" );
    }

    @Test
    void selectQueryWithCollection() {
        List<String> options = new ArrayList<>( Arrays.asList( "option1", "anotherOption", "someOther Text" ) );

        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" )
                .where( Expressions.between( TestEntity.alias( "te" ).fieldTwo(), 12, 33 ) )
                .where( Expressions.in( TestEntity.alias( "te" ).reference().another(), options ) );

        assert builder.build().equals( "select te from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te where (te.fieldTwo between :var_te0 and :var_te1) and te.reference.another in (:var_te2)" );
    }

    @Test
    void selectQueryAndOrTest() {
        HQLBuilder builder = HQLBuilder.select( AnotherTestEntity.class, "ate" )
                .where( Expressions.or(
                        Expressions.eq( EntityPath.fromString( "ate.one" ), "two" ),
                        Expressions.and(
                                Expressions.like( EntityPath.fromString( "ate.another" ), Expressions.wrapLike( "test wrap" ) ),
                                Expressions.eq( EntityPath.fromString( "ate.one" ), "three" )
                        )
                ) );

        assert builder.build().equals( "select ate from org.adaptms.hqlbuilder.builder.HQLBuilderTest.AnotherTestEntity ate where (ate.one = :var_ate0 or (ate.another like :var_ate1 and ate.one = :var_ate2))" );
    }

    @Test
    void selectQueryWithJoin() {
        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" )
                .join( JoinType.LEFT, AnotherTestEntity.class, "ate", Expressions.eq( AnotherTestEntity.alias( "ate" ).another(), TestEntity.alias( "te" ).fieldOne() ) );

        assert builder.build().equals( "select te from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te left join org.adaptms.hqlbuilder.builder.HQLBuilderTest.AnotherTestEntity ate with ate.another = te.fieldOne" );
    }

    @Test
    void selectQueryWithMultipleJoins() {
        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" )
                .join( JoinType.LEFT, AnotherTestEntity.class, "ate", Expressions.eq( AnotherTestEntity.alias( "ate" ).another(), TestEntity.alias( "te" ).fieldOne() ) )
                .join( JoinType.INNER, AnotherTestEntity.class, "tte", Expressions.eq( AnotherTestEntity.alias( "tte" ).one(), TestEntity.alias( "te" ).fieldOne() ) )
                .where( Expressions.like( AnotherTestEntity.alias( "ate" ).one(), Expressions.wrapLike( "test", false, true ) ) );

        assert builder.build().equals( "select te from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te left join org.adaptms.hqlbuilder.builder.HQLBuilderTest.AnotherTestEntity ate with ate.another = te.fieldOne inner join org.adaptms.hqlbuilder.builder.HQLBuilderTest.AnotherTestEntity tte with tte.one = te.fieldOne where ate.one like :var_te0" );
    }

    @Test
    void selectQueryWithOrder() {
        HQLBuilder builder = HQLBuilder.select( TestEntity.class, "te" )
                .where( Expressions.like( TestEntity.alias( "te" ).fieldTwo(), Expressions.wrapLike( "test" ) ) )
                .orderBy( TestEntity.alias( "te" ).fieldTwo() )
                .orderBy( TestEntity.alias( "te" ).fieldOne(), QueryOrderDirection.DESC );

        assert builder.build().equals( "select te from org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity te where te.fieldTwo like :var_te0 order by te.fieldTwo asc, te.fieldOne desc" );
    }

    @Test
    void deleteQuery() {
        HQLBuilder builder = HQLBuilder.delete( TestEntity.class )
                .where( Expressions.isNull( TestEntity.root().reference() ) );

        assert builder.build().equals( "delete org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity where reference is null" );
    }

    @Test
    void updateQuery() {
        HQLBuilder builder = HQLBuilder.update( TestEntity.class )
                .set( TestEntity.root().fieldOne(), "test new" )
                .where( Expressions.eq( TestEntity.root().fieldOne(), "test" ) );

        String build = builder.build();
        assert build.startsWith( "update org.adaptms.hqlbuilder.builder.HQLBuilderTest.TestEntity set fieldOne = :var_" )
                && build.contains( " where fieldOne = :var_" );
    }

    @Entity
    public static class TestEntity {

        public static DSL root() { return new DSL( null ); }

        public static DSL alias( String alias ) { return new DSL( alias ); }

        public static class DSL extends EntityPath {
            private DSL( String previousPath ) { super( previousPath ); }
            public EntityPath fieldOne() { return addToPrevious( "fieldOne" ); }
            public EntityPath fieldTwo() { return addToPrevious( "fieldTwo" ); }
            public AnotherTestEntity.DSL reference() { return AnotherTestEntity.alias( step( "reference" ) ); }
        }

        private String fieldOne;
        private Integer fieldTwo;
        private AnotherTestEntity reference;

        public TestEntity() {
        }

        public String getFieldOne() {
            return fieldOne;
        }

        public void setFieldOne( String fieldOne ) {
            this.fieldOne = fieldOne;
        }

        public Integer getFieldTwo() {
            return fieldTwo;
        }

        public void setFieldTwo( Integer fieldTwo ) {
            this.fieldTwo = fieldTwo;
        }

        public AnotherTestEntity getReference() {
            return reference;
        }

        public void setReference( AnotherTestEntity reference ) {
            this.reference = reference;
        }
    }

    @Entity
    public static class AnotherTestEntity {

        public static DSL root() { return new DSL( null ); }

        public static DSL alias( String alias ) { return new DSL( alias ); }

        public static class DSL extends EntityPath {
            private DSL( String previousPath ) { super( previousPath ); }
            public EntityPath one() { return addToPrevious( "one" ); }
            public EntityPath another() { return addToPrevious( "another" ); }
        }

        public AnotherTestEntity() {
        }

        private boolean one;
        private String another;

        public boolean isOne() {
            return one;
        }

        public void setOne( boolean one ) {
            this.one = one;
        }

        public String getAnother() {
            return another;
        }

        public void setAnother( String another ) {
            this.another = another;
        }
    }
}
