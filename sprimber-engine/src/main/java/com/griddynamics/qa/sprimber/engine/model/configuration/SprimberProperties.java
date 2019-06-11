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

package com.griddynamics.qa.sprimber.engine.model.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fparamonov
 */
@ConfigurationProperties("sprimber.configuration")
public class SprimberProperties {

    private List<String> tagFilters = new ArrayList<>();
    private String featurePath;
    private PoolExecutorProperties tcExecutorPool =
            new PoolExecutorProperties(3, 3, 60, "TC-Executor-");
    private PoolExecutorProperties tsExecutorPool =
            new PoolExecutorProperties(1, 1, 60, "TS-Executor-");

    public String getFeaturePath() {
        return featurePath;
    }

    public void setFeaturePath(String featurePath) {
        this.featurePath = featurePath;
    }

    public List<String> getTagFilters() {
        return tagFilters;
    }

    public void setTagFilters(List<String> tagFilters) {
        this.tagFilters = tagFilters;
    }

    public PoolExecutorProperties getTcExecutorPool() {
        return tcExecutorPool;
    }

    public void setTcExecutorPool(PoolExecutorProperties tcExecutorPool) {
        this.tcExecutorPool = tcExecutorPool;
    }

    public PoolExecutorProperties getTsExecutorPool() {
        return tsExecutorPool;
    }

    public void setTsExecutorPool(PoolExecutorProperties tsExecutorPool) {
        this.tsExecutorPool = tsExecutorPool;
    }

    public static class PoolExecutorProperties {
        private int coreSize;
        private int maxSIze;
        private int keepAliveSeconds;
        private String threadNamePrefix;

        public PoolExecutorProperties(int coreSize, int maxSIze, int keepAliveSeconds, String threadNamePrefix) {
            this.coreSize = coreSize;
            this.maxSIze = maxSIze;
            this.keepAliveSeconds = keepAliveSeconds;
            this.threadNamePrefix = threadNamePrefix;
        }

        public int getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }

        public int getMaxSIze() {
            return maxSIze;
        }

        public void setMaxSIze(int maxSIze) {
            this.maxSIze = maxSIze;
        }

        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        public void setKeepAliveSeconds(int keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
        }

        public String getThreadNamePrefix() {
            return threadNamePrefix;
        }

        public void setThreadNamePrefix(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }
    }
}
