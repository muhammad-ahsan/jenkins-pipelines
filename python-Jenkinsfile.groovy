pipeline {
    agent any

    stages {
        stage('System configuration') {
            steps {
                echo 'CI/CD Pipeline is triggered'
                // Check OS details of Jenkins Server
                sh 'uname -mrs'
                // Work-around for docker command not found
                sh 'export PATH=$PATH:/usr/local/bin'
                // Check docker integration with Jenkins
                sh 'docker --version'
            }
        }
        stage("Checkout Code") {
            steps {
                script {
                    git branch: "main",
                        credentialsId: '77055661-9ced-4e36-97b7-39bb023caded',
                        url: 'https://github.com/muhammad-ahsan/sample-pyproject.git'
                }
            }
        }
        stage('Installing packages') {
            steps {
                script {
                    sh 'python3 -m pip install --user virtualenv'
                    sh 'python3 -m virtualenv .venv'
                    sh 'source .venv/bin/activate'
                    sh 'python3 --version'
                    echo 'Installing packages in newly created python environment'
                    sh 'python3 -m pip install --upgrade pip'
                    sh 'python3 -m pip install --user pylint pytest pytest-cov'
                    sh 'python3 -m pip install -r requirements.txt'
                }
            }
        }
        stage('Code Linting'){
           steps {
                sh 'whereis python3'
                sh 'python3 -m pylint --output-format=parseable $(git ls-files "*.py") --msg-template="{path}:{line}: [{msg_id}({symbol}), {obj}] {msg}" || cat pylint.log || echo "pylint exited with status = $?"'

                // Warnings Next Generation Plugin
                recordIssues(
                        unstableTotalHigh: 100,
                        enabledForFailure: true,
                        aggregatingResults: true,
                        tool: pyLint(pattern: 'pylint.log')
                )
                echo "Generating Report - Linting Success"
            }
           post {
                failure {
                    error('Abort because of pylint warnings')
                }
           }
        }
        stage('Code Testing'){
           steps {
                sh 'python3 -m pytest --cov ./ --cov-report html --verbose'
                publishHTML(target:
                    [allowMissing: false,
                      alwaysLinkToLastBuild: false,
                    keepAll: false,
                    reportDir: 'htmlcov',
                    reportFiles: 'index.html',
                    reportName: 'Test Report',
                    reportTitles: '']
                )
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '/usr/local/bin/docker build -t dockerhubusername/sample-pyproject:build-$BUILD_NUMBER .'
                echo 'Build Image Completed'
            }
        }
        stage('Push Docker to Registry') {
            steps {
                sh '/usr/local/bin/docker build -t dockerhubusername/sample-pyproject:build-$BUILD_NUMBER .'
                echo 'Build Image Completed'
            }
        }
    }
    post {
        always {
            echo 'Post stages pipeline cleanup'
            // Logout from Docker Registry
        }
   }
}

