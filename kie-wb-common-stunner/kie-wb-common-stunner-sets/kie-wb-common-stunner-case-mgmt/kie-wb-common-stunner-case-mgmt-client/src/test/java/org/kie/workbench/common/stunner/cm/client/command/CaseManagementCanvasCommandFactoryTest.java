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

package org.kie.workbench.common.stunner.cm.client.command;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.cm.client.CaseManagementShapeSet;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommand;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.processing.traverse.tree.TreeWalkTraverseProcessor;
import org.kie.workbench.common.stunner.core.graph.processing.traverse.tree.TreeWalkTraverseProcessorImpl;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseManagementCanvasCommandFactoryTest {

    @Mock
    private Node parent;

    @Mock
    private Node child;

    private TreeWalkTraverseProcessor treeWalkTraverseProcessor;

    private CaseManagementCanvasCommandFactory factory;

    @Before
    public void setup() {
        this.treeWalkTraverseProcessor = new TreeWalkTraverseProcessorImpl();
        this.factory = new CaseManagementCanvasCommandFactory(treeWalkTraverseProcessor);
    }

    @Test
    public void checkDrawCommandType() {
        //Checks the command is a specific sub-class for Case Management
        final CanvasCommand<AbstractCanvasHandler> command = factory.draw();
        assertNotNull(command);
        assertTrue(command instanceof CaseManagementDrawCommand);
    }

    @Test
    public void checkAddChildNodeCommandType() {
        //Checks the command is a specific sub-class for Case Management
        final CanvasCommand<AbstractCanvasHandler> command = factory.addChildNode(parent,
                                                                                  child,
                                                                                  CaseManagementShapeSet.class.getName());
        assertNotNull(command);
        assertTrue(command instanceof CaseManagementAddChildCommand);
    }

    @Test
    public void checkSetChildNodeCommandType() {
        //Checks the command is a specific sub-class for Case Management
        final CanvasCommand<AbstractCanvasHandler> command = factory.setChildNode(parent,
                                                                                  child);
        assertNotNull(command);
        assertTrue(command instanceof CaseManagementSetChildCommand);
    }

    @Test
    public void checkSetChildNodeForCanvasManagementCommandType() {
        //Checks the command is a specific sub-class for Case Management
        final CanvasCommand<AbstractCanvasHandler> command = factory.setChildNode(parent,
                                                                                  child,
                                                                                  Optional.of(0),
                                                                                  Optional.empty(),
                                                                                  Optional.empty());
        assertNotNull(command);
        assertTrue(command instanceof CaseManagementSetChildCommand);
    }

    @Test
    public void checkRemoveChildCommandType() {
        //Checks the command is a specific sub-class for Case Management
        final CanvasCommand<AbstractCanvasHandler> command = factory.removeChild(parent,
                                                                                 child);
        assertNotNull(command);
        assertTrue(command instanceof CaseManagementRemoveChildCommand);
    }

    @Test
    public void checkUpdatePositionCommandType() {
        //Checks the command is a specific sub-class for Case Management
        final CanvasCommand<AbstractCanvasHandler> command = factory.updatePosition(child,
                                                                                    0.0,
                                                                                    0.0);
        assertNotNull(command);
        assertTrue(command instanceof CaseManagementUpdatePositionCommand);
    }
}
