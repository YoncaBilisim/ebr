VU=src/main/java/com/yoncabt/ebr/util/VersionUtil.java
TOMCAT=apache-tomcat-8.0.28

war: versiyon
	./mvnw clean install -DskipTests

clean:
	rm -rf target

builder:
	git push origin master
	ssh yonca-builder "cd ebr/ebr && git pull && make build-on-builder"

build-on-builder:
	git pull origin master
	PATH="$$HOME/jdk8/bin:$$PATH" ./mvnw clean install -DskipTests
	cd target && md5sum ebr.war > md5sum.txt && mv ebr.war ~/builder.yoncabt.com.tr/ebr && mv md5sum.txt ~/builder.yoncabt.com.tr/ebr


versiyon:
	sed -i "0,/RE/s/WAR_DATE = [^;]*/WAR_DATE = 1000L * `date +%s`/" $(VU)
	sed -i "0,/RE/s/GIT_ID = [^;]*/GIT_ID = \"`git rev-parse HEAD`\"/" $(VU)
	git commit $(VU) -m "versiyon dosyası değişikliği"

release:
	rm -rf ebr target ebr.tar.xz
	./mvnw clean compile war:exploded
	mkdir ebr
	aria2c --continue -x5 -j5 -s5 --checksum=md5=4b7ba7a5af0a5c395c0740fc011b59d1 http://download.yoncabt.com.tr/d/$(TOMCAT).tar.gz
	tar xf $(TOMCAT).tar.gz -C ebr
	rm ebr/$(TOMCAT)/webapps/{docs,examples} -rf
	cp -r target/ebr ebr/$(TOMCAT)/webapps
	tar cJf ebr.tar.xz ebr
