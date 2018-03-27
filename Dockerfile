FROM java:8-jre-alpine

#add timezone and default it to Shanghai
RUN apk --update add --no-cache tzdata
ENV TZ=Asia/Shanghai

RUN mkdir -p /app/log
COPY  target/auto-scale-java.jar  /app/auto-scale-java.jar

EXPOSE 9050
VOLUME ["/app/log"]
WORKDIR /app/

ENTRYPOINT ["java","-jar","auto-scale-java.jar"]
CMD []