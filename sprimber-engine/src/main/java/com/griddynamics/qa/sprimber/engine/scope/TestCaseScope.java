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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * A simple thread-backed {@link Scope} implementation.
 * <p>
 * This scope automatically registered by Sprimber auto configuration.
 * To disable or explicitly enable it property {@code sprimber.configuration.custom.scopes.enable} exist
 * <p>
 * This scope works in assumption that each test case executed in isolated thread(basic concept from Sprimber).
 * It allow to register registry that will hold all instances of statefull object that required by test case.
 * <p>
 * Simple use case: when step fetch some attributes from application and then validation in then step should happens.
 * TestCaseScope help to safely share variables across different threads and scenarios. So, beans that marked
 * with this scope will be created at the beginning of the scenario and destroyed at the end.
 *
 * @author fparamonov
 */

public class TestCaseScope implements Scope {

    public static final String TEST_CASE_SCOPE_NAME = "testcase";
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseScope.class);

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        TestCaseContext testCaseContext = TestCaseContextHolder.getCurrentContext();
        Object scopedObject = testCaseContext.getOptionalObject(name).orElseGet(objectFactory::getObject);
        testCaseContext.putObject(name, scopedObject);
        return scopedObject;
    }

    @Override
    public Object remove(String name) {
        TestCaseContext testCaseContext = TestCaseContextHolder.getCurrentContext();
        testCaseContext.getOptionalObject(name)
                .ifPresent((object) -> testCaseContext.removeObject(name));
        return testCaseContext.getOptionalObject(name).orElse(null);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        LOGGER.warn("TestCaseScope does not support destruction callbacks. " +
                "Consider using other scopes.");
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
