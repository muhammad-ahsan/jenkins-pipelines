pipeline {
    agent any

    stages {
        stage('Welcome Message') {
            steps {
                echo 'CI/CD Pipeline is triggered'
                sh 'uname -mrs'
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
                    sh 'python3 --version'
                    sh 'python3 -m venv venv'
                    sh 'ls -all'
                    sh 'source venv/bin/activate'
                    sh 'whereis python3'
                    sh 'python3 -m pip install --upgrade pip'
                    sh 'python3 -m pip install --user pylint pytest pytest-cov'
                    sh 'python3 -m pip install -r requirements.txt'
                }
            }
        }
        stage('Code Linting'){
           steps {
                sh 'python3 -m pylint --output-format=parseable $(git ls-files "*.py") --msg-template="{path}:{line}: [{msg_id}({symbol}), {obj}] {msg}" || cat pylint.log || echo "pylint exited with status = $?"'
                recordIssues(
                        tool: pyLint(pattern: 'pylint.log'),
                        unstableTotalHigh: 100,
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
                    reportTitles: ''])  
                   
                echo "Testing Success"   
                }  
        }
    }
}
