# build project (Первый этап: сборка приложения с Maven и JDK 21)

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Копируем файл pom.xml
COPY pom.xml .
RUN mvn dependency:go-offline
# Копируем исходники
COPY src ./src
# Сборка приложения (обычно собирается jar с зависимостями)
RUN mvn clean package -DskipTests

# runtime project (Второй этап: запуск приложения на OpenJDK 21)
# С одного этапа на другой копируются только нужные нам артефакты (то есть зависимости нашего приложения не
# упаковываются в конечный образ, как в предыдущем методе)

FROM eclipse-temurin:21-jre
LABEL authors="Semenikhin A.F."
WORKDIR /app
# Копируем готовый jar из этапа сборки
COPY --from=build /app/target/*.jar myapp.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "myapp.jar"]

# Ключевые моменты:
# В первом этапе используется официальный Maven-образ с JDK 21 (например, maven:3.9-eclipse-temurin-21), в котором
# происходит сборка проекта Maven. Это позволяет собрать все зависимости и создать jar без мусора компилятора.
# Во втором этапе используется более лёгкий образ с Java Runtime Environment (JRE) на базе OpenJDK 21 от Eclipse Temurin
# (eclipse-temurin:21-jre) — это оптимальный вариант для запуска приложения.
# Такой подход позволяет создать компактный итоговый контейнер без исходников и инструментов сборки.


# Первый этап - builder
# FROM eclipse-temurin:21-jdk-alpine AS builder
# WORKDIR /opt/app
# COPY .mvn/ .mvn
# COPY mvnw pom.xml ./
# RUN ./mvnw dependency:go-offline
# COPY ./src ./src
# RUN ./mvnw clean install
#
# # Второй этап - runtime
# FROM eclipse-temurin:21-jre-alpine
# LABEL authors="Semenikhin A.F"
# WORKDIR /opt/app
# COPY --from=builder /opt/app/target/*.jar /opt/app/*.jar
# EXPOSE 8080
# ENTRYPOINT ["java", "-jar", "/opt/app/*.jar"]


# При использовании многоэтапной сборки не нужно вручную запускать команду "mvn package" и создавать jar-ник нашего
# проекта, в этом случае jar-ник будет генерироваться автоматически.

# FROM eclipse-temurin:21-jdk-alpine AS builder
# Указываем docker-у, какой образ мы будем использовать для сборки нашего проекта и указываем именно jdk, так как нам
# понадобятся инструменты для компиляции нашего проекта.

# as builder - это название, которое мы присвоили, для того чтобы обратиться с другого слоя контейнера для получения
# данных.

# WORKDIR /opt/app - создаем папки в данном слое

# COPY .mvn/ .mvn - копируем папку mvn и все ее содержимое в такую же папку в корень данного слоя, для того чтобы у нас
# был maven.

# COPY mvnw pom.xml ./ - копируем mvnw и pom.xml тоже в корень данного слоя.

# RUN ./mvnw dependency:go-offline - данной строчкой мы подтягиваем все зависимости из pom.xml в наш слой, чтобы у нас
# в контейнере были все зависимости, необходимые для нашего проекта.

# COPY ./src ./src - копируем непосредственно папку с нашим проектом.

# RUN ./mvnw clean install - запускаем maven, который все чистит и создает jar-ник нашего проекта.

# строчка 10: WORKDIR /opt/app - создаем папки в другом слое.
# COPY --from=builder /opt/app/target/*.jar /opt/app/*.jar - используя доступ к первому слою, копируем jar-ник с нашим
# проектом в данный слой.