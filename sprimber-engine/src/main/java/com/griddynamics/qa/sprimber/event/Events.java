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

package com.griddynamics.qa.sprimber.event;

import com.griddynamics.qa.sprimber.discovery.step.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.testsuite.TestSuiteDefinition;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * This class is a container for all typical events during Sprimber lifecycle
 *
 * @author fparamonov
 */
public class Events {

    @Getter
    @Setter
    public static abstract class StepEvent extends ApplicationEvent {

        private StepDefinition stepDefinition;

        /**
         * Create a new ApplicationEvent.
         *
         * @param source the object on which the event initially occurred (never {@code null})
         */
        public StepEvent(Object source) {
            super(source);
        }

        public StepEvent(Object source, StepDefinition stepDefinition) {
            super(source);
            this.stepDefinition = stepDefinition;
        }
    }

    @Getter
    @Setter
    public static abstract class TestEvent extends ApplicationEvent {

        private TestSuiteDefinition.TestDefinition testDefinition;

        /**
         * Create a new ApplicationEvent.
         *
         * @param source the object on which the event initially occurred (never {@code null})
         */
        public TestEvent(Object source) {
            super(source);
        }

        public TestEvent(Object source, TestSuiteDefinition.TestDefinition testDefinition) {
            super(source);
            this.testDefinition = testDefinition;
        }
    }

    @Getter
    @Setter
    public static abstract class TestCaseEvent extends ApplicationEvent {

        private TestSuiteDefinition.TestCaseDefinition testCaseDefinition;

        /**
         * Create a new ApplicationEvent.
         *
         * @param source the object on which the event initially occurred (never {@code null})
         */
        public TestCaseEvent(Object source) {
            super(source);
        }

        public TestCaseEvent(Object source, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
            super(source);
            this.testCaseDefinition = testCaseDefinition;
        }
    }

    @Getter
    @Setter
    public static abstract class TestSuiteEvent extends ApplicationEvent {

        private TestSuiteDefinition testSuiteDefinition;

        /**
         * Create a new ApplicationEvent.
         *
         * @param source the object on which the event initially occurred (never {@code null})
         */
        public TestSuiteEvent(Object source) {
            super(source);
        }

        public TestSuiteEvent(Object source, TestSuiteDefinition testSuiteDefinition) {
            super(source);
            this.testSuiteDefinition = testSuiteDefinition;
        }
    }
}
