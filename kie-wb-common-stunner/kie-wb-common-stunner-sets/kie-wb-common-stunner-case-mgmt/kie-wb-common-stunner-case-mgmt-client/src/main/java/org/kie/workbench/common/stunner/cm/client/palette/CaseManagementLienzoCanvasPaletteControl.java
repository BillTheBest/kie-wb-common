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

package org.kie.workbench.common.stunner.cm.client.palette;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.client.lienzo.canvas.controls.palette.LienzoCanvasPaletteControl;
import org.kie.workbench.common.stunner.client.lienzo.components.palette.factory.LienzoDefinitionSetPaletteFactory;
import org.kie.workbench.common.stunner.cm.qualifiers.CaseManagementEditor;
import org.kie.workbench.common.stunner.core.client.api.ShapeManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.builder.ElementBuilderControl;
import org.kie.workbench.common.stunner.core.client.canvas.controls.builder.impl.Element;
import org.kie.workbench.common.stunner.core.client.canvas.event.selection.CanvasElementSelectedEvent;
import org.kie.workbench.common.stunner.core.client.service.ClientFactoryService;

@Dependent
@CaseManagementEditor
public class CaseManagementLienzoCanvasPaletteControl extends LienzoCanvasPaletteControl {

    @Inject
    public CaseManagementLienzoCanvasPaletteControl(final LienzoDefinitionSetPaletteFactory paletteFactory,
                                                    final @Element @CaseManagementEditor ElementBuilderControl<AbstractCanvasHandler> elementBuilderControl,
                                                    final ClientFactoryService factoryServices,
                                                    final ShapeManager shapeManager,
                                                    final Event<CanvasElementSelectedEvent> elementSelectedEvent) {
        super(paletteFactory,
              elementBuilderControl,
              factoryServices,
              shapeManager,
              elementSelectedEvent);
    }
}
