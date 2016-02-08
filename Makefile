VU=src/main/java/com/yoncabt/ebr/util/VersionUtil.java

war: versiyon
	mvn clean install -DskipTests

clean:
	rm -rf target

builder:
	ssh yonca-builder "cd ebr/ebr && git pull && make build-on-builder"

build-on-builder:
	git pull origin master
	PATH="~/jdk1.8.0_45/bin:$$PATH" ./mvnw clean install -DskipTests
	cd target && md5sum ebr.war > md5sum.txt && mv ebr.war ~/builder.yoncabt.com.tr/ebr && mv md5sum.txt ~/builder.yoncabt.com.tr/ebr


send-war: war
	scp target/*.war yururdurmazm@download.yoncabt.com.tr:~/download.yoncabt.com.tr/d/

deploy-152: war
	scp target/isuws-0.0.1-SNAPSHOT.war  tomcat@172.16.3.152:/home/tomcat/apache-tomcat-7.0.42/webapps/isu.war

versiyon:
	sed -i "0,/RE/s/WAR_DATE = [^;]*/WAR_DATE = 1000L * `date +%s`/" $(VU)
	sed -i "0,/RE/s/GIT_ID = [^;]*/GIT_ID = \"`git rev-parse HEAD`\"/" $(VU)
	git commit $(VU) -m "versiyon dosyası değişikliği"


