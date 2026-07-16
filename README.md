# Page Analyzer

### Hexlet tests and linter status:

[![Actions Status](https://github.com/askirya/java-project-72/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/askirya/java-project-72/actions)
[![build](https://github.com/askirya/java-project-72/actions/workflows/build.yml/badge.svg)](https://github.com/askirya/java-project-72/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=askirya_java-project-72&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=askirya_java-project-72)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=askirya_java-project-72&metric=coverage)](https://sonarcloud.io/summary/new_code?id=askirya_java-project-72)
[![Coverage on New Code](https://sonarcloud.io/api/project_badges/measure?project=askirya_java-project-72&metric=new_coverage)](https://sonarcloud.io/summary/new_code?id=askirya_java-project-72)

## Описание

Веб-приложение для анализа страниц на SEO-пригодность. Позволяет добавлять сайты в список и запускать проверки: код ответа, заголовок `title`, тег `h1` и meta-описание.

### Демо

[https://java-projy-72.onrender.com](https://java-projy-72.onrender.com)

## Требования

- JDK 21
- Gradle (используется Gradle Wrapper из проекта)

## Установка и запуск

```bash
git clone https://github.com/askirya/java-project-72.git
cd java-project-72/app
./gradlew run
```

После запуска откройте в браузере [http://localhost:7070](http://localhost:7070).

На Windows используйте `gradlew.bat` вместо `./gradlew`.

## Тесты

```bash
cd app
./gradlew test
```

## Стек

- Java 21
- Javalin
- JTE
- HikariCP
- H2 / PostgreSQL
- JUnit 5
