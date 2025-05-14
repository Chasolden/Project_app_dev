
pipeline {
    agent any
    environment {
        DOCKER_IMAGE_NAME = 'brightex99/mywedapp'
        DOCKERFILE_PATH = './Dockerfile'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_CREDENTIALS_ID = 'docker_credentials_id'
        SSH_CREDENTIALS_ID = 'bright-ssh-creds-id'
    }

    stages {
        stage('Set Git Tag') {
            steps {
                script {
                    // Truncate the Git commit to 8 characters and assign it to TAG
                    env.TAG = GIT_COMMIT.take(8)
                    echo "Using image tag: ${env.TAG}"
                }
            }
        }
        
        stage('Building Stage') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_IMAGE_NAME}:${TAG}")
                }
            }
        }

        stage('Scanning Image with Snyk') {
            steps {
                withCredentials([string(credentialsId: 'snyk-api-token', variable: 'SNYK_TOKEN')]) {
                    sh 'snyk auth $SNYK_TOKEN || true'
                    sh "snyk test --docker ${DOCKER_IMAGE_NAME}:${TAG} --file=Dockerfile || true"
                }
            }
        }

        stage('Pushing Image to DockerHub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    withEnv(["IMAGE_TAG=${DOCKER_IMAGE_NAME}:${TAG}"]) {
                        script {
                            sh '''
                                echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                                docker push "$IMAGE_TAG"
                            '''
                            dockerImage.push("latest")
                        }
                    }
                }
            }
        }

        stage('Deploy to Remote VM') {
            environment {
                VM_USER = 'bright'
                VM_HOST = '192.168.168.135'
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
