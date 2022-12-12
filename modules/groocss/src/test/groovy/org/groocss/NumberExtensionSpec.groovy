/*
 * Copyright [2016] [Adam L. Davis]
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
import org.junit.Before
import spock.lang.Specification

class NumberExtensionSpec extends  Specification {


    GrooCSS gcss

    @Before
    def initThreadLocal1() {
        gcss = new GrooCSS(new Config(addWebkit: false)) // need to do this to set the ThreadLocal value
    }

    @After
    def nullifyThreadLocal() {
        GrooCSS.threadLocalInstance.set(null) // so we don't pollute
    }

    def "should create color using .color"() {
        expect:
        0x123456.color instanceof Color
    }
    def "should create color using .toColor()"() {
        expect:
        0x123456.toColor() instanceof Color
    }

    def "should create keyframes"() {
        expect:
        gcss.keyframes('showIt') {
            10 % { color 'white' }
        }
        gcss.toString().contains "@keyframes showIt {\n10%{color: white;}\n}"
    }

    def "should create measurements"() {
        expect:
        //sizes
        1.px
        0.001.m
        10.cm
        100.mm
        10.pt
        100.rem
        10.em
        11.1.in
        //times
        10.s
        0.5.s
        100.ms
        //trigs
        10.rad
        180.deg
    }

    def "should create percentage using percent"() {
        expect:
        10.percent == new Measurement(10, '%')
    }

    def "should create percentage using % _"() {
        given:
        def a = new Underscore()
        expect:
        10 % a == new Measurement(10, '%')
        ''.groocss { body { width 100%get_() } }.toString() == 'body{width: 100%;}'
    }

}
