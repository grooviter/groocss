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
import org.groocss.MediaCSS
import org.groocss.StyleGroup

/**
 * Finds any StyleGroup with placeholder in it and adds a copy of it with -webkit-input- prefix added to placeholder.
 */
@CompileStatic
class PlaceholderProcessor implements Processor<MediaCSS> {

    static final String webkitInputPrefix = '-webkit-input-'
    static final String placeholder = 'placeholder'

    @Override
    Optional<String> process(MediaCSS cssPart, Processor.Phase phase) {

        if (phase == Processor.Phase.POST_VALIDATE) {
            cssPart.groups.findAll {
                it instanceof StyleGroup && ((StyleGroup) it).selector.contains('placeholder')
            }.each {
                println "WARNING: $placeholder not supported by IE"
                def sg = (StyleGroup) it
                def selector = sg.selector
                def newSelector = selector.replace(placeholder, webkitInputPrefix + placeholder)

                def newSg = new StyleGroup(newSelector, sg.config, sg.owner)

                newSg.styleList.addAll sg.styleList

                cssPart.groups << newSg
            }
        }
        return Optional.empty()
    }
}
