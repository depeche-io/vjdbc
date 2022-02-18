Fork of VJDBC - Remote access for JDBC-Datasources
==========================================

[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=depeche-io_vjdbc&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=depeche-io_vjdbc)

UPDATED
-------
- proprietary Oracle *Parameter.setAtName methods for prepared statements
- maven build
- update to JDK 8
- EnvServletCommandSink - embedded Jetty server, start with env variables:
- embedded Jetty server - src/main/java/de/simplicit/vjdbc/server/servlet/JettyEmbeddedServer.java


     VJDBC_PORT=7001 // default 8080
     VJDBC_CONNECTIONS=dev4
     VJDBC_dev4_URL=jdbc:oracle:thin:@srv-dev-db1:1521:DGALOB
     VJDBC_dev4_DRIVER=oracle.jdbc.driver.OracleDriver
     VJDBC_dev4_USER=xxx
     VJDBC_dev4_PASSWORD=yyy

Forked from:
http://svn.code.sf.net/p/vjdbc/code/
