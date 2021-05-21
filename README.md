# becpg-artworks

Provides two additional features to your Alfresco Content Service. This addon was created by beCPG : https://www.becpg.fr/

# Annotation feature : provides an annotation tool for PDF files
  * In order to use the pdf file annotation feature, you need to provide a Kami license key by setting the property "beCPG.annotationAuthorization" under docker-compose.override.yml (sample file provided)
  
![](doc-images/annotation.png) ![](doc-images/annotation2.png)

# Comparison feature : enables comparison button in document version view

![](doc-images/comparison.png) ![](doc-images/comparison2.png)

# Setup
 * download the two AMPs provided by the becpg-artworks release
 * install the two AMPs into your content service by running "java -jar /root/alfresco-mmt.jar install /root/amp/ webapps/alfresco -nobackup -directory -force" in your Dockerfile
 
 
# Build

This is an All-In-One (AIO) project for Alfresco SDK 4.2.

Run with `./run.sh build_start` or `./run.bat build_start` and verify that it

 * Runs Alfresco Content Service (ACS)
 * Runs Alfresco Share
 * Runs Alfresco Search Service (ASS)
 * Runs PostgreSQL database
 * Deploys the JAR assembled modules
 
All the services of the project are now run as docker containers. The run script offers the next tasks:

 * `build_start`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment composed by ACS, Share, ASS and 
 PostgreSQL and tail the logs of all the containers.
 * `build_start_it_supported`. Build the whole project including dependencies required for IT execution, recreate the ACS and Share docker images, start the 
 dockerised environment composed by ACS, Share, ASS and PostgreSQL and tail the logs of all the containers.
 * `start`. Start the dockerised environment without building the project and tail the logs of all the containers.
 * `stop`. Stop the dockerised environment.
 * `purge`. Stop the dockerised container and delete all the persistent data (docker volumes).
 * `tail`. Tail the logs of all the containers.
 * `reload_share`. Build the Share module, recreate the Share docker image and restart the Share container.
 * `reload_acs`. Build the ACS module, recreate the ACS docker image and restart the ACS container.
 * `build_test`. Build the whole project, recreate the ACS and Share docker images, start the dockerised environment, execute the integration tests from the
 `integration-tests` module and stop the environment.
 * `test`. Execute the integration tests (the environment must be already started).

