pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'aymanelh'
        SONAR_PROJECT_KEY = 'conferencehub'
    }

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    stages {
        stage('checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME} | PR: ${env.CHANGE_ID ?: 'none'}"
            }
        }

        stage('Build & Test') {
            steps {
                sh '''
                    mvn clean verify \
                        --batch-mode \
                        -Dspring.cloud.config.enabled=false \
                        -Dspring.profiles.active=test
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        set -eu
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                            --batch-mode \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.projectName="ConferenceHub" \
                            -Dsonar.host.url="${SONAR_HOST_URL:-http://localhost:9000}" \
                            -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml \
                            -Dsonar.exclusions='**/target/**,**/.github/**,docs/**,monitoring/**,keycloak/**,**/Dockerfile,**/*.png,**/mvnw,**/mvnw.cmd' \
                            -Dsonar.cpd.exclusions='**/Dockerfile,**/.github/**,docs/**,monitoring/**,keycloak/**' \
                            -Dsonar.coverage.exclusions='**/*Application.java,**/config/**,**/dto/**,**/entity/**,**/model/**,**/mapper/**,**/mappers/**,**/*MapperImpl.java,**/exception/**,**/kafka/event/**,**/*Event.java' \
                            -Dsonar.maven.scanAll=true \
                            -Dspring.cloud.config.enabled=false
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'dev'
                    branch 'staging'
                }
            }
            steps {
                script {
                    def services = [
                        [name: 'config',       file: 'config-service/Dockerfile'],
                        [name: 'discovery',    file: 'discovery-service/Dockerfile'],
                        [name: 'gateway',      file: 'gateway-service/Dockerfile'],
                        [name: 'keynote',      file: 'Keynote-service/Dockerfile'],
                        [name: 'conference',   file: 'conference-service/Dockerfile'],
                        [name: 'notification', file: 'notification-service/Dockerfile']
                    ]

                    // Get git tag if exists (e.g. v1.0.0), else use branch-build
                    def gitTag = sh(
                        script: "git tag --points-at HEAD | head -1",
                        returnStdout: true
                    ).trim()

                    def imageVersion = gitTag ?: "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
                    echo "Image version: ${imageVersion}"

                    withCredentials([usernamePassword(
                        credentialsId: 'docker-hub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {
                        sh '''
                            set +x
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        '''

                        services.each { svc ->
                            def imageName = "${DOCKER_REGISTRY}/conferencehub-${svc.name}"

                            // Always build with build number + branch-latest
                            def buildArgs = [
                                "-f ${svc.file}",
                                "--build-arg BUILD_VERSION=${imageVersion}",
                                "-t ${imageName}:${imageVersion}",
                                "-t ${imageName}:${env.BRANCH_NAME}-latest"
                            ]

                            // main branch also gets :latest tag
                            if (env.BRANCH_NAME == 'main') {
                                buildArgs << "-t ${imageName}:latest"
                            }

                            sh "docker build ${buildArgs.join(' ')} ."

                            // Push all tags
                            sh """
                                docker push ${imageName}:${imageVersion} || {
                                    echo 'Docker push failed. Check Jenkins credential docker-hub-credentials: the Docker Hub access token must have Read/Write permissions for ${DOCKER_REGISTRY}/conferencehub-${svc.name}.'
                                    exit 1
                                }
                            """
                            sh """
                                docker push ${imageName}:${env.BRANCH_NAME}-latest || {
                                    echo 'Docker push failed. Check Jenkins credential docker-hub-credentials: the Docker Hub access token must have Read/Write permissions for ${DOCKER_REGISTRY}/conferencehub-${svc.name}.'
                                    exit 1
                                }
                            """

                            if (env.BRANCH_NAME == 'main') {
                                sh """
                                    docker push ${imageName}:latest || {
                                        echo 'Docker push failed. Check Jenkins credential docker-hub-credentials: the Docker Hub access token must have Read/Write permissions for ${DOCKER_REGISTRY}/conferencehub-${svc.name}.'
                                        exit 1
                                    }
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('Cleanup') {
            steps {
                sh 'docker image prune -f'
            }
        }
    }

    post {
        success {
            echo "SUCCESS — Branch: ${env.BRANCH_NAME} Build: #${BUILD_NUMBER}"
        }
        failure {
            echo "FAILED — Branch: ${env.BRANCH_NAME} Build: #${BUILD_NUMBER}"
        }
        always {
            cleanWs()
        }
    }
}
