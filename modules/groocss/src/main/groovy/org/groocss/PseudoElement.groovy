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

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

/**
 * Represents a CSS3 Pseudo-element such as "::before", "::after", "::first-letter", "::first-line",
 * and "::placeholder". "::placeholder" is NOT supported by IE as of July 11 2019.
 *
 * @see GrooCSS
 * @since 1.0-M4
 */
@InheritConstructors
@CompileStatic
class PseudoElement extends PseudoClass {

    /** Only here to restrict the DSL so that pseudo-element is used properly. */
    @InheritConstructors
    static class StyleGroup extends org.groocss.StyleGroup {}

    /** Allows this to be chainable to pseudo-classes. */
    PseudoElement mod(PseudoClass other) {
        new PseudoElement(this.value + "$other")
    }

    /** Allows this to be chainable to pseudo-classes. */
    PseudoElement.StyleGroup mod(PseudoClass.StyleGroup other) {
        new PseudoElement.StyleGroup(this.value + "$other", other.config, other.owner)
    }

    @Override
    String toString() {
        return "::$value"
    }
}
