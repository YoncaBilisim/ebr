war: versiyon
	mvn clean install -DskipTests

clean:
	rm -rf target

send-war: war
	scp target/isuws.war yururdurmazm@download.yoncabt.com.tr:~/download.yoncabt.com.tr/d/isuws.war

deploy-152: war
	scp target/isuws-0.0.1-SNAPSHOT.war  tomcat@172.16.3.152:/home/tomcat/apache-tomcat-7.0.42/webapps/isu.war

versiyon:
	sed -i "0,/RE/s/WAR_DATE = [^;]*/WAR_DATE = 1000L * `date +%s`/" src/main/java/com/yoncabt/abys/isuws/util/VersionUtil.java
	sed -i "0,/RE/s/GIT_ID = [^;]*/GIT_ID = \"`git rev-parse HEAD`\"/" src/main/java/com/yoncabt/abys/isuws/util/VersionUtil.java
	git commit src/main/java/com/yoncabt/abys/isuws/util/VersionUtil.java -m "versiyon dosyası değişikliği"


