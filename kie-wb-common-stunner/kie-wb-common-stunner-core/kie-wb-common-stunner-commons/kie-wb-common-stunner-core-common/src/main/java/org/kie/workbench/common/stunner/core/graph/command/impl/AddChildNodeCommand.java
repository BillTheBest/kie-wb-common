/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.core.graph.command.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandResultBuilder;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.uberfire.commons.validation.PortablePreconditions;

/**
 * Creates a new node on the target graph and creates/defines a new parent-child connection so new node will be added as a child of
 * given parent.
 */
@Portable
public class AddChildNodeCommand extends AbstractGraphCompositeCommand {

    private final String parentUUID;
    private final Node candidate;
    private final Double x;
    private final Double y;
    private transient Node<?, Edge> parent;

    public AddChildNodeCommand(final @MapsTo("parentUUID") String parentUUID,
                               final @MapsTo("candidate") Node candidate,
                               final @MapsTo("x") Double x,
                               final @MapsTo("y") Double y) {
        this.parentUUID = PortablePreconditions.checkNotNull("parentUUID",
                                                             parentUUID);
        this.candidate = PortablePreconditions.checkNotNull("candidate",
                                                            candidate);
        this.x = x;
        this.y = y;
    }

    public AddChildNodeCommand(final Node<?, Edge> parent,
                               final Node candidate,
                               final Double x,
                               final Double y) {
        this(parent.getUUID(),
             candidate,
             x,
             y);
        this.parent = parent;
    }

    public AddChildNodeCommand(final Node<?, Edge> parent,
                               final Node candidate) {
        this(parent,
             candidate,
             null,
             null);
    }

    @SuppressWarnings("unchecked")
    protected AddChildNodeCommand initialize(final GraphCommandExecutionContext context) {
        super.initialize(context);
        final Node<?, Edge> parent = getParent(context);
        this.addCommand(new RegisterNodeCommand(candidate));
        this.addCommand(new SetChildNodeCommand(parent,
                                                candidate));
        if (null != x && null != y) {
            this.addCommand(new UpdateElementPositionCommand(candidate,
                                                             x,
                                                             y));
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandResult<RuleViolation> allow(final GraphCommandExecutionContext context) {
        ensureInitialized(context);
        // Check if rules are present.
        if (null == context.getRulesManager()) {
            return GraphCommandResultBuilder.SUCCESS;
        }
        final Node<?, Edge> parent = getParent(context);
        final Collection<RuleViolation> containmentRuleViolations = (Collection<RuleViolation>) context.getRulesManager().containment().evaluate(parent,
                                                                                                                                                 candidate).violations();
        final Collection<RuleViolation> cardinalityRuleViolations = (Collection<RuleViolation>) context.getRulesManager().cardinality().evaluate(getGraph(context),
                                                                                                                                                 candidate,
                                                                                                                                                 RuleManager.Operation.ADD).violations();
        final Collection<RuleViolation> violations = new LinkedList<RuleViolation>();
        violations.addAll(containmentRuleViolations);
        violations.addAll(cardinalityRuleViolations);
        return new GraphCommandResultBuilder(violations).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandResult<RuleViolation> undo(final GraphCommandExecutionContext context) {
        return new SafeDeleteNodeCommand(getCandidate()).execute(context);
    }

    @SuppressWarnings("unchecked")
    protected Node<?, Edge> getParent(final GraphCommandExecutionContext context) {
        if (null == parent) {
            parent = checkNodeNotNull(context,
                                      parentUUID);
        }
        return parent;
    }

    public Node<?, Edge> getParent() {
        return parent;
    }

    public Node getCandidate() {
        return candidate;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "AddChildNodeCommand [parent=" + parentUUID + ", candidate=" + candidate.getUUID() + "]";
    }

    @Override
    protected boolean delegateRulesContextToChildren() {
        return false;
    }
}
