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

import spock.lang.Specification

/**
 * Created by adavis on 8/10/17.
 */
class CommentSpec extends Specification {

    def "comment should include comment in output"() {
        given:
        def css = GrooCSS.process {
            comment 'I am Groocss!'
        }
        expect:
        '/* I am Groocss! */' == "$css"
    }

    def "comment should print in order"() {
        given:
        def css = GrooCSS.process {
            comment 'comment1'
            a { fontSize 18.px }
            comment 'comment2'
            aside { fontSize 15.px }
        }
        expect:
        "$css" == '''
        /* comment1 */
        a{font-size: 18px;}
        /* comment2 */
        aside{font-size: 15px;}'''.stripIndent().trim()
    }
}
