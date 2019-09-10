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

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.event.Events.StepEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author fparamonov
 */

@Component
@RequiredArgsConstructor
public class SprimberEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void stepStarted(Object target, StepDefinition stepDefinition) {
        StepStartedEvent stepStartedEvent = new StepStartedEvent(target, stepDefinition);
        eventPublisher.publishEvent(stepStartedEvent);
    }

    public void stepFinished(Object target, StepDefinition stepDefinition) {
        StepFinishedEvent stepFinishedEvent = new StepFinishedEvent(target, stepDefinition);
        eventPublisher.publishEvent(stepFinishedEvent);
    }

    public void testStarted(Object target, TestSuiteDefinition.TestDefinition testDefinition) {
        TestStartedEvent testStartedEvent = new TestStartedEvent(target, testDefinition);
        eventPublisher.publishEvent(testStartedEvent);
    }

    public void testFinished(Object target, TestSuiteDefinition.TestDefinition testDefinition) {
        TestFinishedEvent testFinishedEvent = new TestFinishedEvent(target, testDefinition);
        eventPublisher.publishEvent(testFinishedEvent);
    }

    public void testCaseStarted(Object target, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
        TestCaseStartedEvent testCaseStartedEvent = new TestCaseStartedEvent(target, testCaseDefinition);
        eventPublisher.publishEvent(testCaseStartedEvent);
    }

    public void testCaseFinished(Object target, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
        TestCaseFinishedEvent testCaseFinishedEvent = new TestCaseFinishedEvent(target, testCaseDefinition);
        eventPublisher.publishEvent(testCaseFinishedEvent);
    }

    public void testSuiteStarted(Object target, TestSuiteDefinition testSuiteDefinition) {
        TestSuiteStartedEvent testSuiteStartedEvent = new TestSuiteStartedEvent(target, testSuiteDefinition);
        eventPublisher.publishEvent(testSuiteStartedEvent);
    }

    public void testSuiteFinished(Object target, TestSuiteDefinition testSuiteDefinition) {
        TestSuiteFinishedEvent testSuiteFinishedEvent = new TestSuiteFinishedEvent(target, testSuiteDefinition);
        eventPublisher.publishEvent(testSuiteFinishedEvent);
    }

    public class StepStartedEvent extends StepEvent {

        public StepStartedEvent(Object source) {
            super(source);
        }

        public StepStartedEvent(Object source, StepDefinition stepDefinition) {
            super(source, stepDefinition);
        }
    }

    public class StepFinishedEvent extends StepEvent {

        public StepFinishedEvent(Object source) {
            super(source);
        }

        public StepFinishedEvent(Object source, StepDefinition stepDefinition) {
            super(source, stepDefinition);
        }
    }

    public class TestStartedEvent extends Events.TestEvent {
        public TestStartedEvent(Object source) {
            super(source);
        }

        public TestStartedEvent(Object source, TestSuiteDefinition.TestDefinition testDefinition) {
            super(source, testDefinition);
        }
    }

    public class TestFinishedEvent extends Events.TestEvent {
        public TestFinishedEvent(Object source) {
            super(source);
        }

        public TestFinishedEvent(Object source, TestSuiteDefinition.TestDefinition testDefinition) {
            super(source, testDefinition);
        }
    }

    public class TestCaseStartedEvent extends Events.TestCaseEvent {
        public TestCaseStartedEvent(Object source) {
            super(source);
        }

        public TestCaseStartedEvent(Object source, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
            super(source, testCaseDefinition);
        }
    }

    public class TestCaseFinishedEvent extends Events.TestCaseEvent {

        public TestCaseFinishedEvent(Object source) {
            super(source);
        }

        public TestCaseFinishedEvent(Object source, TestSuiteDefinition.TestCaseDefinition testCaseDefinition) {
            super(source, testCaseDefinition);
        }
    }

    public class TestSuiteStartedEvent extends Events.TestSuiteEvent {
        public TestSuiteStartedEvent(Object source) {
            super(source);
        }

        public TestSuiteStartedEvent(Object source, TestSuiteDefinition testSuiteDefinition) {
            super(source, testSuiteDefinition);
        }
    }

    public class TestSuiteFinishedEvent extends Events.TestSuiteEvent {
        public TestSuiteFinishedEvent(Object source) {
            super(source);
        }

        public TestSuiteFinishedEvent(Object source, TestSuiteDefinition testSuiteDefinition) {
            super(source, testSuiteDefinition);
        }
    }
}
