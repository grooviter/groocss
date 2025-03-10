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

import org.junit.After
import spock.lang.Specification

class StringExtensionSpec extends Specification {

    @After
    def nullifyThreadLocal() {
        GrooCSS.threadLocalInstance.set(null) // so we don't pollute
    }

    def "should create GrooCSS dsl from string"() {
        expect:
        'my css'.groocss { section { color red } } instanceof GrooCSS
    }

    def "should create GrooCSS dsl from string with config"() {
        expect:
        'main.css'.groocss(new Config(compress: true)) { body { color red } } instanceof GrooCSS
    }

    def "should create color using .color"() {
        given:
        def gcss = new GrooCSS() // need to do this to set the ThreadLocal value
        expect:
        '123456'.color instanceof Color
    }

    def "should create color using .toColor()"() {
        given:
        def gcss = new GrooCSS() // need to do this to set the ThreadLocal value
        expect:
        '123456'.toColor() instanceof Color
    }

    def "should create SG using .sg {} syntax"() {
        given:
        def gcss = new GrooCSS() // need to do this to set the ThreadLocal value
        expect:
        def s = 'div.test a:hover'.sg { color 'white' }
        s instanceof StyleGroup
        s.selector == 'div.test a:hover'
    }

    def "should create SG using .\$ {} syntax"() {
        given:
        def gcss = new GrooCSS() // need to do this to set the ThreadLocal value
        expect:
        def s = 'div.test a:hover'.$ { color 'white' }
        s instanceof StyleGroup
        s.selector == 'div.test a:hover'
    }

    def "should create SG using .id {} syntax"() {
        given:
        def gcss = new GrooCSS() // need to do this to set the ThreadLocal value
        expect:
        def s = 'test'.id { color whiteSmoke }
        s instanceof StyleGroup
        s.selector == '#test'
    }

    def "should create KeyFrames using .keyframes {} syntax"() {
        given:
        def gcss = new GrooCSS() // need to do this to set the ThreadLocal value
        expect:
        def s = 'test'.keyframes { 10% { color white } }
        s instanceof KeyFrames
        s.name == 'test'
    }

    def "should create Media using .media {} syntax"() {
        given:
        def gcss = new GrooCSS() // need to do this to set the ThreadLocal value
        expect:
        def s = 'test'.media { body { fontSize 15.px } }
        s instanceof MediaCSS
        s.mediaRule == 'test'
    }
}
