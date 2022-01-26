#!/bin/sh

export COMPOSE_FILE_PATH="${PWD}/target/classes/docker/docker-compose.yml"

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker volume create becpg-artworks-acs-volume
    docker volume create becpg-artworks-db-volume
    docker volume create becpg-artworks-ass-volume
    docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml up --build -d
}

start_share() {
    docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml up --build -d becpg-artworks-share
}

start_acs() {
    docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml up --build -d becpg-artworks-acs
}

down() {
    if [ -f "$COMPOSE_FILE_PATH" ]; then
        docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml down
    fi
}


deploy_fast(){

	#becpg-share
	docker cp becpg-artworks-share/src/main/assembly/web/. docker_becpg-artworks-share_1:/usr/local/tomcat/webapps/share/
	#docker cp becpg-artworks-share/src/main/resources/alfresco/. docker_becpg-artworks-share_1:/usr/local/tomcat/webapps/share/WEB-INF/classes/alfresco/

	#wget --delete-after --http-user=admin --http-password=admin --header=Accept-Charset:iso-8859-1,utf-8 --header=Accept-Language:en-us --post-data reset=on http://localhost:8180/share/page/index

}


purge() {
    docker volume rm -f becpg-artworks-acs-volume
    docker volume rm -f becpg-artworks-db-volume
    docker volume rm -f becpg-artworks-ass-volume
}

build() {
    $MVN_EXEC clean package
}

build_share() {
    docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml kill becpg-artworks-share
    yes | docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml rm -f becpg-artworks-share
    $MVN_EXEC clean package -pl becpg-artworks-share,becpg-artworks-share-docker
}

build_acs() {
    docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml kill becpg-artworks-acs
    yes | docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml rm -f becpg-artworks-acs
    $MVN_EXEC clean package -pl becpg-artworks-integration-tests,becpg-artworks-platform,becpg-artworks-platform-docker
}

tail() {
    docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml logs -f
}

tail_all() {
    docker-compose -f "$COMPOSE_FILE_PATH" -f docker-compose.override.yml logs --tail="all"
}

prepare_test() {
    $MVN_EXEC verify -DskipTests=true -pl becpg-artworks-platform,becpg-artworks-integration-tests,becpg-artworks-platform-docker
}

test() {
    $MVN_EXEC verify -pl becpg-artworks-platform,becpg-artworks-integration-tests
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  build_start_it_supported)
    down
    build
    prepare_test
    start
    tail
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  purge)
    down
    purge
    ;;
  tail)
    tail
    ;;
  deploy_fast)
    deploy_fast
    ;;  
  reload_share)
    build_share
    start_share
    tail
    ;;
  reload_acs)
    build_acs
    start_acs
    tail
    ;;
  build_test)
    down
    build
    prepare_test
    start
    test
    tail_all
    down
    ;;
  test)
    test
    ;;
  *)
    echo "Usage: $0 {build_start|build_start_it_supported|start|stop|purge|tail|reload_share|reload_acs|build_test|test}"
esac