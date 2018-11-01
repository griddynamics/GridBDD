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

import java.util.HashMap;
import java.util.Optional;

/**
 * This class represent the place where test case related objects can be stored.
 * This class should not be directly instantiated and should be used via {@link TestCaseContextHolder}
 *
 * @author fparamonov
 */

public class TestCaseContext extends HashMap<String, Object> {

    /**
     * Return the value for the scoped object of the given name, if any.
     *
     * @param name the name of the object
     * @return the current object value, or {@code Optional.empty} if not found
     */
    public Optional<Object> getOptionalObject(String name) {
        return Optional.ofNullable(get(name));
    }

    /**
     * Set the value for the scoped object of the given name,
     * replacing an existing value (if any).
     *
     * @param name  the name of the object
     * @param value the value for the object
     */
    public void putObject(String name, Object value) {
        put(name, value);
    }

    /**
     * Remove the scoped object of the given name, if it exists.
     * <p>Note that an implementation should also remove a registered destruction
     * callback for the specified object, if any. It does, however, <i>not</i>
     * need to <i>execute</i> a registered destruction callback in this case,
     * since the object will be destroyed by the caller (if appropriate).
     *
     * @param name the name of the object
     */
    public void removeObject(String name) {
        remove(name);
    }
}
