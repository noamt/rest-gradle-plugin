package org._10ne.gradle.rest

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author Noam Y. Tenne
 */
class RestPluginSpec extends Specification {

    def 'Apply the plugin to a project and check the default values of the configuration'() {
        setup:
        def project = ProjectBuilder.builder().build()

        expect:
        project.tasks.findByName('rest') == null

        when:
        project.apply plugin: RestPlugin

        then:
        def restTask = project.tasks.findByName('rest')
        restTask != null
        restTask.httpMethod == 'get'
    }
}
