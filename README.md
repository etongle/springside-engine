# Springside Engine

An embeded backend engine base on Spring Boot. It support Netty + Thrift, and also Jetty or Tomcat + Restful.

It will integrate with other projects to provide:

- dynamic configuration
- service registration, loadbalance and routing rule
- retry and short circuit
- metrics monitor
- distributed trace logs

Rather than the java smart client, it will also provide a proxy to support other languages.