@Library('my-shared-library') _  // Import the shared library

pipeline {
    agent any
    environment {
        DOCKER_IMAGE_NAME = 'brightex99/mywedapp'
        TAG = "${BUILD_NUMBER}"
        DOCKERFILE_PATH = './Dockerfile'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_CREDENTIALS_ID = 'docker_credentials_id'
        SSH_CREDENTIALS_ID = 'bright-ssh-creds-id'
    }

    stages {
        stage('Build and Push Docker Image') {
            steps {
                script {
                    // Use shared library function to build and push the Docker image
                    buildAndPushImage(dockerImage: DOCKER_IMAGE_NAME, tag: TAG, registry: DOCKER_REGISTRY, credentialsId: DOCKER_CREDENTIALS_ID)
                }
            }
        }

        stage('Verify Docker Image') {
            steps {
                // Verify that the image is built successfully and exists
                sh 'docker images'
            }
        }

        stage('Scanning Image with Snyk') {
            steps {
                withCredentials([string(credentialsId: 'snyk-api-token', variable: 'SNYK_TOKEN')]) {
                    sh 'snyk auth $SNYK_TOKEN || true'
                    sh "snyk test --docker ${DOCKER_IMAGE_NAME}:${TAG}"
                }
            }
        }

        stage('Deploy to Remote VM') {
            environment {
                VM_USER = 'bright'
                VM_HOST = '192.168.168.129'
                VM_DIR  = '/home/bright/mywedapp/mywedapp'
            }
            steps {
                sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
                    sh """
                        ssh -o StrictHostKeyChecking=no \$VM_USER@\$VM_HOST '
                            cd /home/bright/mywedapp &&
                            ls -l &&
                            docker-compose pull &&
                            docker-compose down &&
                            docker-compose up -d
                        '
                    """
                }
            }
        }
    }
}
