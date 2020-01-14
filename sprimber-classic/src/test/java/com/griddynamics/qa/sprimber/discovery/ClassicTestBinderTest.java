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


import com.griddynamics.qa.sprimber.engine.Node;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static com.griddynamics.qa.sprimber.discovery.ExampleTestController.*;

/**
 * @author fparamonov
 */
public class ClassicTestBinderTest {

    private static final String DEFAULT_HISTORY_ID = "ee17157fc5eba979689e028b6132883d";

    @Test
    public void testSimpleBinding() {
//        ClassicStepFactory stepFactoryMock = Mockito.mock(ClassicStepFactory.class);
//        Method testMethod = ReflectionUtils.findMethod(ExampleTestController.class, "testMe");
//        BDDMockito.when(stepFactoryMock.provideStepNode(testMethod)).then(invocationOnMock -> buildStep());
//        ClassicTestBinder classicTestBinder = new ClassicTestBinder(stepFactoryMock);
//        Node test = classicTestBinder.bind(testMethod);
//        Assertions.assertThat(test.getName()).isEqualTo(TEST_NAME);
//        Assertions.assertThat(test.getDescription()).isEqualTo(TEST_DESCRIPTION);
//        Assertions.assertThat(test.getHistoryId()).isEqualTo(DEFAULT_HISTORY_ID);
//        Assertions.assertThat(test.getRuntimeId()).isNotEmpty();
    }

//    private Node.ExecutableNode buildStep() {
//        Node.ExecutableNode executableNode = new Node.ExecutableNode("step");
//        executableNode.setName(TEST_STEP_NAME);
//        return executableNode;
//    }
}
