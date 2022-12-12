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

import spock.lang.Specification

class ExtendSpec extends Specification {


    def "should extend StyleGroup"() {
        when:
        def css = GrooCSS.process {
            sg '.a', {
                color black
                background white
            }
            sg '.b', {
                extend '.a'
                color blue
            }
        }
        then:
        "$css" == ".a,.b{color: Black;\n\tbackground: White;}\n.b{color: Blue;}"
    }

    def "should extend StyleGroup twice "() {
        when:
        def css = GrooCSS.process {
            sg '.a', {
                color black
            }
            sg '.b', { extend '.a' }
            sg '.c', { extend '.a' }
        }
        then:
        "$css" == ".a,.b,.c{color: Black;}"
    }

    def "should extend using dsl"() {
        when:
        def css = GrooCSS.process {
            input {
                color black
                background white
            }
            sg '.b', {
                extend input
                color blue
            }
        }
        then:
        "$css" == "input,.b{color: Black;\n\tbackground: White;}\n.b{color: Blue;}"
    }

    def "should extend using more complex dsl"() {
        when:
        def css = GrooCSS.process {
            input.foo {
                color black
                background white
            }
            sg '.bar', {
                extend(input.foo)
                color blue
            }
        }
        then:
        "$css" == "input.foo,.bar{color: Black;\n\tbackground: White;}\n.bar{color: Blue;}"
    }

    def "you should be able to extend a pseudo-class lazily"() {
        when:
        def css = GrooCSS.process {
            odd { backgroundColor '#eee' }
            li % even { extend(odd) }
        }
        then:
        "$css" == ':nth-child(odd),li:nth-child(even){background-color: #eee;}'
    }

}