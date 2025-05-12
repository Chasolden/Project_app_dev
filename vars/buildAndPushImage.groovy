def call(Map config = [:]) {
    // Default values if not provided in config
    def dockerImage = config.dockerImage ?: 'default/image'
    def tag = config.tag ?: 'latest'
    def registry = config.registry ?: 'docker.io'
    def credentialsId = config.credentialsId ?: 'docker_credentials_id'

    stage('Building Docker Image') {
        script {
            docker.build("${dockerImage}:${tag}")
        }
    }

    stage('Pushing Docker Image') {
        withCredentials([usernamePassword(
            credentialsId: credentialsId,
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
        )]) {
            withEnv(["IMAGE_TAG=${dockerImage}:${tag}"]) {
                script {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push "$IMAGE_TAG"
                    '''
                }
            }
        }
    }
}

