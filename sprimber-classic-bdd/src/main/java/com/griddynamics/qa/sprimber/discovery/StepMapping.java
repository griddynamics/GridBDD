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

import com.griddynamics.qa.sprimber.common.StepDefinition;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation for mapping steps from text files onto methods in step-handling classes
 * with flexible method signatures.
 *
 * @author fparamonov
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface StepMapping {

    /**
     * Assign the mapped patterns to the target method
     * Depends on the usecase the pattern cab be straightforward text that provide 1-1 mapping between step
     * and method or can be regular expression or other type of expression that supported by Sprimber
     * or any of extension.
     * In case of "hidden" steps this pattern is optional and can be used to add verbose levels to the reports
     *
     * @return text pattern
     */
    String textPattern() default "";

    @AliasFor("textPattern")
    String name() default "";

    /**
     * Assign the logical meaning of the annotated step
     *
     * @return the one of the supported step types
     */
    StepDefinition.StepType stepType();

    /**
     * The execution phase of annotated step-method. Note that multiple phases work better for "hidden" steps
     * Actual engine implementation will treat any phases different from Step as "hidden"
     * and add the step to the execution queue
     *
     * @return one or several phases where this step can be used
     */
    StepDefinition.StepPhase[] stepPhase() default StepDefinition.StepPhase.STEP;
}
