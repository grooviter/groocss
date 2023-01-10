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

class PerformanceSpec extends Specification {

    def "test converting a file 100 times" () {
        when:
        long start = System.currentTimeMillis()
        def output = new File('build/test3.css')
        def input = new File('src/test/groovy/test3.css.groovy')
        println input.absolutePath
        100.times { GrooCSS.convertFile(new Config().noExts().compress(), input, output) }
        def css = output.text
        def time = System.currentTimeMillis() - start
        println "took $time ms"
        then:
        css.toString() == 'body{font-size: 2em;color: Black;}article{padding: 2em;}#thing{font-size: 200%;}' +
                '@keyframes test {from{color: Black;}to{color: Red;}}'
    }

}
