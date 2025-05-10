pipeline {
    agent any
    environment {
        DOCKER_IMAGE_NAME = 'brightex99/mywedapp'
        TAG = "${BUILD_NUMBER}"
        DOCKERFILE_PATH = './Dockerfile'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_CREDENTIALS_ID = 'docker_credentials_id'
        SNYK_TOKEN = credentials('snyk-api-token')
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
                withEnv(["SNYK_TOKEN=${SNYK_TOKEN}"]) {
                    sh 'snyk auth $SNYK_TOKEN'
                    sh "snyk test --docker ${dockerImage.imageName} || true"
                }
            }
        }
    }
}
