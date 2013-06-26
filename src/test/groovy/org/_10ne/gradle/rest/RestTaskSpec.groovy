package org._10ne.gradle.rest

import groovyx.net.http.AuthConfig
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.http.HttpHeaders
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author Noam Y. Tenne
 */
class RestTaskSpec extends Specification {

    Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def 'Execute a request with no URI'() {
        setup:
        Task task = project.tasks.create(name: 'noUri', type: RestTask) {}

        when:
        task.executeRequest()

        then:
        thrown(InvalidUserDataException)
    }

    def 'Configure and execute a request'() {
        setup:
        Task task = project.tasks.create(name: 'request', type: RestTask) {
            httpMethod = 'post'
            uri = 'bob.com'
            username = 'username'
            password = 'password'
            requestContentType = 'requestContentType'
            requestBody = 'requestBody'
            contentType = 'contentType'
        }
        def mockClient = Mock(RESTClient)
        task.client = mockClient

        def mockAuth = Mock(AuthConfig)

        def mockResponse = Mock(HttpResponseDecorator)

        when:
        task.executeRequest()

        then:
        1 * mockClient.setUri('bob.com')
        1 * mockClient.getAuth() >> { mockAuth }
        1 * mockAuth.basic('username', 'password')
        1 * mockClient.post(_ as Map) >> { Map params ->
            assert params.body == 'requestBody'
            assert params.contentType == 'contentType'
            assert params.requestContentType == 'requestContentType'
            mockResponse
        }
        1 * mockResponse.getData() >> { 'somedata' }
    }

    def 'Configure and execute a preemptive authentication request'() {
        setup:
        Task task = project.tasks.create(name: 'request', type: RestTask) {
            httpMethod = 'post'
            uri = 'bob.com'
            preemptiveAuth = true
            username = 'username'
            password = 'password'
            requestContentType = 'requestContentType'
            requestBody = 'requestBody'
            contentType = 'contentType'
        }
        def mockClient = Mock(RESTClient)
        task.client = mockClient

        def mockAuth = Mock(AuthConfig)

        def mockResponse = Mock(HttpResponseDecorator)

        def headers = [:]

        when:
        task.executeRequest()

        and:
        headers[HttpHeaders.AUTHORIZATION] == 'Basic dXNlcm5hbWU6cGFzc3dvcmQ='

        then:
        1 * mockClient.setUri('bob.com')
        1 * mockClient.getHeaders() >> { headers }
        1 * mockClient.getAuth() >> { mockAuth }
        1 * mockAuth.basic('username', 'password')
        1 * mockClient.post(_ as Map) >> { Map params ->
            assert params.body == 'requestBody'
            assert params.contentType == 'contentType'
            assert params.requestContentType == 'requestContentType'
            mockResponse
        }
        1 * mockResponse.getData() >> { 'somedata' }
    }
}
