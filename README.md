REST Gradle Plugin
==================

A Gradle plugin that provides a task infrastructure to perform REST requests.

Installation
------------

TBD

Usage
-----

The plugin adds a new task named `rest`. This task exposes the following properties:

 * httpMethod - The type of HTTP method to execute. Type: String. Default: `get`. Possible values: `delete`, `get`, `head`, `options`, `post`, `put`.
 * uri - The target URI of the request. An invocation of toString() on the value should result in a valid URI. Type: Object.
 * username - Authentication username. Type: String.
 * password - Authentication password. Type: String.
 * contentType - The expected content type of both request and response. Type: groovyx.net.http.ContentType / String.
 * requestContentType - The expected content type of the request. Overrides the `contentType` parameter. Type: groovyx.net.http.ContentType / String.
 * requestBody - The request content. Type: Object.