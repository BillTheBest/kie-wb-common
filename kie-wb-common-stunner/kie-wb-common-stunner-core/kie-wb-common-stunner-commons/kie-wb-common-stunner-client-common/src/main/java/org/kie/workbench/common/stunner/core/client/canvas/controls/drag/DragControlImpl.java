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

package org.kie.workbench.common.stunner.core.client.canvas.controls.drag;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvas;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.Point2D;
import org.kie.workbench.common.stunner.core.client.canvas.controls.AbstractCanvasHandlerRegistrationControl;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommand;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandFactory;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandManager;
import org.kie.workbench.common.stunner.core.client.command.CanvasCommandResultBuilder;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.shape.Shape;
import org.kie.workbench.common.stunner.core.client.shape.view.HasEventHandlers;
import org.kie.workbench.common.stunner.core.client.shape.view.ShapeView;
import org.kie.workbench.common.stunner.core.client.shape.view.event.DragEvent;
import org.kie.workbench.common.stunner.core.client.shape.view.event.DragHandler;
import org.kie.workbench.common.stunner.core.client.shape.view.event.MouseEnterEvent;
import org.kie.workbench.common.stunner.core.client.shape.view.event.MouseEnterHandler;
import org.kie.workbench.common.stunner.core.client.shape.view.event.MouseExitEvent;
import org.kie.workbench.common.stunner.core.client.shape.view.event.MouseExitHandler;
import org.kie.workbench.common.stunner.core.client.shape.view.event.ViewEventType;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.command.util.CommandUtils;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.util.GraphUtils;

