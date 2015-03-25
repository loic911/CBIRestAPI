README for retrieval
==========================

Full documentation is here: http://loic911.github.io/cbirest/ (work in progress)

Requirements
* Linux, Mac OS (Windows should work)
* Java 8
* Redis (not mandatory)
* Tomcat 7 (not mandatory)

Redis is mandatory if you want persistance. If you use Memory mode, you will lost your data if you reboot the app.

Download

Download the last distribution: https://github.com/loic911/CBIRestAPI/releases

The retrieval-*.war file from the zip is a war with an embedded TOMCAT server (for quick install). The other war (.war.original) can be install inside a web server/servlet container (only test with tomcat).

Quick install

    unzip CBIRest-$VERSION.zip
    cd CBIRest-$VERSION
    java -jar retrieval-$VERSION-SNAPSHOT.war --spring.profiles.active=prod --retrieval.store.name=MEMORY
    
open your browser and go to http://localhost:9999/

For more doc: Go to http://loic911.github.io/cbirest/
