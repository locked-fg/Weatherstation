@echo off
set PATH=%PATH%;C:\Users\Franz\AppData\Roaming\NetBeans\7.4\maven\bin\
set M2_HOME=C:\Users\Franz\AppData\Roaming\NetBeans\7.4\maven

REM http://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html
mvn install:install-file -Dfile=libs\Tinkerforge-2.0.14.jar -DpomFile=libs\Tinkerforge-2.0.14.pom
