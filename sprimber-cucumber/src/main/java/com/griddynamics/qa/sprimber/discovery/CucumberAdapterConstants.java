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

/**
 * @author fparamonov
 */
public abstract class CucumberAdapterConstants {

    public static final String ADAPTER_NAME = "cucumberDiscovery";
    public static final String CUCUMBER_FEATURE_ROLE = "testCase";
    public static final String CUCUMBER_SUITE_ROLE = "testSuite";
    public static final String CUCUMBER_SCENARIO_ROLE = "test";
    public static final String CUCUMBER_STEP_CONTAINER_ROLE = "stepContainer";
    public static final String CUCUMBER_STEP_ROLE = "step";

    public static final String BEFORE_SUITE_ACTION_STYLE = "Before Suite";
    public static final String AFTER_SUITE_ACTION_STYLE = "After Suite";
    public static final String BEFORE_FEATURE_ACTION_STYLE = "Before Feature";
    public static final String AFTER_FEATURE_ACTION_STYLE = "After Feature";
    public static final String BEFORE_TEST_ACTION_STYLE = "Before Test";
    public static final String AFTER_TEST_ACTION_STYLE = "After Test";
    public static final String BEFORE_STEP_ACTION_STYLE = "Before Step";
    public static final String AFTER_STEP_ACTION_STYLE = "After Step";

    public static final String GIVEN_STEP_ACTION_STYLE = "Given";
    public static final String WHEN_STEP_ACTION_STYLE = "When";
    public static final String THEN_STEP_ACTION_STYLE = "Then";
    public static final String AND_STEP_ACTION_STYLE = "And";
    public static final String BUT_STEP_ACTION_STYLE = "But";

    public static final String TAGS_ATTRIBUTE = "Tags";
    public static final String TIMEOUT_ATTRIBUTE = "Timeout";
    public static final String ORDER_ATTRIBUTE = "Order";

    public static final String TEST_LOCATION_ATTRIBUTE_NAME = "testLocation";
    public static final String BDD_TAGS_ATTRIBUTE_NAME = "bddTags";
    public static final String LOCATION_ATTRIBUTE_NAME = "location";
    public static final String META_ATTRIBUTE_NAME = "meta";
    public static final String STEP_DATA_ATTRIBUTE = "stepData";

    private CucumberAdapterConstants() {
    }
}
