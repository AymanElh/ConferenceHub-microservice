// Jenkinsfile — Declarative Pipeline
pipeline {
    agent any

    environment {
        // Docker Hub or your registry credentials (configured in Jenkins UI)
        DOCKER_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_REGISTRY     = 'conferencehub'
        MAVEN_OPTS          = '-Xmx1024m'
    }

    tools {
        maven 'Maven-3.9' 
        jdk   'JDK-17'
    }

    stages {

        stage('Checkout') {
            // Jenkins pulls your code from GitHub
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME}"
            }
        }

        stage('Set Image Tag') {
            steps {
                script {
                    // Logic: main -> latest, dev -> dev, other -> branch-name
                    if (env.BRANCH_NAME == 'main') {
                        env.IMAGE_TAG = 'latest'
                    } else if (env.BRANCH_NAME == 'dev') {
                        env.IMAGE_TAG = 'dev'
                    } else {
                        env.IMAGE_TAG = env.BRANCH_NAME.replaceAll(/[^a-zA-Z0-9]/, "-")
                    }
                    echo "Calculated IMAGE_TAG: ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Build & Test') {
            // Compile all modules, skip integration tests for speed
            steps {
                sh 'mvn clean verify -DskipTests=false -Dspring.cloud.config.enabled=false --batch-mode'
            }
            post {
                always {
                    // Publish JUnit test results in Jenkins UI
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            // Build Docker images for each service
            when {
                anyOf {
                    branch 'main'
                    branch 'dev'
                }
            }
            parallel {
                stage('config-service') {
                    steps {
                        sh """
                            docker build \
                              -f config-service/Dockerfile \
                              -t ${DOCKER_REGISTRY}/conferencehub-config:${IMAGE_TAG} \
                              -t ${DOCKER_REGISTRY}/conferencehub-config:${BUILD_NUMBER} \
                              .
                        """
                    }
                }
                stage('discovery-service') {
                    steps {
                        sh """
                            docker build \
                              -f discovery-service/Dockerfile \
                              -t ${DOCKER_REGISTRY}/conferencehub-discovery:${IMAGE_TAG} \
                              -t ${DOCKER_REGISTRY}/conferencehub-discovery:${BUILD_NUMBER} \
                              .
                        """
                    }
                }
                stage('gateway-service') {
                    steps {
                        sh """
                            docker build \
                              -f gateway-service/Dockerfile \
                              -t ${DOCKER_REGISTRY}/conferencehub-gateway:${IMAGE_TAG} \
                              -t ${DOCKER_REGISTRY}/conferencehub-gateway:${BUILD_NUMBER} \
                              .
                        """
                    }
                }
                stage('keynote-service') {
                    steps {
                        sh """
                            docker build \
                              -f Keynote-service/Dockerfile \
                              -t ${DOCKER_REGISTRY}/conferencehub-keynote:${IMAGE_TAG} \
                              -t ${DOCKER_REGISTRY}/conferencehub-keynote:${BUILD_NUMBER} \
                              .
                        """
                    }
                }
                stage('conference-service') {
                    steps {
                        sh """
                            docker build \
                              -f conference-service/Dockerfile \
                              -t ${DOCKER_REGISTRY}/conferencehub-conference:${IMAGE_TAG} \
                              -t ${DOCKER_REGISTRY}/conferencehub-conference:${BUILD_NUMBER} \
                              .
                        """
                    }
                }
                stage('notification-service') {
                    steps {
                        sh """
                            docker build \
                              -f notification-service/Dockerfile \
                              -t ${DOCKER_REGISTRY}/conferencehub-notification:${IMAGE_TAG} \
                              -t ${DOCKER_REGISTRY}/conferencehub-notification:${BUILD_NUMBER} \
                              .
                        """
                    }
                }
            }
        }

        stage('Docker Push') {
            // Push images to Docker Hub
            when {
                anyOf {
                    branch 'dev'
                    branch 'main'
                }
            }
            steps {
                withDockerRegistry(credentialsId: 'docker-hub-credentials', url: '') {
                    sh """
                        docker push ${DOCKER_REGISTRY}/conferencehub-config:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/conferencehub-config:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/conferencehub-discovery:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/conferencehub-discovery:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/conferencehub-gateway:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/conferencehub-gateway:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/conferencehub-keynote:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/conferencehub-keynote:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/conferencehub-conference:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/conferencehub-conference:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/conferencehub-notification:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/conferencehub-notification:${BUILD_NUMBER}
                    """
                }
            }
        }

        stage('Cleanup') {
            // Remove dangling images to save disk space
            steps {
                sh 'docker image prune -f'
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS — Build #${BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline FAILED — Check logs"
            // Add email/Slack notification here later
        }
        always {
            cleanWs()   // Clean workspace after each build
        }
    }
}