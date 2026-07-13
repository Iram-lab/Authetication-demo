pipeline {
    agent any
    
    tools {
        // Enclosed in quotes because it contains a space
        nodejs 'node 24' 
    }

    stages {
        stage('Install Dependencies') {
            steps {
                dir('my-login-app') {
                    echo 'Installing npm packages...'
                    bat 'npm install'
                }
            }
        }

        stage('Lint & Test') {
            steps {
                dir('my-login-app') {
                    echo 'Running linting checks...'
                    bat 'npm run lint -- --silent' 
                }
            }
        }

        stage('Build Production Bundle') {
            steps {
                dir('my-login-app') {
                    echo 'Compiling Angular for Production...'
                    bat 'npm run build -- --configuration=production'
                }
            }
        }

        stage('Deploy to Web Server') {
    steps {
        dir('my-login-app') {
            echo 'Deploying static files to web server...'
            
            // 1. Clean existing deployment folder if it exists
            bat 'powershell -Command "if (Test-Path \'C:\\nginx\\html\\my-angular-app\') { Remove-Item -Path \'C:\\nginx\\html\\my-angular-app\\*\' -Recurse -Force }"'
            
            // 2. CRITICAL FIX: Ensure target directory path actually exists
            bat 'powershell -Command "if (!(Test-Path \'C:\\nginx\\html\\my-angular-app\')) { New-Item -ItemType Directory -Path \'C:\\nginx\\html\\my-angular-app\' -Force }"'
            
            // 3. Copy production static bundle files
            bat 'powershell -Command "Copy-Item -Path \'dist\\my-login-app\\browser\\*\' -Destination \'C:\\nginx\\html\\my-angular-app\' -Recurse -Force"'
        }
    }
}

    }
}
