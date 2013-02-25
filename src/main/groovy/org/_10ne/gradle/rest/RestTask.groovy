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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

/**
 * @author Noam Y. Tenne
 */
class RestTask extends DefaultTask {

    @Input String httpMethod
    @Input Object uri
    @Input
    @Optional String username
    @Input
    @Optional String password
    @Input
    @Optional Object requestContentType
    @Input
    @Optional Object contentType
    @Input
    @Optional Object requestBody

    RestTask() {
        httpMethod = 'get'
    }

    @TaskAction
    void executeRequest() {
        if (!uri || StringUtils.isBlank(uri.toString())) {
            throw new TaskExecutionException(this, new IllegalArgumentException('No resource URI provided'))
        }

        def client = new RESTClient(uri)
        if (StringUtils.isNotBlank(username)) {
            client.auth.basic(username, password)
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

        println "Executing a '$httpMethod' request to '$uri'"

        HttpResponseDecorator responseDecorator = client."${httpMethod.toLowerCase()}"(params)

        println "Received response: ${responseDecorator.getData()}"
    }
}