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

import org.groocss.proc.Processor

/** Extension to Gradle for configuring GrooCSS. */
class GroocssExtension {

    boolean addWebkit = true,
            addMs = true,
            addOpera = true,
            addMoz = true,
            prettyPrint = false,
            compress = false

    String charset = null

    /** Element-names that you only want to use as CSS classes. */
    Set styleClasses = []

    /** Whether or not convert under-scores in CSS classes into dashes (main_content becomes main-content).
     * Default is false. */
    boolean convertUnderline = false

    /** Custom processors/validators to use.
     * @see org.groocss.proc.Processor
     * @see org.groocss.valid.DefaultValidator
     */
    Collection<Processor> processors = []

    /** Variables to make available in the processed GrooCSS files.*/
    Map<String, Object> variables = [:]
}
