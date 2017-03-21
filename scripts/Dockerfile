FROM java:8-jre-alpine

#add timezone and default it to Shanghai
RUN apk --update add --no-cache tzdata
ENV TZ=Asia/Shanghai

RUN mkdir -p /app/log
COPY  autoscale-java.jar  /app/autoscale-java.jar

EXPOSE 9050
VOLUME ["/app/log"]
WORKDIR /app/

ENTRYPOINT ["java","-jar","autoscale-java.jar"]
CMD []