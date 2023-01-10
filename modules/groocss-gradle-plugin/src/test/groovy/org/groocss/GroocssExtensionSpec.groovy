/*
 * Copyright 2016-2023 The GrooCSS authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.groocss

import spock.lang.Specification

class GroocssExtensionSpec extends Specification {

    def "should have basic properties" () {
        expect:
        def ext = new GroocssExtension(compress: true, prettyPrint: true)
        ext.compress
        ext.prettyPrint
        ext.addMoz
        ext.addMs
        ext.addOpera
        ext.addWebkit
    }

    def "should have charset default of null" () {
        expect:
        def ext = new GroocssExtension()
        ext.charset == null
    }

    def "should have processors which is empty list by default" () {
        expect:
        def ext = new GroocssExtension()
        ext.processors.empty
        ext.processors instanceof List
    }


    def "should have variables which is empty Map by default" () {
        expect:
        def ext = new GroocssExtension()
        ext.variables.size() == 0
        ext.variables instanceof Map
    }
}
