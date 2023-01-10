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
package org.groocss.valid

import groovy.transform.CompileStatic
import org.groocss.CSSPart
import org.groocss.proc.Processor

/**
 * Convenient abstract class for extending and making Validators but not necessary to extend.
 * Just checks that phase is VALIDATE Phase and if so calls validate method.
 * @see Processor
 * @see DefaultValidator
 * @since 1.0-M2
 */
@CompileStatic
abstract class AbstractValidator<T extends CSSPart> implements Processor<T> {

    /** Implements the interface's process method. */
    @Override
    Optional<String> process(T cssPart, Phase phase) {
        if (phase == Phase.VALIDATE) return validate(cssPart)
        else return Optional.empty()
    }

    /** Returns empty if no problem, otherwise returns Optional wrapped error message.*/
    abstract Optional<String> validate(T style)

}
