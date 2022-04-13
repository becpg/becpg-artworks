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
        stage('sonar') {
 	 		steps {
			    withSonarQubeEnv('beCPG Sonar') {
			      sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar'
			    }
		    }
		  }

 }
    
}

