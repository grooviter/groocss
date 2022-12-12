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
 * Created by adavis on 10/25/16.
 */
class ColorSpec extends Specification {

    def "saturate should increase color saturation"() {
        expect:
        new Color(100i, 0.2d, 1.0d).saturate(0.5) == new Color(100i, 0.7d, 1.0d)
    }

    def "desaturate should decrease color desaturation"() {
        expect:
        new Color(100i, 0.8d, 1.0d).desaturate(0.5) == new Color(100i, 0.3d, 1.0d)
    }

    def "fadein should increase color alpha"() {
        given:
        def c = new Color(100i, 0.2d, 1.0d).alpha(0)
        expect:
        c.alpha(c.alpha + 0.5f) == new Color(100i, 0.2d, 1.0d).alpha(0.5d)
    }

    def "fadeout should decrease color alpha"() {
        given:
        def c = new Color(100i, 0.2d, 1.0d).alpha(1.0d)
        expect:
        c.alpha(c.alpha - 0.5f) == new Color(100i, 0.2d, 1.0d).alpha(0.5d)
    }

    def "hue should work"() {
        expect:
        new Color(100i, 0.2d, 1.0d).hue == 100i
    }
    def "saturation should work"() {
        expect:
        new Color(100i, 0.2d, 1.0d).saturation == 0.2f
    }
    def "brightness should work"() {
        expect:
        new Color(100i, 0.2d, 1.0d).brightness == 1.0f
    }

    def "mix should work"() {
        expect:
        new Color(25, 0, 50).mix(new Color(75, 100, 50)) == new Color(50, 50, 50)
    }

    def "shade should work"() {
        expect:
        new GrooCSS().shade(new Color(80, 100, 50)) == new Color(40, 50, 25)
    }

    def "tint should work"() {
        expect:
        new GrooCSS().tint(new Color(80, 100, 50)) == new Color(167, 177, 152)
    }
}
