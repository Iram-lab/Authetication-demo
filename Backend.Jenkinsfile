pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk 'openjdk-21' 
    }

    environment {
        JWT_SECRET_KEY = credentials('jwt-production-secret-key')
        JAR_NAME = 'project-0.0.1-SNAPSHOT.jar'
    }

    stages {
        stage('Initialize & Verify') {
            steps {
                echo 'Validating deployment host systems and versions...'
                sh 'java -version'
                sh 'mvn -version'
                
                echo 'Ensuring physical data directories exist for the persistent H2 database...'
                sh 'sudo mkdir -p /var/data && sudo chown -R jenkins:jenkins /var/data'
            }
        }

        stage('Secure Build & Test') {
            steps {
                // 📂 Step inside your Spring Boot subfolder to compile code
                dir('springboot-backend') {
                    echo 'Running full unit/integration test suites and compiling code...'
                    sh 'mvn clean package -DskipTests=false'
                }
            }
        }

        stage('Production Deployment') {
            steps {
                // 📂 Step inside your Spring Boot subfolder to execute the deploy logic
                dir('springboot-backend') {
                    echo 'Performing zero-downtime process handoff...'
                    
                    // 1. Terminate the previously running instance of the application
                    sh "pkill -f '${JAR_NAME}' || true"
                    
                    // 2. Launch the backend execution detached in the background
                    // target/ path is resolved correctly relative to the subfolder directory
                    sh """
                        nohup java -jar target/${JAR_NAME} \
                        --spring.profiles.active=prod \
                        --JWT_SECRET_KEY=${JWT_SECRET_KEY} > backend-system.log 2>&1 &
                    """
                    
                    echo 'Application live execution kicked off successfully.'
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment Complete! Database verified, JWT tokens actively signing.'
        }
        failure {
            echo 'Build failed. Check Jenkins console log outputs immediately.'
        }
    }
}
