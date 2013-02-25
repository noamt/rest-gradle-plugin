package org._10ne.gradle.rest

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Noam Y. Tenne
 */
class RestPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task("rest", type: RestTask)
    }
}
