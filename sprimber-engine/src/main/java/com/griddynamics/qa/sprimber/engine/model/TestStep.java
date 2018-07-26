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

package com.griddynamics.qa.sprimber.engine.model;

import com.griddynamics.qa.sprimber.engine.model.action.ActionDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will represent actual steps in scenario. Main difference between steep and hook, that first one should
 * handle arguments that should be passed for method invocation, rather then hooks will be represented directly by
 * action definitions, since methods should not accept any arguments
 *
 * @author fparamonov
 */
public class TestStep {

    private List<ActionDefinition> beforeStepActions = new ArrayList<>();
    private List<ActionDefinition> afterStepActions = new ArrayList<>();
    private ActionDefinition stepAction;
    // TODO: 08/06/2018 rework this to appropriate type
    private Object[] stepArguments;
    private String actualText;

    public List<ActionDefinition> getBeforeStepActions() {
        return beforeStepActions;
    }

    public List<ActionDefinition> getAfterStepActions() {
        return afterStepActions;
    }

    public ActionDefinition getStepAction() {
        return stepAction;
    }

    public void setStepAction(ActionDefinition stepAction) {
        this.stepAction = stepAction;
    }

    public Object[] getStepArguments() {
        return stepArguments;
    }

    public void setStepArguments(Object[] stepArguments) {
        this.stepArguments = stepArguments;
    }

    public String getActualText() {
        return actualText;
    }

    public void setActualText(String actualText) {
        this.actualText = actualText;
    }
}
