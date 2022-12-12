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

class VariableSpec extends Specification {

    def "should allow variables to be passed and resolved within DSL"() {
        expect:
        GrooCSS.convert(new Config().withVariables(kolor: '#123'), "table { color kolor }").toString() ==
                "table{color: #123;}"
        GrooCSS.withConfig { withVariables([kolor: '#123']) }.convert("table { color kolor }").toString() ==
                "table{color: #123;}"
    }

    def "should allow variables to be passed to withVariable and resolved within DSL"() {
        expect:
        GrooCSS.convert(new Config().withVariable('kolor', '#123'), "table { color kolor }").toString() ==
                "table{color: #123;}"
    }

    def "should allow variables to be passed to withVariables2 and resolved within DSL"() {
        expect:
        GrooCSS.convert(new Config().withVariables('kolor', '#123', 'w', 200.px), "table { color kolor width w}")
                .toString() == "table{color: #123;\n\twidth: 200px;}"
    }


    def "should allow variables to be passed to convert(Config, File, File) and resolved within DSL"() {
        given:
        def input = File.createTempFile("input", ".css.groovy")
        def output = File.createTempFile("output", ".css")
        input.text = "table { color kolor }"
        when:
        GrooCSS.convert(Config.builder().variables([kolor: '#123']).build(), input, output)
        then:
        "table{color: #123;}" == output.text
        output.delete()
        input.delete()
    }

    def "should allow variables to be passed to convertWithoutBase and resolved within DSL"() {
        given:
        def input = File.createTempFile("input", ".css.groovy")
        def output = File.createTempFile("output", ".css")
        input.text = "''.groocss { table { color kolor } }"
        when:
        GrooCSS.convertWithoutBase(input, output, "UTF-8", [kolor: '#123'])
        then:
        "table{color: #123;}" == output.text
        output.delete()
        input.delete()
    }


}
