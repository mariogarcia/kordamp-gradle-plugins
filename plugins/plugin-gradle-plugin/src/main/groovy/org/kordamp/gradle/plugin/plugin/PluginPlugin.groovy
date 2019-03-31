/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2018-2019 Andres Almiray.
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
package org.kordamp.gradle.plugin.plugin

import com.gradle.publish.PluginBundleExtension
import com.gradle.publish.PluginConfig
import com.gradle.publish.PublishPlugin
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin
import org.kordamp.gradle.plugin.AbstractKordampPlugin
import org.kordamp.gradle.plugin.base.ProjectConfigurationExtension

import static org.kordamp.gradle.PluginUtils.resolveConfig
import static org.kordamp.gradle.PluginUtils.resolveEffectiveConfig
import static org.kordamp.gradle.StringUtils.isBlank
import static org.kordamp.gradle.plugin.base.BasePlugin.isRootProject

/**
 *
 * @author Andres Almiray
 * @since 0.16.0
 */
@CompileStatic
class PluginPlugin extends AbstractKordampPlugin {
    Project project

    void apply(Project project) {
        this.project = project

        if (isRootProject(project)) {
            project.childProjects.values().each {
                configureProject(it)
            }
        }
        configureProject(project)
    }

    private void configureProject(Project project) {
        if (hasBeenVisited(project)) {
            return
        }
        setVisited(project, true)

        project.plugins.apply(JavaGradlePluginPlugin)
        project.plugins.apply(PublishPlugin)

        String pluginName = resolveConfig(project).plugin.pluginName

        PluginBundleExtension pbe = project.extensions.findByType(PluginBundleExtension)
        PluginConfig pc = pbe.plugins.maybeCreate(pluginName)

        project.afterEvaluate {
            ProjectConfigurationExtension effectiveConfig = resolveEffectiveConfig(project)

            if (isBlank(pbe.website)) {
                pbe.website = effectiveConfig.info.url
            }
            if (isBlank(pbe.vcsUrl)) {
                pbe.vcsUrl = effectiveConfig.info.resolveScmLink()
            }
            if (isBlank(pbe.description)) {
                pbe.description = effectiveConfig.info.description
            }
            if (!pbe.tags) {
                pbe.tags = effectiveConfig.plugin.resolveTags(effectiveConfig)
            }

            if (isBlank(pbe.mavenCoordinates.groupId)) {
                pbe.mavenCoordinates.groupId = project.group
            }
            if (isBlank(pbe.mavenCoordinates.artifactId)) {
                pbe.mavenCoordinates.artifactId = project.name
            }

            if (isBlank(pbe.mavenCoordinates.version)) {
                pbe.mavenCoordinates.version = project.version
            }

            if (isBlank(pc.id)) {
                pc.id = effectiveConfig.plugin.id
            }
            if (isBlank(pc.displayName)) {
                pc.displayName = effectiveConfig.info.name
            }
        }
    }
}