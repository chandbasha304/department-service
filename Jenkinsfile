pipeline {
    agent any

    environment {
        PROJECT_ID = 'prefab-lamp-498812-u8'
        REGION = 'us-central1'
        REPOSITORY = 'ems-repo'
        IMAGE_NAME = 'department-backend'
        IMAGE = "${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${IMAGE_NAME}:latest"
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== Checkout Started ==='
                git url: 'https://github.com/chandbasha304/department-service.git',
                    branch: 'main'
                echo '=== Checkout Completed ==='
            }
        }

        stage('Build Application') {
            steps {
                echo '=== Maven Build Started ==='
                sh '''
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests
                '''
                echo '=== Maven Build Completed ==='
            }
        }

        stage('Build Docker Image') {
            steps {
                echo '=== Docker Build Started ==='
                sh '''
                    docker build -t ${IMAGE} .
                '''
                echo '=== Docker Build Completed ==='
            }
        }

        stage('Authenticate Artifact Registry') {
            steps {
                echo '=== Artifact Registry Authentication Started ==='
                sh '''
                    gcloud auth configure-docker us-central1-docker.pkg.dev --quiet
                '''
                echo '=== Artifact Registry Authentication Completed ==='
            }
        }

        stage('Push Image') {
            steps {
                echo '=== Push Started ==='
                sh '''
                    docker push ${IMAGE}
                '''
                echo '=== Push Completed ==='
            }
        }

        stage('Configure Firewall') {
            steps {
                echo '=== Configure Firewall Started ==='
                sh '''
                    gcloud compute firewall-rules create allow-backend-port-8081 \
                        --action=ALLOW \
                        --rules=tcp:8081 \
                        --direction=INGRESS \
                        --priority=1000 \
                        --network=default \
                        --source-ranges=0.0.0.0/0 \
                        --project=prefab-lamp-498812-u8 || true
                '''
                echo '=== Configure Firewall Completed ==='
            }
        }

        stage('Deploy') {
            steps {
                echo '=== Deployment Started ==='
                sh '''
                    # Ensure the network exists
                    docker network create ems-network || true

                    docker pull ${IMAGE}

                    docker stop department-backend || true
                    docker rm department-backend || true

                    # Run with --network ems-network mapping host port 8081 to container port 8080
                    docker run -d \
                        --name department-backend \
                        --network ems-network \
                        -p 8081:8080 \
                        ${IMAGE}

                    docker ps
                '''
                echo '=== Deployment Completed ==='
            }
        }
    }

    post {
        success {
            echo '====================================='
            echo 'PIPELINE EXECUTED SUCCESSFULLY'
            echo '====================================='
        }
        failure {
            echo '====================================='
            echo 'PIPELINE FAILED'
            echo '====================================='
        }
        always {
            echo '====================================='
            echo 'PIPELINE FINISHED'
            echo '====================================='
        }
    }
}
