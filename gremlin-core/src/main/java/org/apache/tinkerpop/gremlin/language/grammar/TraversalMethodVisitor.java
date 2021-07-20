/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.language.grammar;

import org.apache.tinkerpop.gremlin.process.traversal.Operator;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Column;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import static org.apache.tinkerpop.gremlin.process.traversal.SackFunctions.Barrier.normSack;

/**
 * Specific case of TraversalRootVisitor where all TraversalMethods returns
 * a GraphTraversal object.
 */
public class TraversalMethodVisitor extends TraversalRootVisitor<GraphTraversal> {
    // This object is used to append the traversal methods.
    private GraphTraversal graphTraversal;

    public TraversalMethodVisitor(final GremlinAntlrToJava antlr, GraphTraversal graphTraversal) {
        super(antlr, graphTraversal);
        this.graphTraversal = graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Traversal visitTraversalMethod(final GremlinParser.TraversalMethodContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_V(final GremlinParser.TraversalMethod_VContext ctx) {
        if (ctx.genericLiteralList().getChildCount() != 0) {
            this.graphTraversal = this.graphTraversal.V(GenericLiteralVisitor.getGenericLiteralList(ctx.genericLiteralList()));
        } else {
            this.graphTraversal = this.graphTraversal.V();
        }
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_addV_Empty(final GremlinParser.TraversalMethod_addV_EmptyContext ctx) {
        this.graphTraversal = this.graphTraversal.addV();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_addV_String(final GremlinParser.TraversalMethod_addV_StringContext ctx) {
        this.graphTraversal = this.graphTraversal.addV(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_addE_Traversal(final GremlinParser.TraversalMethod_addE_TraversalContext ctx) {
        this.graphTraversal = this.graphTraversal.addE(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_addV_Traversal(final GremlinParser.TraversalMethod_addV_TraversalContext ctx) {
        this.graphTraversal = this.graphTraversal.addV(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_addE_String(final GremlinParser.TraversalMethod_addE_StringContext ctx) {
        final int childIndexOfParameterEdgeLabel = 2;
        final GremlinParser.StringLiteralContext stringLiteralContext =
                (GremlinParser.StringLiteralContext) (ctx.getChild(childIndexOfParameterEdgeLabel));
        this.graphTraversal = this.graphTraversal.addE(GenericLiteralVisitor.getStringLiteral(stringLiteralContext));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_aggregate_String(final GremlinParser.TraversalMethod_aggregate_StringContext ctx) {
        this.graphTraversal = graphTraversal.aggregate(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_aggregate_Scope_String(final GremlinParser.TraversalMethod_aggregate_Scope_StringContext ctx) {
        this.graphTraversal = graphTraversal.aggregate(
                TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)),
                GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_and(final GremlinParser.TraversalMethod_andContext ctx) {
        this.graphTraversal = this.graphTraversal.and(
                antlr.anonymousListVisitor.visitNestedTraversalList(ctx.nestedTraversalList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_as(final GremlinParser.TraversalMethod_asContext ctx) {
        if (ctx.getChildCount() == 4) {
            this.graphTraversal = graphTraversal.as(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        } else {
            this.graphTraversal = graphTraversal.as(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                    GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        }
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_barrier_Consumer(final GremlinParser.TraversalMethod_barrier_ConsumerContext ctx) {
        // normSack is a special consumer enum type defined in org.apache.tinkerpop.gremlin.process.traversal.SackFunctions.Barrier
        // it is not used in any other traversal methods.
        this.graphTraversal = this.graphTraversal.barrier(normSack);
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_barrier_Empty(final GremlinParser.TraversalMethod_barrier_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.barrier();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_barrier_int(final GremlinParser.TraversalMethod_barrier_intContext ctx) {
        this.graphTraversal = graphTraversal.barrier(Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_both(final GremlinParser.TraversalMethod_bothContext ctx) {
        graphTraversal.both(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_bothE(final GremlinParser.TraversalMethod_bothEContext ctx) {
        this.graphTraversal = graphTraversal.bothE(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_bothV(final GremlinParser.TraversalMethod_bothVContext ctx) {
        this.graphTraversal = graphTraversal.bothV();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_branch(final GremlinParser.TraversalMethod_branchContext ctx) {
        this.graphTraversal = this.graphTraversal.branch(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_Comparator(final GremlinParser.TraversalMethod_by_ComparatorContext ctx) {
        this.graphTraversal = graphTraversal.by(TraversalEnumParser.parseTraversalEnumFromContext(Order.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_Empty(final GremlinParser.TraversalMethod_by_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.by();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_Function(final GremlinParser.TraversalMethod_by_FunctionContext ctx) {
        this.graphTraversal = graphTraversal.by(TraversalFunctionVisitor.getInstance().visitTraversalFunction(ctx.traversalFunction()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_Function_Comparator(final GremlinParser.TraversalMethod_by_Function_ComparatorContext ctx) {
        this.graphTraversal = graphTraversal.by(TraversalFunctionVisitor.getInstance().visitTraversalFunction(ctx.traversalFunction()),
                TraversalEnumParser.parseTraversalEnumFromContext(Order.class, ctx.getChild(4)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_Order(final GremlinParser.TraversalMethod_by_OrderContext ctx) {
        this.graphTraversal = graphTraversal.by(TraversalEnumParser.parseTraversalEnumFromContext(Order.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_String(final GremlinParser.TraversalMethod_by_StringContext ctx) {
        this.graphTraversal = graphTraversal.by(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_String_Comparator(final GremlinParser.TraversalMethod_by_String_ComparatorContext ctx) {
        this.graphTraversal = graphTraversal.by(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                TraversalEnumParser.parseTraversalEnumFromContext(Order.class, ctx.getChild(4)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_T(final GremlinParser.TraversalMethod_by_TContext ctx) {
        this.graphTraversal = graphTraversal.by(TraversalEnumParser.parseTraversalEnumFromContext(T.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_Traversal(final GremlinParser.TraversalMethod_by_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.by(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_by_Traversal_Comparator(final GremlinParser.TraversalMethod_by_Traversal_ComparatorContext ctx) {
        this.graphTraversal =
                this.graphTraversal.by(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()),
                        TraversalEnumParser.parseTraversalEnumFromContext(Order.class, ctx.getChild(4)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_cap(final GremlinParser.TraversalMethod_capContext ctx) {
        if (ctx.getChildCount() == 4) {
            this.graphTraversal = graphTraversal.cap(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        } else {
            this.graphTraversal = graphTraversal.cap(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                    GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        }
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_choose_Function(final GremlinParser.TraversalMethod_choose_FunctionContext ctx) {
        graphTraversal.choose(TraversalFunctionVisitor.getInstance().visitTraversalFunction(ctx.traversalFunction()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_choose_Predicate_Traversal(final GremlinParser.TraversalMethod_choose_Predicate_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.choose(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_choose_Predicate_Traversal_Traversal(final GremlinParser.TraversalMethod_choose_Predicate_Traversal_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.choose(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal(0)),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal(1)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_choose_Traversal(final GremlinParser.TraversalMethod_choose_TraversalContext ctx) {
        this.graphTraversal = this.graphTraversal.choose(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_choose_Traversal_Traversal(final GremlinParser.TraversalMethod_choose_Traversal_TraversalContext ctx) {
        this.graphTraversal = this.graphTraversal.choose(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal(0)),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal(1)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_choose_Traversal_Traversal_Traversal(final GremlinParser.TraversalMethod_choose_Traversal_Traversal_TraversalContext ctx) {
        this.graphTraversal = this.graphTraversal.choose(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal(0)),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal(1)),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_coalesce(final GremlinParser.TraversalMethod_coalesceContext ctx) {
        this.graphTraversal = this.graphTraversal.coalesce(
                antlr.anonymousListVisitor.visitNestedTraversalList(ctx.nestedTraversalList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_coin(final GremlinParser.TraversalMethod_coinContext ctx) {
        this.graphTraversal = graphTraversal.coin(Double.valueOf(ctx.floatLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_constant(final GremlinParser.TraversalMethod_constantContext ctx) {
        this.graphTraversal = graphTraversal
                .constant(GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_count_Empty(final GremlinParser.TraversalMethod_count_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.count();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_count_Scope(final GremlinParser.TraversalMethod_count_ScopeContext ctx) {
        this.graphTraversal = graphTraversal.count(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_cyclicPath(final GremlinParser.TraversalMethod_cyclicPathContext ctx) {
        this.graphTraversal = graphTraversal.cyclicPath();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_dedup_Scope_String(final GremlinParser.TraversalMethod_dedup_Scope_StringContext ctx) {
        this.graphTraversal = graphTraversal.dedup(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)),
                GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_dedup_String(final GremlinParser.TraversalMethod_dedup_StringContext ctx) {
        this.graphTraversal = graphTraversal.dedup(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_drop(final GremlinParser.TraversalMethod_dropContext ctx) {
        this.graphTraversal = graphTraversal.drop();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_emit_Empty(final GremlinParser.TraversalMethod_emit_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.emit();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_emit_Predicate(final GremlinParser.TraversalMethod_emit_PredicateContext ctx) {
        this.graphTraversal = graphTraversal.emit(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_emit_Traversal(final GremlinParser.TraversalMethod_emit_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.emit(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_filter_Predicate(final GremlinParser.TraversalMethod_filter_PredicateContext ctx) {
        this.graphTraversal = graphTraversal.filter(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_filter_Traversal(final GremlinParser.TraversalMethod_filter_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.filter(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_flatMap(final GremlinParser.TraversalMethod_flatMapContext ctx) {
        this.graphTraversal =
                this.graphTraversal.flatMap(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_fold_Empty(final GremlinParser.TraversalMethod_fold_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.fold();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_fold_Object_BiFunction(final GremlinParser.TraversalMethod_fold_Object_BiFunctionContext ctx) {
        graphTraversal.fold(GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral()),
                TraversalEnumParser.parseTraversalEnumFromContext(Operator.class, ctx.getChild(4)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_from_String(final GremlinParser.TraversalMethod_from_StringContext ctx) {
        this.graphTraversal = graphTraversal.from(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_from_Traversal(final GremlinParser.TraversalMethod_from_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.from(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_groupCount_Empty(final GremlinParser.TraversalMethod_groupCount_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.groupCount();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_groupCount_String(final GremlinParser.TraversalMethod_groupCount_StringContext ctx) {
        this.graphTraversal = graphTraversal.groupCount(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_group_Empty(final GremlinParser.TraversalMethod_group_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.group();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_group_String(final GremlinParser.TraversalMethod_group_StringContext ctx) {
        this.graphTraversal = graphTraversal.group(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasId_Object_Object(final GremlinParser.TraversalMethod_hasId_Object_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.hasId(GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral()),
                GenericLiteralVisitor.getGenericLiteralList(ctx.genericLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasId_P(final GremlinParser.TraversalMethod_hasId_PContext ctx) {
        this.graphTraversal = graphTraversal.hasId(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasKey_P(final GremlinParser.TraversalMethod_hasKey_PContext ctx) {
        this.graphTraversal = graphTraversal.hasKey(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasKey_String_String(final GremlinParser.TraversalMethod_hasKey_String_StringContext ctx) {
        if (ctx.getChildCount() == 4) {
            this.graphTraversal = graphTraversal.hasKey(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        } else {
            this.graphTraversal = graphTraversal.hasKey(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                    GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        }
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasLabel_P(final GremlinParser.TraversalMethod_hasLabel_PContext ctx) {
        this.graphTraversal = graphTraversal.hasLabel(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasLabel_String_String(final GremlinParser.TraversalMethod_hasLabel_String_StringContext ctx) {
        if (ctx.getChildCount() == 4) {
            this.graphTraversal = graphTraversal.hasLabel(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        } else {
            this.graphTraversal = graphTraversal.hasLabel(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                    GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        }
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasNot(final GremlinParser.TraversalMethod_hasNotContext ctx) {
        this.graphTraversal = graphTraversal.hasNot(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasValue_Object_Object(final GremlinParser.TraversalMethod_hasValue_Object_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.hasValue(GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral()),
                GenericLiteralVisitor.getGenericLiteralList(ctx.genericLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_hasValue_P(final GremlinParser.TraversalMethod_hasValue_PContext ctx) {
        this.graphTraversal = graphTraversal.hasValue(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_String(final GremlinParser.TraversalMethod_has_StringContext ctx) {
        this.graphTraversal = graphTraversal.has(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_String_Object(final GremlinParser.TraversalMethod_has_String_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.has(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                new GenericLiteralVisitor(antlr).visitGenericLiteral(ctx.genericLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_String_P(final GremlinParser.TraversalMethod_has_String_PContext ctx) {
        this.graphTraversal = graphTraversal.has(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_String_String_Object(final GremlinParser.TraversalMethod_has_String_String_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.has(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(0)),
                GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(1)),
                GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_String_String_P(final GremlinParser.TraversalMethod_has_String_String_PContext ctx) {
        this.graphTraversal = graphTraversal.has(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(0)),
                GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(1)),
                TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_String_Traversal(final GremlinParser.TraversalMethod_has_String_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.has(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_T_Object(final GremlinParser.TraversalMethod_has_T_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.has(TraversalEnumParser.parseTraversalEnumFromContext(T.class, ctx.getChild(2)),
                new GenericLiteralVisitor(antlr).visitGenericLiteral(ctx.genericLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_T_P(final GremlinParser.TraversalMethod_has_T_PContext ctx) {
        this.graphTraversal = graphTraversal.has(TraversalEnumParser.parseTraversalEnumFromContext(T.class, ctx.getChild(2)),
                TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_has_T_Traversal(final GremlinParser.TraversalMethod_has_T_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.has(TraversalEnumParser.parseTraversalEnumFromContext(T.class, ctx.getChild(2)),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_id(final GremlinParser.TraversalMethod_idContext ctx) {
        this.graphTraversal = graphTraversal.id();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_identity(final GremlinParser.TraversalMethod_identityContext ctx) {
        this.graphTraversal = graphTraversal.identity();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_in(final GremlinParser.TraversalMethod_inContext ctx) {
        this.graphTraversal = graphTraversal.in(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_inE(final GremlinParser.TraversalMethod_inEContext ctx) {
        this.graphTraversal = graphTraversal.inE(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_inV(final GremlinParser.TraversalMethod_inVContext ctx) {
        this.graphTraversal = graphTraversal.inV();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_inject(final GremlinParser.TraversalMethod_injectContext ctx) {
        this.graphTraversal = graphTraversal.inject(GenericLiteralVisitor.getGenericLiteralList(ctx.genericLiteralList()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_index(final GremlinParser.TraversalMethod_indexContext ctx) {
        this.graphTraversal = graphTraversal.index();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_is_Object(final GremlinParser.TraversalMethod_is_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.is(GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_is_P(final GremlinParser.TraversalMethod_is_PContext ctx) {
        this.graphTraversal = graphTraversal.is(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_key(final GremlinParser.TraversalMethod_keyContext ctx) {
        this.graphTraversal = graphTraversal.key();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_label(final GremlinParser.TraversalMethod_labelContext ctx) {
        this.graphTraversal = graphTraversal.label();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_limit_Scope_long(final GremlinParser.TraversalMethod_limit_Scope_longContext ctx) {
        this.graphTraversal = graphTraversal.limit(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)),
                Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_limit_long(final GremlinParser.TraversalMethod_limit_longContext ctx) {
        this.graphTraversal = graphTraversal.limit(Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_local(final GremlinParser.TraversalMethod_localContext ctx) {
        this.graphTraversal = graphTraversal.local(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_loops_Empty(final GremlinParser.TraversalMethod_loops_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.loops();
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_loops_String(final GremlinParser.TraversalMethod_loops_StringContext ctx) {
        this.graphTraversal = graphTraversal.loops(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_repeat_String_Traversal(final GremlinParser.TraversalMethod_repeat_String_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.repeat((GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral())),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_repeat_Traversal(final GremlinParser.TraversalMethod_repeat_TraversalContext ctx) {
        this.graphTraversal = this.graphTraversal.repeat(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_read(final GremlinParser.TraversalMethod_readContext ctx) {
        this.graphTraversal = graphTraversal.read();
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_write(final GremlinParser.TraversalMethod_writeContext ctx) {
        this.graphTraversal = graphTraversal.write();
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_with_String(final GremlinParser.TraversalMethod_with_StringContext ctx) {
        this.graphTraversal = graphTraversal.with(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_with_String_Object(final GremlinParser.TraversalMethod_with_String_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.with(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                new GenericLiteralVisitor(antlr).visitGenericLiteral(ctx.genericLiteral()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_shortestPath(final GremlinParser.TraversalMethod_shortestPathContext ctx) {
        this.graphTraversal = graphTraversal.shortestPath();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_map(final GremlinParser.TraversalMethod_mapContext ctx) {
        this.graphTraversal = this.graphTraversal.map(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_match(final GremlinParser.TraversalMethod_matchContext ctx) {
        this.graphTraversal = this.graphTraversal.match(
                antlr.anonymousListVisitor.visitNestedTraversalList(ctx.nestedTraversalList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_max_Empty(final GremlinParser.TraversalMethod_max_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.max();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_max_Scope(final GremlinParser.TraversalMethod_max_ScopeContext ctx) {
        this.graphTraversal = graphTraversal.max(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_mean_Empty(final GremlinParser.TraversalMethod_mean_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.mean();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_mean_Scope(final GremlinParser.TraversalMethod_mean_ScopeContext ctx) {
        this.graphTraversal = graphTraversal.mean(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_min_Empty(final GremlinParser.TraversalMethod_min_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.min();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_min_Scope(final GremlinParser.TraversalMethod_min_ScopeContext ctx) {
        this.graphTraversal = graphTraversal.min(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_not(final GremlinParser.TraversalMethod_notContext ctx) {
        this.graphTraversal = this.graphTraversal.not(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_option_Object_Traversal(final GremlinParser.TraversalMethod_option_Object_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.option(GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral()),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_option_Traversal(final GremlinParser.TraversalMethod_option_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.option(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_optional(final GremlinParser.TraversalMethod_optionalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.optional(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_or(final GremlinParser.TraversalMethod_orContext ctx) {
        this.graphTraversal = this.graphTraversal.or(
                antlr.anonymousListVisitor.visitNestedTraversalList(ctx.nestedTraversalList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_order_Empty(final GremlinParser.TraversalMethod_order_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.order();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_order_Scope(final GremlinParser.TraversalMethod_order_ScopeContext ctx) {
        this.graphTraversal = graphTraversal.order(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_otherV(final GremlinParser.TraversalMethod_otherVContext ctx) {
        this.graphTraversal = graphTraversal.otherV();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_out(final GremlinParser.TraversalMethod_outContext ctx) {
        this.graphTraversal = graphTraversal.out(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_outE(final GremlinParser.TraversalMethod_outEContext ctx) {
        this.graphTraversal = graphTraversal.outE(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_outV(final GremlinParser.TraversalMethod_outVContext ctx) {
        this.graphTraversal = graphTraversal.outV();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_pageRank_Empty(final GremlinParser.TraversalMethod_pageRank_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.pageRank();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_pageRank_double(final GremlinParser.TraversalMethod_pageRank_doubleContext ctx) {
        this.graphTraversal = graphTraversal.pageRank(Double.valueOf(ctx.floatLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_path(final GremlinParser.TraversalMethod_pathContext ctx) {
        this.graphTraversal = graphTraversal.path();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_peerPressure(final GremlinParser.TraversalMethod_peerPressureContext ctx) {
        this.graphTraversal = graphTraversal.peerPressure();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_profile_Empty(final GremlinParser.TraversalMethod_profile_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.profile();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_profile_String(final GremlinParser.TraversalMethod_profile_StringContext ctx) {
        this.graphTraversal = graphTraversal.profile(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_project(final GremlinParser.TraversalMethod_projectContext ctx) {
        if (ctx.getChildCount() == 4) {
            this.graphTraversal = graphTraversal.project(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        } else {
            this.graphTraversal = graphTraversal.project(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                    GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        }
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_properties(final GremlinParser.TraversalMethod_propertiesContext ctx) {
        this.graphTraversal = graphTraversal.properties(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_elementMap(final GremlinParser.TraversalMethod_elementMapContext ctx) {
        this.graphTraversal = graphTraversal.elementMap(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_propertyMap(final GremlinParser.TraversalMethod_propertyMapContext ctx) {
        this.graphTraversal = graphTraversal.propertyMap(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_property_Cardinality_Object_Object_Object(final GremlinParser.TraversalMethod_property_Cardinality_Object_Object_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.property(TraversalEnumParser.parseTraversalEnumFromContext(VertexProperty.Cardinality.class, ctx.getChild(2)),
                GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral(0)),
                GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral(1)),
                GenericLiteralVisitor.getGenericLiteralList(ctx.genericLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_property_Object_Object_Object(final GremlinParser.TraversalMethod_property_Object_Object_ObjectContext ctx) {
        this.graphTraversal = graphTraversal.property(GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral(0)),
                GenericLiteralVisitor.getInstance().visitGenericLiteral(ctx.genericLiteral(1)),
                GenericLiteralVisitor.getGenericLiteralList(ctx.genericLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_range_Scope_long_long(final GremlinParser.TraversalMethod_range_Scope_long_longContext ctx) {
        this.graphTraversal = graphTraversal.range(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)),
                Integer.valueOf(ctx.integerLiteral(0).getText()),
                Integer.valueOf(ctx.integerLiteral(1).getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_range_long_long(final GremlinParser.TraversalMethod_range_long_longContext ctx) {
        this.graphTraversal = graphTraversal.range(Integer.valueOf(ctx.integerLiteral(0).getText()),
                Integer.valueOf(ctx.integerLiteral(1).getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_sack_BiFunction(final GremlinParser.TraversalMethod_sack_BiFunctionContext ctx) {
        graphTraversal.sack(TraversalEnumParser.parseTraversalEnumFromContext(Operator.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_sack_Empty(final GremlinParser.TraversalMethod_sack_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.sack();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_sample_Scope_int(final GremlinParser.TraversalMethod_sample_Scope_intContext ctx) {
        this.graphTraversal = graphTraversal.sample(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)),
                Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_sample_int(final GremlinParser.TraversalMethod_sample_intContext ctx) {
        this.graphTraversal = graphTraversal.sample(Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_select_Column(final GremlinParser.TraversalMethod_select_ColumnContext ctx) {
        this.graphTraversal = graphTraversal.select(TraversalEnumParser.parseTraversalEnumFromContext(Column.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_select_Pop_String(final GremlinParser.TraversalMethod_select_Pop_StringContext ctx) {
        this.graphTraversal = graphTraversal.select(TraversalEnumParser.parseTraversalEnumFromContext(Pop.class, ctx.getChild(2)),
                GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_select_Pop_String_String_String(final GremlinParser.TraversalMethod_select_Pop_String_String_StringContext ctx) {
        this.graphTraversal = graphTraversal.select(TraversalEnumParser.parseTraversalEnumFromContext(Pop.class, ctx.getChild(2)),
                GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(0)),
                GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(1)),
                GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_select_Pop_Traversal(final GremlinParser.TraversalMethod_select_Pop_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.select(TraversalEnumParser.parseTraversalEnumFromContext(Pop.class, ctx.getChild(2)),
                antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_select_String(final GremlinParser.TraversalMethod_select_StringContext ctx) {
        this.graphTraversal = graphTraversal.select(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_select_String_String_String(final GremlinParser.TraversalMethod_select_String_String_StringContext ctx) {
        this.graphTraversal = graphTraversal.select(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(0)),
                GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral(1)),
                GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_select_Traversal(final GremlinParser.TraversalMethod_select_TraversalContext ctx) {
        this.graphTraversal = graphTraversal.select(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_sideEffect(final GremlinParser.TraversalMethod_sideEffectContext ctx) {
        this.graphTraversal =
                this.graphTraversal.sideEffect(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_simplePath(final GremlinParser.TraversalMethod_simplePathContext ctx) {
        this.graphTraversal = graphTraversal.simplePath();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_skip_Scope_long(final GremlinParser.TraversalMethod_skip_Scope_longContext ctx) {
        this.graphTraversal = graphTraversal.skip(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)),
                Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_skip_long(final GremlinParser.TraversalMethod_skip_longContext ctx) {
        this.graphTraversal = graphTraversal.skip(Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_store(final GremlinParser.TraversalMethod_storeContext ctx) {
        this.graphTraversal = graphTraversal.store(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_subgraph(final GremlinParser.TraversalMethod_subgraphContext ctx) {
        this.graphTraversal = graphTraversal.subgraph(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_sum_Empty(final GremlinParser.TraversalMethod_sum_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.sum();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_sum_Scope(final GremlinParser.TraversalMethod_sum_ScopeContext ctx) {
        this.graphTraversal = graphTraversal.sum(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_tail_Empty(final GremlinParser.TraversalMethod_tail_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.tail();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_tail_Scope(final GremlinParser.TraversalMethod_tail_ScopeContext ctx) {
        this.graphTraversal = graphTraversal.tail(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_tail_Scope_long(final GremlinParser.TraversalMethod_tail_Scope_longContext ctx) {
        this.graphTraversal = graphTraversal.tail(TraversalEnumParser.parseTraversalEnumFromContext(Scope.class, ctx.getChild(2)),
                Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_tail_long(final GremlinParser.TraversalMethod_tail_longContext ctx) {
        this.graphTraversal = graphTraversal.tail(Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_timeLimit(final GremlinParser.TraversalMethod_timeLimitContext ctx) {
        this.graphTraversal = graphTraversal.timeLimit(Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_times(final GremlinParser.TraversalMethod_timesContext ctx) {
        this.graphTraversal = graphTraversal.times(Integer.valueOf(ctx.integerLiteral().getText()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_toE(final GremlinParser.TraversalMethod_toEContext ctx) {
        this.graphTraversal = graphTraversal.toE(TraversalEnumParser.parseTraversalEnumFromContext(Direction.class, ctx.getChild(2)),
                GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_toV(final GremlinParser.TraversalMethod_toVContext ctx) {
        this.graphTraversal = graphTraversal.toV(TraversalEnumParser.parseTraversalEnumFromContext(Direction.class, ctx.getChild(2)));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_to_Direction_String(final GremlinParser.TraversalMethod_to_Direction_StringContext ctx) {
        this.graphTraversal = graphTraversal.to(TraversalEnumParser.parseTraversalEnumFromContext(Direction.class, ctx.getChild(2)),
                GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_to_String(final GremlinParser.TraversalMethod_to_StringContext ctx) {
        this.graphTraversal = graphTraversal.to(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_to_Traversal(final GremlinParser.TraversalMethod_to_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.to(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_tree_Empty(final GremlinParser.TraversalMethod_tree_EmptyContext ctx) {
        this.graphTraversal = graphTraversal.tree();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_tree_String(final GremlinParser.TraversalMethod_tree_StringContext ctx) {
        this.graphTraversal = graphTraversal.tree(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_unfold(final GremlinParser.TraversalMethod_unfoldContext ctx) {
        this.graphTraversal = graphTraversal.unfold();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_union(final GremlinParser.TraversalMethod_unionContext ctx) {
        this.graphTraversal = this.graphTraversal.union(
                antlr.anonymousListVisitor.visitNestedTraversalList(ctx.nestedTraversalList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_until_Predicate(final GremlinParser.TraversalMethod_until_PredicateContext ctx) {
        this.graphTraversal = graphTraversal.until(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_until_Traversal(final GremlinParser.TraversalMethod_until_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.until(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_value(final GremlinParser.TraversalMethod_valueContext ctx) {
        this.graphTraversal = graphTraversal.value();
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_valueMap_String(final GremlinParser.TraversalMethod_valueMap_StringContext ctx) {
        this.graphTraversal = graphTraversal.valueMap(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_valueMap_boolean_String(final GremlinParser.TraversalMethod_valueMap_boolean_StringContext ctx) {
        if (ctx.getChildCount() == 4) {
            this.graphTraversal = graphTraversal.valueMap(Boolean.valueOf(ctx.booleanLiteral().getText()));
        } else {
            this.graphTraversal = graphTraversal.valueMap(Boolean.valueOf(ctx.booleanLiteral().getText()),
                    GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        }
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_values(final GremlinParser.TraversalMethod_valuesContext ctx) {
        this.graphTraversal = graphTraversal.values(GenericLiteralVisitor.getStringLiteralList(ctx.stringLiteralList()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_where_P(final GremlinParser.TraversalMethod_where_PContext ctx) {
        this.graphTraversal = graphTraversal.where(TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_where_String_P(final GremlinParser.TraversalMethod_where_String_PContext ctx) {
        this.graphTraversal = graphTraversal.where(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()),
                TraversalPredicateVisitor.getInstance().visitTraversalPredicate(ctx.traversalPredicate()));
        return this.graphTraversal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphTraversal visitTraversalMethod_where_Traversal(final GremlinParser.TraversalMethod_where_TraversalContext ctx) {
        this.graphTraversal =
                this.graphTraversal.where(antlr.tvisitor.visitNestedTraversal(ctx.nestedTraversal()));
        return this.graphTraversal;
    }

    @Override
    public GraphTraversal visitTraversalMethod_math(final GremlinParser.TraversalMethod_mathContext ctx) {
        this.graphTraversal = graphTraversal.math(GenericLiteralVisitor.getStringLiteral(ctx.stringLiteral()));
        return this.graphTraversal;
    }

    public GraphTraversal[] getNestedTraversalList(final GremlinParser.NestedTraversalListContext ctx) {
        return ctx.nestedTraversalExpr().nestedTraversal()
                .stream()
                .map(this::visitNestedTraversal)
                .toArray(GraphTraversal[]::new);
    }
}
