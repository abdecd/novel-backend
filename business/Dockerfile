FROM bellsoft/liberica-openjre-alpine:21
ENV TZ=GMT+8
CMD ["sh","-c","cd /novel-backend && java -jar -XX:MaxRAM=90M -Xss256k novel-backend-main.jar --spring.profiles.active=prod"]