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

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author fparamonov
 */
public interface TestSuiteDiscovery {

    Statistic getDiscoveredInfo();

    Node discover();

    String name();

    class Statistic extends HashMap<String, AtomicInteger> {

        private static final String FILTERED_PREFIX = "filter";
        private static final String PREPARED_PREFIX = "prepared";

        public void registerFilteredStage(String stageRole) {
            putIfAbsent(FILTERED_PREFIX + stageRole, new AtomicInteger(0));
            get(FILTERED_PREFIX + stageRole).incrementAndGet();
        }

        public void registerPreparedStage(String stageRole) {
            putIfAbsent(PREPARED_PREFIX + stageRole, new AtomicInteger(0));
            get(PREPARED_PREFIX + stageRole).incrementAndGet();
        }

        public int filteredCountByStage(String stageRole) {
            return getOrDefault(FILTERED_PREFIX + stageRole, new AtomicInteger(0)).get();
        }

        public int preparedCountByStage(String stageRole) {
            return getOrDefault(PREPARED_PREFIX + stageRole, new AtomicInteger(0)).get();
        }

        public void accumulate(Statistic statistic) {
            statistic.keySet().forEach(key -> {
                int newValue = getOrDefault(key, new AtomicInteger(0)).addAndGet(statistic.get(key).get());
                put(key, new AtomicInteger(newValue));
            });
        }
    }
}
