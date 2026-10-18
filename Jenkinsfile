pipeline {
    agent any

    parameters {
        choice(
            name: 'APP_ENV',
            choices: ['qa', 'dev'],
            description: 'Target application environment'
        )
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'edge', 'firefox'],
            description: 'Browser installed on the Jenkins agent'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run the browser without a visible window'
        )
        choice(
            name: 'TEST_SUITE',
            choices: ['Purchase', 'Regression', 'ErrorValidationTest', 'CucumberTests', 'LinkValidation'],
            description: 'Maven test profile to run'
        )
    }

    options {
        timestamps()
        timeout(time: 20, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        DEV_BASE_URL = 'https://rahulshettyacademy.com/client'
        QA_BASE_URL = 'https://rahulshettyacademy.com/client'
        QA_TEST_PRODUCT_1 = 'ZARA COAT 3'
        QA_TEST_PRODUCT_2 = 'ADIDAS ORIGINAL'
        DEV_TEST_PRODUCT_1 = 'ZARA COAT 3'
        DEV_TEST_PRODUCT_2 = 'ADIDAS ORIGINAL'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Preflight') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'java -version && mvn --version'
                    } else {
                        bat 'java -version && mvn --version'
                    }
                }
            }
        }

        stage('UI tests') {
            steps {
                script {
                    def environmentPrefix = params.APP_ENV.toUpperCase()
                    def emailVariable = "${environmentPrefix}_TEST_USER_EMAIL"
                    def passwordVariable = "${environmentPrefix}_TEST_USER_PASSWORD"
                    withCredentials([
                        string(credentialsId: "${params.APP_ENV}-test-user-email", variable: emailVariable),
                        string(credentialsId: "${params.APP_ENV}-test-user-password", variable: passwordVariable)
                    ]) {
                        if (isUnix()) {
                            sh 'mvn --batch-mode --no-transfer-progress -DfailIfNoTests=true -Dapp.env=$APP_ENV -Dbrowser=$BROWSER -Dheadless=$HEADLESS -P "$TEST_SUITE" test'
                        } else {
                            bat 'mvn --batch-mode --no-transfer-progress -DfailIfNoTests=true -Dapp.env=%APP_ENV% -Dbrowser=%BROWSER% -Dheadless=%HEADLESS% -P %TEST_SUITE% test'
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
            archiveArtifacts allowEmptyArchive: true, artifacts: 'target/surefire-reports/**,target/cucumber.html,report/**,reportss/**'
        }
    }
}
