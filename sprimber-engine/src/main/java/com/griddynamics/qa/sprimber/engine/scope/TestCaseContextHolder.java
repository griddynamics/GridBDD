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

package com.griddynamics.qa.sprimber.engine.scope;

import com.griddynamics.qa.sprimber.engine.model.TestCase;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.NamedThreadLocal;

/**
 * Holder for test case context. Provided basic management functions to clean up the state for test case
 * for current scenario, set new state for scenario and retrieve back current test case context
 *
 * @author fparamonov
 */

public abstract class TestCaseContextHolder {

    private static final ThreadLocal<TestCaseContext> TEST_CASE_OBJECTS_HOLDER =
            new NamedThreadLocal<>("Test case objects");

    private TestCaseContextHolder() {
    }

    public static void cleanContext(AbstractBeanFactory beanFactory) {
        destroyScopedBeans(beanFactory);
        TEST_CASE_OBJECTS_HOLDER.remove();
    }

    public static void setupNewContext(TestCase testCase) {
        TestCaseContext testCaseContext = new TestCaseContext();
        testCaseContext.saveConversationId(testCase.getRuntimeId());
        setContext(testCaseContext);
    }

    public static void setContext(TestCaseContext context) {
        TEST_CASE_OBJECTS_HOLDER.set(context);
    }

    public static TestCaseContext getCurrentContext() {
        return TEST_CASE_OBJECTS_HOLDER.get();
    }

    private static void destroyScopedBeans(AbstractBeanFactory beanFactory) {
        TestCaseContext testCaseContext = getCurrentContext();
        testCaseContext.getAllScopedBeanNames().forEach(beanFactory::destroyScopedBean);
    }
}
