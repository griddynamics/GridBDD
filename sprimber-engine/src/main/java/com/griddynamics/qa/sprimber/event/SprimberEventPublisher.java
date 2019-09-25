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

import com.griddynamics.qa.sprimber.common.TestSuite;
import com.griddynamics.qa.sprimber.event.Events.StepEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author fparamonov
 */

@RequiredArgsConstructor
public class SprimberEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void stepStarted(Object target, TestSuite.Step step) {
        StepStartedEvent stepStartedEvent = new StepStartedEvent(target, step);
        eventPublisher.publishEvent(stepStartedEvent);
    }

    public void stepFinished(Object target, TestSuite.Step step) {
        StepFinishedEvent stepFinishedEvent = new StepFinishedEvent(target, step);
        eventPublisher.publishEvent(stepFinishedEvent);
    }

    public void testStarted(Object target, TestSuite.Test test) {
        if (test.getSteps().isEmpty()) {
            return;
        }
        TestStartedEvent testStartedEvent = new TestStartedEvent(target, test);
        eventPublisher.publishEvent(testStartedEvent);
    }

    public void testFinished(Object target, TestSuite.Test test) {
        if (test.getSteps().isEmpty()) {
            return;
        }
        TestFinishedEvent testFinishedEvent = new TestFinishedEvent(target, test);
        eventPublisher.publishEvent(testFinishedEvent);
    }

    public void testCaseStarted(Object target, TestSuite.TestCase testCase) {
        if (testCase.getTests().isEmpty()) {
            return;
        }
        TestCaseStartedEvent testCaseStartedEvent = new TestCaseStartedEvent(target, testCase);
        eventPublisher.publishEvent(testCaseStartedEvent);
    }

    public void testCaseFinished(Object target, TestSuite.TestCase testCase) {
        if (testCase.getTests().isEmpty()) {
            return;
        }
        TestCaseFinishedEvent testCaseFinishedEvent = new TestCaseFinishedEvent(target, testCase);
        eventPublisher.publishEvent(testCaseFinishedEvent);
    }

    public void testSuiteStarted(Object target, TestSuite testSuite) {
        TestSuiteStartedEvent testSuiteStartedEvent = new TestSuiteStartedEvent(target, testSuite);
        eventPublisher.publishEvent(testSuiteStartedEvent);
    }

    public void testSuiteFinished(Object target, TestSuite testSuite) {
        TestSuiteFinishedEvent testSuiteFinishedEvent = new TestSuiteFinishedEvent(target, testSuite);
        eventPublisher.publishEvent(testSuiteFinishedEvent);
    }

    public class StepStartedEvent extends StepEvent {

        StepStartedEvent(Object source) {
            super(source);
        }

        StepStartedEvent(Object source, TestSuite.Step step) {
            super(source, step);
        }
    }

    public class StepFinishedEvent extends StepEvent {

        StepFinishedEvent(Object source) {
            super(source);
        }

        StepFinishedEvent(Object source, TestSuite.Step step) {
            super(source, step);
        }
    }

    public class TestStartedEvent extends Events.TestEvent {
        TestStartedEvent(Object source) {
            super(source);
        }

        TestStartedEvent(Object source, TestSuite.Test test) {
            super(source, test);
        }
    }

    public class TestFinishedEvent extends Events.TestEvent {
        TestFinishedEvent(Object source) {
            super(source);
        }

        TestFinishedEvent(Object source, TestSuite.Test test) {
            super(source, test);
        }
    }

    public class TestCaseStartedEvent extends Events.TestCaseEvent {
        TestCaseStartedEvent(Object source) {
            super(source);
        }

        TestCaseStartedEvent(Object source, TestSuite.TestCase testCase) {
            super(source, testCase);
        }
    }

    public class TestCaseFinishedEvent extends Events.TestCaseEvent {

        TestCaseFinishedEvent(Object source) {
            super(source);
        }

        TestCaseFinishedEvent(Object source, TestSuite.TestCase testCase) {
            super(source, testCase);
        }
    }

    public class TestSuiteStartedEvent extends Events.TestSuiteEvent {
        TestSuiteStartedEvent(Object source) {
            super(source);
        }

        TestSuiteStartedEvent(Object source, TestSuite testSuite) {
            super(source, testSuite);
        }
    }

    public class TestSuiteFinishedEvent extends Events.TestSuiteEvent {
        TestSuiteFinishedEvent(Object source) {
            super(source);
        }

        TestSuiteFinishedEvent(Object source, TestSuite testSuite) {
            super(source, testSuite);
        }
    }
}
