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

import groovy.transform.TypeChecked
import org.gradle.api.InvalidUserDataException
import org.gradle.api.file.CopySpec
import org.gradle.api.internal.file.CopyActionProcessingStreamAction
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.WorkResults
import org.gradle.internal.file.PathToFileResolver

/** Converts GrooCSS files using the same features as the Copy task. */
@TypeChecked
class GroocssTask extends Copy {

    Config conf
    String charset = 'UTF-8'
    File propertiesFile

    @Override
    protected CopyAction createCopyAction() {
        return new CopyAction() {
            @Override
            WorkResult execute(CopyActionProcessingStream stream) {
                if (!conf) {
                    if (propertiesFile) conf = new Config(propertiesFile)
                    else conf = new Config()
                }
                if(destinationDir == null) {
                    throw new InvalidUserDataException("No copy destination directory has been specified, use 'into' to specify a target directory.");
                }
                def action = new GroocssFileAction(fileLookup.getFileResolver(destinationDir), conf, rootSpec)
                action.charset = charset
                stream.process(action)
                return WorkResults.didWork(action.didWork)
            }
        }
    }

    /** Converts the given GrooCSS file with current config, and outputs to given file. */
    static class GroocssFileAction implements CopyActionProcessingStreamAction {

        private final PathToFileResolver fileResolver
        Config config
        String charset = 'UTF-8'
        CopySpec copySpec
        boolean didWork

        GroocssFileAction(PathToFileResolver fileResolver, Config config, CopySpec copySpec) {
            this.fileResolver = fileResolver
            this.config = config
            this.copySpec = copySpec
        }

        @Override
        void processFile(FileCopyDetailsInternal details) {
            def path = details.relativePath.pathString
            File target = fileResolver.resolve(path)
            println "relativePath=$path  target=$target.absolutePath"

            target.parentFile.mkdirs()
            boolean copied = details.copyTo(target)

            if (copied) {
                File newTarget = new File(target.parentFile, GroocssPlugin.toCssName(target.name))

                GrooCSS.convert config, target, newTarget, charset, true
                target.delete()
                didWork = true
            }
        }
    }
}