@Dependent
public class DragControlImpl extends AbstractCanvasHandlerRegistrationControl<AbstractCanvasHandler>
        implements DragControl<AbstractCanvasHandler, Element> {

    private static Logger LOGGER = Logger.getLogger(DragControlImpl.class.getName());

    private static final int delta = 10;

    private final CanvasCommandFactory<AbstractCanvasHandler> canvasCommandFactory;
    private CommandManagerProvider<AbstractCanvasHandler> commandManagerProvider;

    protected final double[] dragShapeSize = new double[]{0, 0};

    protected DragControlImpl() {
        this(null);
    }

    @Inject
    public DragControlImpl(final CanvasCommandFactory<AbstractCanvasHandler> canvasCommandFactory) {
        this.canvasCommandFactory = canvasCommandFactory;
    }

    @Override
    public void setCommandManagerProvider(final CommandManagerProvider<AbstractCanvasHandler> provider) {
        this.commandManagerProvider = provider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void register(final Element element) {
        if (checkNotRegistered(element)) {
            final AbstractCanvas<?> canvas = canvasHandler.getAbstractCanvas();
            final Shape<?> shape = canvas.getShape(element.getUUID());
            if (shape.getShapeView() instanceof HasEventHandlers) {
                // Register the drag handler.
                final HasEventHandlers hasEventHandlers = (HasEventHandlers) shape.getShapeView();
                final DragHandler handler = new DragHandler() {

                    @Override
                    public void start(final DragEvent event) {
                        doDragStart(element);
                    }

                    @Override
                    public void handle(final DragEvent event) {
                        doDragUpdate(element);
                    }

                    @Override
                    public void end(final DragEvent event) {
                        doDragEnd(element);
                    }
                };
                hasEventHandlers.addHandler(ViewEventType.DRAG,
                                            handler);
                registerHandler(element.getUUID(),
                                handler);
                // Change mouse cursor, if shape supports it.
                if (hasEventHandlers.supports(ViewEventType.MOUSE_ENTER) &&
                        hasEventHandlers.supports(ViewEventType.MOUSE_EXIT)) {
                    final MouseEnterHandler overHandler = new MouseEnterHandler() {
                        @Override
                        public void handle(MouseEnterEvent event) {
                            canvasHandler.getAbstractCanvas().getView().setCursor(AbstractCanvas.Cursors.MOVE);
                        }
                    };
                    hasEventHandlers.addHandler(ViewEventType.MOUSE_ENTER,
                                                overHandler);
                    registerHandler(shape.getUUID(),
                                    overHandler);
                    final MouseExitHandler outHandler = new MouseExitHandler() {
                        @Override
                        public void handle(MouseExitEvent event) {
                            canvasHandler.getAbstractCanvas().getView().setCursor(AbstractCanvas.Cursors.AUTO);
                        }
                    };
                    hasEventHandlers.addHandler(ViewEventType.MOUSE_EXIT,
                                                outHandler);
                    registerHandler(shape.getUUID(),
                                    outHandler);
                }
            }
        }
    }

    protected void doDragStart(final Element element) {
        final double[] size = GraphUtils.getNodeSize((View) element.getContent());
        dragShapeSize[0] = size[0];
        dragShapeSize[1] = size[1];
    }

    protected void doDragUpdate(final Element element) {
        final AbstractCanvas<?> canvas = canvasHandler.getAbstractCanvas();
        final Shape<?> shape = canvas.getShape(element.getUUID());
        ensureDragConstrains(shape.getShapeView());
    }

    protected void doDragEnd(final Element element) {
        final AbstractCanvas<?> canvas = canvasHandler.getAbstractCanvas();
        final Shape<?> shape = canvas.getShape(element.getUUID());
        final double x = shape.getShapeView().getShapeX();
        final double y = shape.getShapeView().getShapeY();
        move(element,
             x,
             y);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandResult<CanvasViolation> move(final Element element,
                                               final double tx,
                                               final double ty) {
        final CanvasCommand<AbstractCanvasHandler> c = canvasCommandFactory.updatePosition((Node<View<?>, Edge>) element,
                                                                                           tx,
                                                                                           ty);
        CommandResult<CanvasViolation> result = getCommandManager().allow(canvasHandler,
                                                                          c);
        if (!CommandUtils.isError(result)) {
            result = getCommandManager().execute(canvasHandler,
                                                 c);
        }
        if (CommandUtils.isError(result)) {
            LOGGER.log(Level.SEVERE,
                       "Update element's position command failed [result=" + result + "]");
        }
        return result;
    }

    public CommandResult<CanvasViolation> moveUp(final Element element) {
        return translate(element,
                         0,
                         -delta);
    }

    public CommandResult<CanvasViolation> moveDown(final Element element) {
        return translate(element,
                         0,
                         delta);
    }

    public CommandResult<CanvasViolation> moveLeft(final Element element) {
        return translate(element,
                         -delta,
                         0);
    }

    public CommandResult<CanvasViolation> moveRight(final Element element) {
        return translate(element,
                         delta,
                         0);
    }

    public CommandResult<CanvasViolation> translate(final Element element,
                                                    final double dx,
                                                    final double dy) {
        final Point2D p;
        try {
            p = GraphUtils.getPosition((View) element.getContent());
        } catch (ClassCastException e) {
            LOGGER.log(Level.WARNING,
                       "Update element's position command only cannot be applied to View elements.");
            return CanvasCommandResultBuilder.FAILED;
        }
        final double tx = p.getX() + dx;
        final double ty = p.getY() + dy;
        return move(element,
                    tx,
                    ty);
    }

    @Override
    protected void doDisable() {
        super.doDisable();
        commandManagerProvider = null;
    }

    /**
     * Setting dragBounds for the shape doesn't work on lienzo side, so
     * ensure drag does not exceed the canvas bounds.
     * @param shapeView The shape view instance being drag.
     */
    private void ensureDragConstrains(final ShapeView<?> shapeView) {
        final int mw = canvasHandler.getCanvas().getWidth();
        final int mh = canvasHandler.getCanvas().getHeight();
        final Point2D sa = shapeView.getShapeAbsoluteLocation();
        LOGGER.log(Level.FINE,
                   "Ensuring drag constraints for absolute coordinates at [" + sa.getX() + ", " + sa.getY() + "]");
        final double ax = mw - dragShapeSize[0];
        final double ay = mh - dragShapeSize[1];
        final boolean xb = sa.getX() >= ax || sa.getX() < 0;
        final boolean yb = sa.getY() >= ay || sa.getY() < 0;
        if (xb || yb) {
            final double tx = sa.getX() >= ax ? ax : (sa.getX() < 0 ? 0 : sa.getX());
            final double ty = sa.getY() >= ay ? ay : (sa.getY() < 0 ? 0 : sa.getY());
            LOGGER.log(Level.FINE,
                       "Setting constraint coordinates at [" + tx + ", " + ty + "]");
            shapeView.setShapeX(tx);
            shapeView.setShapeY(ty);
        }
    }

    private CanvasCommandManager<AbstractCanvasHandler> getCommandManager() {
        return commandManagerProvider.getCommandManager();
    }
}
