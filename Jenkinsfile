#!/usr/bin/env groovy
@Library('jeap-pipelinelibrary@feature/pipeline-with-pact') _

pipeline {

    // Run everything on an agent with the docker daemon
    agent {
        node {
            label 'docker'
        }
    }


    stages {

        stage('Prepare build environment') {
            steps {
                script {
                    // Create settings.xml file for the Maven build pipeline
                    withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: 'nexusCredentials',
                                      usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                        def settingsXml = libraryResource('ch/admin/bit/maven/settings.xml')
                                .replaceAll("@USERNAME@", "${USERNAME}")
                                .replaceAll("@PASSWORD@", "${PASSWORD}")
                        writeFile file: 'settings.xml', text: settingsXml
                    }
                }
            }
        }

        stage('Build & Unit Tests') {
            steps {
                script {
                    docker.withRegistry('https://repo.bit.admin.ch:8444', 'nexusCredentials') {
                        def jdk = docker.image('bit/openjdk:8-jdk')
                        jdk.pull()
                        jdk.inside() {
                            sh './mvnw clean install'
                            stash 'build-workspace-documentservice'
                        }
                    }
                }
            }
        }

        stage('Deploy DEV') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    docker.withRegistry('https://repo.bit.admin.ch:8444', 'nexusCredentials') {
                        def cfApi = docker.image('bit/cf-cli-autopilot:latest')
                        cfApi.pull()
                        cfApi.inside('-u root') {
                            withCredentials([usernamePassword(credentialsId: 'stackatoCredentials', passwordVariable: 'STACKATO_PASSWORD', usernameVariable: 'STACKATO_USERNAME')]) {
                                sh "cf login -a '${STACKATO_API_URL}' -u '${STACKATO_USERNAME}' -p '${STACKATO_PASSWORD}' -o 'TEST' -s 'EBD' --skip-ssl-validation"
                            }
                            unstash 'build-workspace-documentservice'
                            sh "cf zero-downtime-push cassandra-performancetester -f 'manifest.yml'"
                        }
                    }
                }
            }
        }
    }
}