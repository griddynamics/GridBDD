/*
Copyright (c) 2010-2018 Grid Dynamics International, Inc. All Rights Reserved
http://www.griddynamics.com

This library is free software; you can redistribute it and/or modify it under the terms of
the GNU Lesser General Public License as published by the Free Software Foundation; either
version 2.1 of the License, or any later version.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

$Id:
@Project:     Sprimber
@Description: Framework that provide bdd engine and bridges for most popular BDD frameworks
 */

package com.griddynamics.qa.sprimber.engine.executor;

import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;
import com.griddynamics.qa.sprimber.engine.model.action.ActionScope;
import com.griddynamics.qa.sprimber.engine.model.action.ActionType;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

/**
 * @author fparamonov
 */
public class MockActionBuilder {

    public static ActionDefinition mockBeforeScenarioAction() {
        ActionDefinition definition = Mockito.mock(ActionDefinition.class);
        BDDMockito.when(definition.getActionType()).thenReturn(ActionType.Before);
        BDDMockito.when(definition.getActionScope()).thenReturn(ActionScope.SCENARIO);
        return definition;
    }

    public static ActionDefinition mockAfterScenarioAction() {
        ActionDefinition definition = Mockito.mock(ActionDefinition.class);
        BDDMockito.when(definition.getActionType()).thenReturn(ActionType.After);
        BDDMockito.when(definition.getActionScope()).thenReturn(ActionScope.SCENARIO);
        return definition;
    }

    public static ActionDefinition mockBeforeStepAction() {
        ActionDefinition definition = Mockito.mock(ActionDefinition.class);
        BDDMockito.when(definition.getActionType()).thenReturn(ActionType.Before);
        BDDMockito.when(definition.getActionScope()).thenReturn(ActionScope.STEP);
        return definition;
    }

    public static ActionDefinition mockAfterStepAction() {
        ActionDefinition definition = Mockito.mock(ActionDefinition.class);
        BDDMockito.when(definition.getActionType()).thenReturn(ActionType.After);
        BDDMockito.when(definition.getActionScope()).thenReturn(ActionScope.STEP);
        return definition;
    }

    public static ActionDefinition mockGivenAction() {
        ActionDefinition definition = Mockito.mock(ActionDefinition.class);
        BDDMockito.when(definition.getActionType()).thenReturn(ActionType.Given);
        BDDMockito.when(definition.getActionScope()).thenReturn(ActionScope.STEP);
        return definition;
    }

    public static ActionDefinition mockWhenAction() {
        ActionDefinition definition = Mockito.mock(ActionDefinition.class);
        BDDMockito.when(definition.getActionType()).thenReturn(ActionType.When);
        BDDMockito.when(definition.getActionScope()).thenReturn(ActionScope.STEP);
        return definition;
    }

    public static ActionDefinition mockThenAction() {
        ActionDefinition definition = Mockito.mock(ActionDefinition.class);
        BDDMockito.when(definition.getActionType()).thenReturn(ActionType.Then);
        BDDMockito.when(definition.getActionScope()).thenReturn(ActionScope.STEP);
        return definition;
    }
}
