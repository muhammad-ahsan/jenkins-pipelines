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
                    sh 'python3 -m pip install --upgrade pip'
                    sh 'python3 -m pip install --user pylint'
                    sh 'python3 -m pip install --user pytest'
                    sh 'python3 -m pip install --user pytest-cov'
                    sh 'python3 -m pip install -r requirements.txt'
                }
            }
        }
        stage('Code Linting'){
           steps {
                  
                sh 'python3 -m pylint --output-format=parseable --fail-under=1 src --msg-template="{path}:{line}: [{msg_id}({symbol}), {obj}] {msg}" | tee pylint.log || echo "pylint exited with $?"'
                echo "linting Success, Generating Report"
                recordIssues enabledForFailure: true, aggregatingResults: true, tool: pyLint(pattern: 'pylint.log')

               
            }   
        }
        stage('Code Testing'){
           steps {
                sh 'python3 -m pytest --version'
                sh 'python3 -m coverage --version'
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
