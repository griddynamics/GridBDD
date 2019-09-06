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

package com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase;

import com.griddynamics.qa.sprimber.engine.model.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.model.TestCase;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author fparamonov
 */
//@Aspect
//@Component
public class TestCaseAdvice {

    private final ApplicationEventPublisher eventPublisher;

    public TestCaseAdvice(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

//    @Around("com.griddynamics.qa.sprimber.lifecycle.model.executor.testcase.TestCasePointcut.testCaseExecution(testCase)")
    public Object surroundTestCaseExecution(ProceedingJoinPoint joinPoint, TestCase testCase) throws Throwable {
        TestCaseStartedEvent startedEvent = new TestCaseStartedEvent(joinPoint.getTarget());
        TestCaseFinishedEvent finishedEvent = new TestCaseFinishedEvent(joinPoint.getTarget());
        startedEvent.setTestCase(testCase);
        finishedEvent.setTestCase(testCase);
        eventPublisher.publishEvent(startedEvent);
        Object result = joinPoint.proceed();
        finishedEvent.setExecutionResult((ExecutionResult) result);
        eventPublisher.publishEvent(finishedEvent);
        return result;
    }
}
