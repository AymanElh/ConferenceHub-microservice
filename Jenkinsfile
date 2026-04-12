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
                        mvn sonar:sonar \
                             --batch-mode \
                             -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                             -Dsonar.projectName="ConferenceHub" \
                             -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml \
                             -Dsonar.exclusions=**/generated/**,**/target/** \
                             -Dspring.cloud.config.enabled=false
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            when {
                anyOf {
                    branch 'main'
                    branch 'dev'
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

                    services.each { svc -> 
                        sh """
                            docker build \
                                -f ${svc.file} \
                                -t ${DOCKER_REGISTRY}/conferencehub-${svc.name}:${BUILD_NUMBER} \
                                -t ${DOCKER_REGISTRY}/conferencehub-${svc.name}:latest \
                                .
                        """
                    }
                }
            }
        }

        stage('Docker Push') {
            when {
                branch 'main'
            }

            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-hub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        for svc in config discovery gateway keynote conference notification; do
                            docker push ${DOCKER_REGISTRY}/conferencehub-${svc}:${BUILD_NUMBER}
                            docker push ${DOCKER_REGISTRY}/conferencehub-${svc}:latest
                        done
                    '''
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