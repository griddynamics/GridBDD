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

package com.griddynamics.qa.sprimber.reporting;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author fparamonov
 */

@Component
public class StepDefinitionFormatter {

    public String formatStepName(StepDefinition stepDefinition) {
        if (isGeneralStep(stepDefinition) || isHookStep(stepDefinition)) {
            return StringUtils.isBlank(stepDefinition.getName()) ? stepDefinition.getMethod().getName() : stepDefinition.getName();
        } else {
            return StringUtils.isBlank(stepDefinition.getResolvedTextPattern()) ? stepDefinition.getName() : stepDefinition.getResolvedTextPattern();
        }
    }

    public boolean isHookStep(StepDefinition stepDefinition) {
        return stepDefinition.getStepType().equals(StepDefinition.StepType.BEFORE) || stepDefinition.getStepType().equals(StepDefinition.StepType.AFTER);
    }

    public boolean isGeneralStep(StepDefinition stepDefinition) {
        return stepDefinition.getStepType().equals(StepDefinition.StepType.GENERAL);
    }
}
