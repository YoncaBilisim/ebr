VU=src/main/java/com/yoncabt/ebr/util/VersionUtil.java

war: versiyon
	mvn clean install -DskipTests

clean:
	rm -rf target

builder:
	ssh yonca-builder "cd ebr/ebr && git pull && make build-on-builder"

build-on-builder:
	git pull origin master
	PATH="$$HOME/jdk8/bin:$$PATH" ./mvnw clean install -DskipTests
	cd target && md5sum ebr.war > md5sum.txt && mv ebr.war ~/builder.yoncabt.com.tr/ebr && mv md5sum.txt ~/builder.yoncabt.com.tr/ebr


versiyon:
	sed -i "0,/RE/s/WAR_DATE = [^;]*/WAR_DATE = 1000L * `date +%s`/" $(VU)
	sed -i "0,/RE/s/GIT_ID = [^;]*/GIT_ID = \"`git rev-parse HEAD`\"/" $(VU)
	git commit $(VU) -m "versiyon dosyası değişikliği"


