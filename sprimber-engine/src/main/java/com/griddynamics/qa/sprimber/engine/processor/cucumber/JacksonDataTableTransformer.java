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

package com.griddynamics.qa.sprimber.engine.processor.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This is a default implementation for any case data table transformer that currently missed in official Cucumber release
 *
 * @author fparamonov
 */

@Component
public class JacksonDataTableTransformer implements TableEntryByTypeTransformer, TableCellByTypeTransformer {

    private final ObjectMapper dataTableObjectMapper;

    public JacksonDataTableTransformer(ObjectMapper dataTableObjectMapper) {
        this.dataTableObjectMapper = dataTableObjectMapper;
    }

    @Override
    public <T> T transform(String value, Class<T> cellType) throws Throwable {
        return dataTableObjectMapper.convertValue(value, cellType);
    }

    @Override
    public <T> T transform(Map<String, String> entry, Class<T> type, TableCellByTypeTransformer cellTransformer) throws Throwable {
        return dataTableObjectMapper.convertValue(entry, type);
    }
}
