FROM bellsoft/liberica-openjdk-alpine-musl:21
ENV TZ=GMT+8
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN set -xe && apk --no-cache add ttf-dejavu fontconfig
CMD ["sh","-c","cd /novel-backend && java -jar -XX:MaxRAM=128M -Xss256k novel-backend-main.jar --spring.profiles.active=prod"]