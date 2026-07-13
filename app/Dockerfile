FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .

RUN chmod +x gradlew && ./gradlew shadowJar

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar

EXPOSE 7070

CMD ["java", "-jar", "app.jar"]
