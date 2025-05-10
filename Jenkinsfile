pipeline {
    agent any
    environment {
        DOCKER_IMAGE_NAME = 'brightex99/mywedapp'
        TAG = "${BUILD_NUMBER}"
        DOCKERFILE_PATH = './Dockerfile'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_CREDENTIALS_ID = 'docker_credentials_id'
    }

    stages {
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
                    sh "snyk test --docker ${DOCKER_IMAGE_NAME}:${TAG} || true"
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
                        sh '''
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker push "$IMAGE_TAG"
                        '''
                    }
                }
            }
        }
    }
}
