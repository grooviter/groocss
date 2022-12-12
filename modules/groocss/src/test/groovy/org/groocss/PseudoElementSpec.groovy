/*
 * Copyright [2019] [Adam L. Davis]
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

import org.groocss.proc.PlaceholderProcessor
import spock.lang.Specification
import spock.lang.Unroll

/** Tests for pseudo-elements such as ::before and ::after. */
class PseudoElementSpec extends Specification {

    def "should create style group with _**placeholder"() {
        when:
        def css = GrooCSS.process {
            get_()**placeholder { color blue }
        }.css
        then:
        "$css" == "::placeholder{color: Blue;}"
    }

    def "should create style group with _**before"() {
        when:
        def css = GrooCSS.process { get_()**before { color blue } }.css
        then:
        "$css" == "::before{color: Blue;}"
    }

    def "should create style group with _**after"() {
        when:
        def css = GrooCSS.process { get_()**after { color blue } }.css
        then:
        "$css" == "::after{color: Blue;}"
    }

    def "should create style group with _%before"() {
        when:
        def css = GrooCSS.process { get_()%before { color blue } }.css
        then:
        "$css" == "::before{color: Blue;}"
    }

    def "should create style group with _%after"() {
        when:
        def css = GrooCSS.process { get_()%after { color blue } }.css
        then:
        "$css" == "::after{color: Blue;}"
    }

    def "should create style group with p**firstLine"() {
        when:
        def css = GrooCSS.process { p**firstLine { color red } }.css
        then:
        "$css" == "p::first-line{color: Red;}"
    }

    @Unroll
    def "should create a pseudo-element with #name"() {
        expect:
        GrooCSS.process closure
        where:
        name || closure
        'after' || { after instanceof PseudoElement }
        'before' || { before instanceof PseudoElement }
        'placeholder' || { placeholder instanceof PseudoElement }
        'cue' || { cue instanceof PseudoElement }
        'backdrop' || { backdrop instanceof PseudoElement }
        'firstLine' || { firstLine instanceof PseudoElement }
        'firstLetter' || { firstLetter instanceof PseudoElement }
        'selection' || { selection instanceof PseudoElement }
    }


    def "should be able to chain pseudo-element and pseudo-class"() {
        when:
        def css = GrooCSS.process { p **firstLine %hover { color red } }.css
        then:
        "$css" == "p::first-line:hover{color: Red;}"
    }

    def "should be able to chain pseudo-element and last-child"() {
        when:
        def css = GrooCSS.process { p **firstLine %lastChild { color red } }.css
        then:
        "$css" == "p::first-line:last-child{color: Red;}"
    }

    def "should be able to chain pseudo-element and last-of-type"() {
        when:
        def css = GrooCSS.process { p **firstLine %lastOfType { color red } }.css
        then:
        "$css" == "p::first-line:last-of-type{color: Red;}"
    }

    def "should make copy of placeholder with -webkit-input- prefix with PlaceholderProcessor"() {
        when:
        def css = GrooCSS.process(new Config().withProcessors([new PlaceholderProcessor()])) {

            input**placeholder { color red }

        }.css
        then:
        "$css" == "input::placeholder{color: Red;}\ninput::-webkit-input-placeholder{color: Red;}"
    }

}
