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

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by adavis on 8/10/17.
 */
class ImportSpec extends Specification {
    
    @Shared
    File otherCss
    
    def setup() {
        otherCss = new File(File.createTempDir(),"other.css")
    }

    @Unroll
    def "import should just import it: #name"() {
        given:
        otherCss.text = "a {color blue}"
        expect:
        def css = GrooCSS.process closure
        'a{color: Blue;}' == "$css".trim()
        where:
        name            | closure
        'File name'     | { importFile otherCss.absoluteFile.toString() }
        'File'          | { importFile otherCss.absoluteFile }
        'String'        | { importString otherCss.text }
        'InputStream'   | { importStream otherCss.newInputStream() }
    }

    def "import should be put wherever its put"() {
        given:
        otherCss.text = "a {color blue}"
        expect:
        def css = GrooCSS.process {
            importFile otherCss.absoluteFile
            header { fontSize '15pt' }
        }
        'a{color: Blue;}\nheader{font-size: 15pt;}' == "$css".trim()
    }

    def "import should allow parameters"() {
        given:
        otherCss.text = "a {color linkColor}"
        expect:
        def css = GrooCSS.process {
            importFile otherCss.absoluteFile, linkColor: '#456789'
        }
        'a{color: #456789;}' == "$css".trim()
    }

    def "import should CSS before and after and keep original Config"() {
        given:
        otherCss.text = "a {color linkColor}"
        expect:
        def css = GrooCSS.process(new Config(compress: true)) {
            table { padding 5.px }
            importFile otherCss.absoluteFile, linkColor: '#456789'
            div { padding 2.px margin 1.px }
        }
        'table{padding: 5px;}a{color: #456789;}div{padding: 2px;margin: 1px;}' == "$css".trim()
    }

    @Unroll
    def "import should allow #type parameter"() {
        given:
        otherCss.text = "a {color linkColor}"
        expect:
        def css = GrooCSS.process {
            importFile otherCss.absoluteFile, linkColor: value, param2: 123
        }
        'a{color: #123456;}' == "$css".trim()
        where:
        type        | value
        'String'    | '#123456'
        'Integer'   | 0x123456
        'Color'     | new Color(0x123456)
    }
}
