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
package org.groocss.proc

import groovy.transform.CompileStatic
import org.groocss.CSSPart

/**
 * Interface for defining a custom Processor that can modify or validate GrooCSS input.
 * <p>
 * By extending this interface you can write your own custom validators and add them via {@link org.groocss.Config}.
 * <p>
 * You could also write your own Processor that modifies values in the PRE_VALIDATE phase or any other Phase.
 * For example:<pre><code>
 * class ConvertAllIntsToPixels implements Processor< Style > {
 *     Optional<String> process(Style style, Phase phase) {
 *         if (phase == Phase.PRE_VALIDATE && style.value instanceof Integer) {
 *             style.value = new Measurement(style.value, 'px')
 *         }
 *         return Optional.empty();
 *     }
 * }     </code></pre>
 * @see org.groocss.Config
 * @since 1.0-M2
 * */
@CompileStatic
interface Processor<T extends CSSPart> {

    /** Enum of phases for which this Processor should be called. */
    enum Phase { PRE_VALIDATE, VALIDATE, POST_VALIDATE }

    /**
     * Returns empty if valid, otherwise returns an optional containing an error string.
     *
     * @param
     */
    Optional<String> process(T cssPart, Phase phase)

}
