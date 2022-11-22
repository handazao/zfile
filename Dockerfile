FROM java:8

MAINTAINER zfile <zfile@163.com>

ENV PROJECT_HOME /root/.zfile/
ENV JAVA_OPTS="-Xms256m -Xmx256m -Xss1m -Xmn128m"

RUN mkdir -p "$PROJECT_HOME"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \&& echo 'Asia/Shanghai' >/etc/timezone

VOLUME /tmp

ADD target/zfile-4.1.2.jar zfile.jar

ENTRYPOINT ["java", "-Xmx1024m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/zfile.jar"]

EXPOSE 8080
