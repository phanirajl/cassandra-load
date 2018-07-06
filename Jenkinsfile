#!/usr/bin/env groovy
@Library('jeap-pipelinelibrary@feature/deploy-cf') _
mavenCFPipelineTemplate {
    dockerContainerUser = 'root'
    buildContainer = 'bit/openjdk:8-jdk'
    skipTests = true
    skipCodeInspection = true
    org = 'TEST'
    space = 'EBD'
    appname = 'cassandra-performance'
    manifest = 'manifest.yml'
    sslValidation = false
}