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
        K8S_NAMESPACE = 'default'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

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
                    sh 'snyk auth $SNYK_TOKEN'
                    sh "snyk test --docker ${DOCKER_IMAGE_NAME}:${TAG} --file=${DOCKERFILE_PATH}"
                }
            }
        }

        // Deploying to Remote VM
        stage('Deploy to Remote VM') {
            steps {
                script {
                    // Load env vars from file
                    def envMap = readProperties file: '/etc/jenkins-envs/deploy_vm.env'
                    def user = envMap.VM_USER
                    def host = envMap.VM_HOST
                    def dir  = envMap.VM_DIR

                    sshagent(credentials: ["${SSH_CREDENTIALS_ID}"]) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ${user}@${host} '
                                cd ${dir} &&
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

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    // Apply deployment, service, and HPA to Kubernetes
                    sh 'kubectl apply -f k8s/mywed-deployment.yaml'
                    sh 'kubectl apply -f k8s/mywed-service.yaml'
                    sh 'kubectl apply -f k8s/horinzontalpa.yaml'
                    sh 'kubectl rollout status deployment/mywedapp'
                    sh 'kubectl get pods'
                }
            }
        }

        stage('Rollback to Previous Version') {
            when {
                expression { return currentBuild.result == 'FAILURE' }
            }
            steps {
                script {
                    // Rollback to the previous deployment in case of failure
                    sh 'kubectl rollout undo deployment/mywedapp'
                }
            }
        }
    }
}
