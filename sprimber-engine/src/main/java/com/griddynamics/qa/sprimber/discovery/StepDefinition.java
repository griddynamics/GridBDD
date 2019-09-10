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

package com.griddynamics.qa.sprimber.discovery;

import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import lombok.Data;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * The step definition present the referenced Java method along with the binding text pattern
 * In Sprimber there is no actual difference between hooks and steps, hooks treated as "hidden" steps
 *
 * @author fparamonov
 */

@Data
public class StepDefinition {

    private String bindingTextPattern;
    private String name;
    private String resolvedTextPattern;
    private ExecutionResult executionResult;
    private Method method;
    private StepType stepType;
    private StepPhase stepPhase;
    private CustomDetails customDetails;
    private Map<String, Object> parameters = new HashMap<>();
    private String hash;

    public String calculateAndSaveHash() {
        String uniqueName = method.getDeclaringClass().getCanonicalName() + "#" +
                method.getName() + "#" +
                method.getParameterCount();
        this.hash = DigestUtils.md5DigestAsHex(uniqueName.getBytes());
        return this.hash;
    }

    /**
     * Sprimber supports the next step types based on the meaning of them.
     * All other step types can be adjusted to one of the base type
     * <ul>
     *     <li>
     *         BEFORE - kind of "hidden" step. Usually some helpful action that should be executed before something
     *     </li>
     *     <li>
     *         GIVEN - kind of "vocal" step. Meaning the precondition or base statement
     *     </li>
     *     <li>
     *         WHEN - kind of "vocal" step. Meaning the mutable action like data manipulation
     *     </li>
     *     <li>
     *         THEN - kind of "vocal" step. Meaning the immutable action like data validation
     *     </li>
     *     <li>
     *         AFTER - kind of "hidden" step. Usually some helpful routine that executed after something
     *     </li>
     *     <li>
     *         GENERAL - kind of step when the exact type doesn't matter
     *     </li>
     * </ul>
     *
     * Any other possible combination are derivative from this, like "AND", "BUT", etc.
     */
    public enum StepType {
        BEFORE, GIVEN, WHEN, THEN, AFTER, GENERAL
    }

    /**
     * Sprimber supports the next phases of lifecycle execution. Step can be assigned to any of this phases
     * But usually all "vocal" steps assigned to the phase step and only "hidden" steps assigned to the other phases
     * <ul>
     *     <li>
     *         GLOBAL - the global phase that happens on application initialization and closing
     *     </li>
     *     <li>
     *         SUITE - depends on different places of test case locations or other factors suite is a large phase
     *     </li>
     *     <li>
     *         TESTCASE - phase that join together several tests. Can be equivalent to the feature/story level in BDD
     *     </li>
     *     <li>
     *         TEST - phase that describing single and atomic validation unit
     *     </li>
     *     <li>
     *         STEP - smallest phase that includes some particular action like step or hook
     *     </li>
     * </ul>
     */
    public enum StepPhase {
        GLOBAL, SUITE, TESTCASE, TEST, STEP
    }

    /**
     * This is a marker interface.
     * JBehave, Cucumber and other BDD frameworks introduced his own step definition annotations.
     * Each of this annotation contains field with step text but other support fields possible.
     * The goal of this class to mark custom support fields holders and collect them into one place
     *
     */
    public interface CustomDetails {}
}
