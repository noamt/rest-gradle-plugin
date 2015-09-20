/*
 * Copyright 2013 Noam Y. Tenne
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

package org._10ne.gradle.rest

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import org.apache.commons.lang.StringUtils
import org.apache.http.HttpHeaders
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Noam Y. Tenne
 */
class RestTask extends DefaultTask {

    private static Logger slf4jLogger = LoggerFactory.getLogger(RestTask)

    RESTClient client

    @Input
    String httpMethod

    @Input
    Object uri

    @Input
    @Optional
    boolean preemptiveAuth

    @Input
    @Optional
    String username

    @Input
    @Optional
    String password

    @Input
    @Optional
    Object requestContentType

    @Input
    @Optional
    Object contentType

    @Input
    @Optional
    Object requestBody

    @Input
    @Optional
    Object requestHeaders

    @Input
    @Optional
    Closure responseHandler

    HttpResponseDecorator serverResponse

    RestTask() {
        httpMethod = 'get'
        client = new RESTClient()
    }

    void configureProxy(String protocol) {
        String proxyHost = System.getProperty("${protocol}.proxyHost", '')
        int proxyPort = System.getProperty("${protocol}.proxyPort", '0') as int
        if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
            slf4jLogger.info "Using ${protocol.toUpperCase()} proxy: $proxyHost:$proxyPort"
            client.setProxy(proxyHost, proxyPort, protocol)
        }
    }

    @TaskAction
    void executeRequest() {
        if (!uri || StringUtils.isBlank(uri)) {
            throw new InvalidUserDataException('No resource URI provided')
        }

        client.uri = uri
        if (StringUtils.isNotBlank(username)) {
            if (preemptiveAuth) {
                client.headers[HttpHeaders.AUTHORIZATION] = 'Basic ' + ("$username:$password".toString().bytes.encodeBase64())
            }
            client.auth.basic(username, password)
        }

        configureProxy('http')
        configureProxy('https')

        if (requestHeaders instanceof Map) {
            client.headers.putAll(requestHeaders);
        }

        def params = [:]
        if (requestBody) {
            params.body = requestBody
        }
        if (contentType) {
            params.contentType = contentType
        }
        if (requestContentType) {
            params.requestContentType = requestContentType
        }

        slf4jLogger.info "Executing a '$httpMethod' request to '$uri'"

        try {
            serverResponse = client."${httpMethod.toLowerCase()}"(params)
            if (noResponseHandler()) {
                slf4jLogger.info "Server Response:" + System.lineSeparator() + serverResponse.getData()
            } else {
                callResponseHandler()
            }
        } catch (groovyx.net.http.HttpResponseException e) {
            throw new GradleException(e.getResponse().getData().toString(), e)
        }
    }

    private boolean noResponseHandler() {
        !responseHandler || responseHandler.maximumNumberOfParameters != 1
    }

    void callResponseHandler() {
        def parameterType = responseHandler.parameterTypes.first()
        if (InputStream.isAssignableFrom(parameterType)) {
            responseHandler.call(serverResponse.entity.content)
        } else if (String.isAssignableFrom(parameterType)) {
            serverResponse.entity.content.withStream {
                responseHandler.call(it.text)
            }
        } else {
            responseHandler.call(serverResponse.data)
        }
    }
}
