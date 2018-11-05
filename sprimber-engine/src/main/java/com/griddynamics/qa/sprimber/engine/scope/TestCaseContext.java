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

import org.springframework.beans.factory.ObjectFactory;

import java.util.HashMap;

/**
 * This class represent the place where test case related objects can be stored.
 * This class should not be directly instantiated and should be used via {@link TestCaseContextHolder}
 *
 * @author fparamonov
 */

public class TestCaseContext extends HashMap<String, Object> {

    private static final String CONVERSATION_ID_KEY = "conversationId";

    /**
     * Method to return object by bean name for current thread.
     * If object already initialised then the value for actual key will be returned
     * Otherwise Spring Object factory {@code getObject()} will be invoked and object initialisation happens
     *
     * @param name - target object name
     * @param objectFactory - Spring Object factory
     * @return - new or already exist object for current thread
     */
    public Object getCurrentObjectByName(String name, ObjectFactory objectFactory) {
        return computeIfAbsent(name, (key) -> objectFactory.getObject());
    }

    /**
     * Method to check if object with target name already exist or not. If exist, then mapping will be removed.
     *
     * @param name - target object name
     * @return - null
     */
    public Object removeCurrentObjectByName(String name) {
        return computeIfPresent(name, (k, v) -> null);
    }

    public void saveConversationId(String conversationId) {
        put(CONVERSATION_ID_KEY, conversationId);
    }

    public String getConversationId() {
        return String.valueOf(get(CONVERSATION_ID_KEY));
    }
}
