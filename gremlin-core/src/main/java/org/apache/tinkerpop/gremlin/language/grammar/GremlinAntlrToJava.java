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

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.tinkerpop.gremlin.process.traversal.Bytecode;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import java.util.function.Supplier;

/**
 * This is the entry point for converting the Gremlin Antlr grammar into Java. It is bound to a {@link Graph} instance
 * as that instance may spawn specific {@link Traversal} or {@link TraversalSource} types. A new instance should be
 * created for each parse execution.
 */
public class GremlinAntlrToJava extends GremlinBaseVisitor<Object> {

    /**
     * The {@link Graph} instance to which this instance is bound.
     */
    final Graph graph;

    /**
     * A {@link GremlinBaseVisitor} that processes {@link TraversalSource} methods.
     */
    final GremlinBaseVisitor<GraphTraversalSource> gvisitor;

    /**
     * A {@link GremlinBaseVisitor} that processes {@link Traversal} methods and is meant to construct traversals
     * anonymously.
     */
    final GremlinBaseVisitor<GraphTraversal> tvisitor;

    final GremlinBaseVisitor<Traversal[]> anonymousListVisitor;

    final Supplier<GraphTraversal> createAnonymous;

    /**
     * Constructs a new instance and is bound to an {@link EmptyGraph}. This form of construction is helpful for
     * generating {@link Bytecode} or for various forms of testing. {@link Traversal} instances constructed from this
     * form will not be capable of iterating.
     */
    public GremlinAntlrToJava() {
        this(EmptyGraph.instance());
    }

    /**
     * Constructs a new instance that is bound to the specified {@link Graph} instance.
     */
    public GremlinAntlrToJava(final Graph graph) {
        this(graph, __::start);
    }

    /**
     * Constructs a new instance that is bound to the specified {@link Graph} instance with an override to using
     * {@link __} for constructing anonymous {@link Traversal} instances.
     */
    protected GremlinAntlrToJava(final Graph graph, final Supplier<GraphTraversal> createAnonymous) {
        this.graph = graph;
        this.gvisitor = new GraphTraversalSourceVisitor(this);
        this.tvisitor = new TraversalRootVisitor(this);
        this.anonymousListVisitor = new NestedTraversalSourceListVisitor(this);
        this.createAnonymous = createAnonymous;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object visitQuery(final GremlinParser.QueryContext ctx) {
        final int childCount = ctx.getChildCount();
        if (childCount <= 3) {
            final ParseTree firstChild = ctx.getChild(0);
            if (firstChild instanceof GremlinParser.TraversalSourceContext) {
                if (childCount == 1) {
                    // handle traversalSource
                    return gvisitor.visitTraversalSource((GremlinParser.TraversalSourceContext) firstChild);
                } else {
                    // handle traversalSource DOT transactionPart
                    throw new GremlinParserException("Transaction operation is not supported yet");
                }
            } else if (firstChild instanceof GremlinParser.EmptyQueryContext) {
                // handle empty query
                return "";
            } else {
                if (childCount == 1) {
                    // handle rootTraversal
                    return tvisitor.visitRootTraversal(
                            (GremlinParser.RootTraversalContext) firstChild);
                } else {
                    // handle rootTraversal DOT traversalTerminalMethod
                    return new TraversalTerminalMethodVisitor(tvisitor.visitRootTraversal(
                            (GremlinParser.RootTraversalContext) firstChild)).visitTraversalTerminalMethod(
                            (GremlinParser.TraversalTerminalMethodContext)ctx.getChild(2));
                }
            }
        } else {
            // handle toString
            return String.valueOf(visitChildren(ctx));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override public Object visitQueryList(final GremlinParser.QueryListContext ctx) {
        return visitChildren(ctx);
    }

    /**
     * Override the aggregate result behavior.
     * If the next Result is null, return the current result.
     * This is used to handle child EOF, which is the last child of the QueryList context.
     * If the next Result is not null, return the next result.
     * This is used to handle multiple queries, and return only the last query result logic.
     */
    @Override
    protected Object aggregateResult(final Object result, final Object nextResult) {
        if (nextResult == null) {
            return result;
        } else {
            return nextResult;
        }
    }
}
