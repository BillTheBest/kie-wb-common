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

package org.kie.workbench.common.stunner.core.client.api;

import java.util.ArrayList;
import java.util.List;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.core.client.session.ClientSessionFactory;
import org.kie.workbench.common.stunner.core.client.session.impl.AbstractClientSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class AbstractClientSessionManagerTest {

    @Mock
    AbstractClientSession session;
    @Mock
    AbstractClientSession session1;

    private AbstractClientSessionManager tested;

    @Before
    public void setup() throws Exception {
        this.tested = spy(new AbstractClientSessionManager() {

            @Override
            protected <D extends Diagram> List<ClientSessionFactory<?>> getFactories(final D diagram) {
                return new ArrayList<ClientSessionFactory<?>>(0);
            }

            @Override
            protected void postOpen() {
            }

            @Override
            protected void postPause() {
            }

            @Override
            protected void postResume() {
            }

            @Override
            protected void postDestroy() {
            }
        });
    }

    @Test
    public void testOpen() {
        tested.open(session);
        assertEquals(session,
                     tested.getCurrentSession());
        verify(session,
               times(1)).open();
        verify(session,
               times(0)).pause();
        verify(session,
               times(0)).resume();
        verify(session,
               times(0)).destroy();
        verify(tested,
               times(1)).postOpen();
        verify(tested,
               times(0)).postPause();
        verify(tested,
               times(0)).postResume();
        verify(tested,
               times(0)).postDestroy();
    }

    @Test
    public void testOpenAnotherSession() {
        tested.current = session;
        tested.open(session1);
        assertEquals(session1,
                     tested.getCurrentSession());
        verify(session,
               times(1)).pause();
        verify(session,
               times(0)).open();
        verify(session,
               times(0)).resume();
        verify(session,
               times(0)).destroy();
        verify(session1,
               times(1)).open();
        verify(session1,
               times(0)).pause();
        verify(session1,
               times(0)).resume();
        verify(session1,
               times(0)).destroy();
        verify(tested,
               times(1)).postOpen();
        verify(tested,
               times(1)).postPause();
        verify(tested,
               times(0)).postResume();
        verify(tested,
               times(0)).postDestroy();
    }

    @Test
    public void testPause() {
        tested.current = session;
        tested.pause();
        assertEquals(session,
                     tested.getCurrentSession());
        verify(session,
               times(0)).open();
        verify(session,
               times(1)).pause();
        verify(session,
               times(0)).resume();
        verify(session,
               times(0)).destroy();
        verify(tested,
               times(0)).postOpen();
        verify(tested,
               times(1)).postPause();
        verify(tested,
               times(0)).postResume();
        verify(tested,
               times(0)).postDestroy();
    }

    @Test
    public void testResume() {
        tested.current = session1;
        tested.resume(session);
        assertEquals(session,
                     tested.getCurrentSession());
        verify(session1,
               times(0)).open();
        verify(session1,
               times(1)).pause();
        verify(session1,
               times(0)).resume();
        verify(session1,
               times(0)).destroy();
        verify(session,
               times(0)).open();
        verify(session,
               times(0)).pause();
        verify(session,
               times(1)).resume();
        verify(session,
               times(0)).destroy();
        verify(tested,
               times(0)).postOpen();
        verify(tested,
               times(1)).postPause();
        verify(tested,
               times(1)).postResume();
        verify(tested,
               times(0)).postDestroy();
    }

    @Test
    public void testDestroy() {
        tested.current = session;
        tested.destroy();
        assertNull(tested.getCurrentSession());
        verify(session,
               times(0)).open();
        verify(session,
               times(0)).pause();
        verify(session,
               times(0)).resume();
        verify(session,
               times(1)).destroy();
        verify(tested,
               times(0)).postOpen();
        verify(tested,
               times(0)).postPause();
        verify(tested,
               times(0)).postResume();
        verify(tested,
               times(1)).postDestroy();
    }
}
