FROM bellsoft/liberica-openjdk-alpine-musl:21
RUN set -xe && apk --no-cache add ttf-dejavu fontconfig

ENV TZ=UTC-8

WORKDIR /app
COPY ./business/target/novel-backend-main.jar novel-backend-main.jar

CMD ["sh","-c","cd /app && java -jar -XX:MaxRAM=128M -Xss256k novel-backend-main.jar --spring.profiles.active=prod"]