pipeline {
    agent any
    
    tools {
        nodejs 'node 24' 
    }

    stages {
        stage('Install Dependencies') {
            steps {
                // 📂 Step into the subfolder to find package.json
                dir('my-login-app') {
                    echo 'Installing npm packages...'
                    sh 'npm install'
                }
            }
        }

        stage('Lint & Test') {
            steps {
                dir('my-login-app') {
                    echo 'Running linting checks...'
                    sh 'npm run lint -- --silent' 
                }
            }
        }

        stage('Build Production Bundle') {
            steps {
                dir('my-login-app') {
                    echo 'Compiling Angular for Production...'
                    sh 'npm run build -- --configuration=production'
                }
            }
        }

        stage('Deploy to Web Server') {
            steps {
                dir('my-login-app') {
                    echo 'Deploying static files to Nginx web root...'
                    // Cleans out the directory
                    sh 'sudo rm -rf /var/www/html/my-angular-app/*'
                    // Copies browser files out of your subfolder's dist structure
                    sh 'sudo cp -r dist/my-login-app/browser/* /var/www/html/my-angular-app/'
                }
            }
        }
    }
}
