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

import org.groocss.valid.DefaultValidator
import org.groocss.valid.RequireMeasurements
import spock.lang.Specification
import spock.lang.Unroll

class ValidationSpec extends Specification {

    def "rotate should throw AssertionError for non-angle Measurements"() {
        when:
        GrooCSS.process closure
        then:
        thrown(AssertionError)
        where:
        closure << [{ p { rotate 12.px } }, { p { rotate 12.ms } }, { p { rotate 12.cm } }]
    }

    def "minWidth should throw AssertionError for non-length Measurements"() {
        when:
        GrooCSS.process closure
        then:
        thrown(AssertionError)
        where:
        closure << [{ p { minWidth 12.deg } }, { p { minWidth 12.rad } }, { p { minWidth 12.ms } }]
    }

    def "height should not throw AssertionError for percent Measurements"() {
        when:
        GrooCSS.process { p { height 10%_ } }
        then:
        notThrown(AssertionError)
    }

    @Unroll
    def "#name should throw AssertionError for non-length Measurements"() {
        when:
        GrooCSS.process { p { "$name"(value) } }
        then:
        thrown(AssertionError)
        where:
        name << ['top', 'left', 'right', 'width', 'height', 'minWidth', 'minHeight', 'fontSize']*3
        value << [12.ms, 1.rad, 20.deg]*8
    }

    @Unroll
    def "#name should throw AssertionError for non-time measurements"() {
        when:
        GrooCSS.process { p { "$name"(value) } }
        then:
        thrown(AssertionError)
        where:
        name << ['animationDelay', 'animationDuration', 'transitionDuration']*2
        value << [1.px, 2.em, 2.pt, 1.mm, 2.pt, 1.mm]
    }

    @Newify([RequireMeasurements])
    @Unroll
    def "#name should throw AssertionError for non-measurements"() {
        when:
        GrooCSS.process(new Config(processors: [RequireMeasurements()])) { p { "$name"('1px') } }
        then:
        thrown(AssertionError)
        where:
        name << DefaultValidator.sizeNames + DefaultValidator.timeNames
    }

    def "converting a file should also validate using validator"() {
        given:
        def temp = new File('build/temp')
        temp.mkdirs()
        when:
        def file = new File(temp,'test.groocss')
        def file2 = new File(temp,'test.css')
        file.text = '\'.a\'.sg { width 10.ms }'
        GrooCSS.process(file.newInputStream(), file2.newOutputStream())
        then:
        thrown(AssertionError)
    }

}
