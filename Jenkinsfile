pipeline {
    agent { 
    	docker {
            image "${env.AGENT_URL}:jdk-11"
            args "-u jenkins --privileged -v /var/run/docker.sock:/var/run/docker.sock"
        }
    }
    stages {
        stage('build') {
            steps {
                sh 'mvn -B -DskipTests clean install'
            }
        }
         stage('test') {
            steps {
                sh 'mvn test'
            }
            post {
              	always {
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
			        mail to: "${env.DEV_MAIL}",
			        subject: "beCPG CI - Failed tests: ${currentBuild.fullDisplayName}",
			        body: "Something is wrong with ${env.BUILD_URL}"
			    }
            }
        }
        stage('sonar') {
 	 		steps {
			    withSonarQubeEnv('beCPG Sonar') {
			      sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
			    }
		    }
		  }

 }
    
}

