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

/**
 * Created by adavis on 8/9/17.
 */
class RawSpec extends Specification {

    def "raw should just include it"() {
        given:
        def css = GrooCSS.process {
            raw '::webkit-blaw { dostuff }'
        }
        expect:
        '::webkit-blaw { dostuff }' == "$css"
    }

    def "raw should print in order"() {
        given:
        def css = GrooCSS.process {
            raw 'raw1'
            raw 'raw2'
        }
        expect:
        'raw1\nraw2' == "$css"
    }
}
