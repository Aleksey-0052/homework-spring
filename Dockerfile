# build project
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# runtime project
# С одного этапа на другой копируются только нужные нам артефакты
FROM eclipse-temurin:21-jre
LABEL authors="Semenikhin A.F."
WORKDIR /app
COPY --from=build /app/target/*.jar user-microservice.jar
ENTRYPOINT ["java", "-jar", "/user-microservice.jar"]

# Ключевые моменты:

# На первом этапе используется официальный Maven-образ с JDK 21 (в данном случае, maven:3.9-eclipse-temurin-21),
# на котором происходит сборка проекта Maven. Это позволяет собрать все зависимости и создать jar без мусора компилятора.

# На втором этапе используется более лёгкий образ с Java Runtime Environment (JRE) на базе OpenJDK 21 от Eclipse Temurin
# (eclipse-temurin:21-jre) — это оптимальный вариант для запуска приложения.
# Такой подход позволяет создать компактный итоговый контейнер без исходников и инструментов сборки.

# Первый этап

# WORKDIR /app
# Устанавливает рабочую директорию внутри контейнера на /app. Все последующие команды (COPY, RUN и т.д.) будут
# выполняться относительно этой директории.

# COPY pom.xml .
# Копирует файл pom.xml из локальной директории в рабочую директорию контейнера (/app).

# RUN mvn dependency:go-offline
# Выполняет команду Maven, чтобы заранее загрузить все зависимости проекта в локальный кэш. Это ускоряет последующие
# сборки, так как зависимости уже будут доступны оффлайн.

# COPY src ./src
# Копирует директорию src (исходный код Java) из локальной системы в папку /app/src внутри контейнера.

# RUN mvn clean package -DskipTests
# Выполняет сборку Java-проекта с помощью Maven: очищает старые артефакты (clean), собирает проект (package), пропуская
# запуск тестов (ключ -DskipTests). В результате в директории контейнера /app/target появится собранный JAR-файл.

# Второй этап

# WORKDIR /app
# Снова устанавливает рабочую директорию /app, но уже в новом образе.

# COPY --from=build /app/target/*.jar user-microservice.jar
# Копирует собранный JAR-файл из первого этапа (build) в текущий образ. В результате в /app появится файл jar файл
# с именем user-microservice.jar.

# ENTRYPOINT ["java", "-jar", "user-microservice.jar"]
# Определяет команду, которая будет выполняться при запуске контейнера. В данном случае — запуск Java-приложения из файла
# user-microservice.jar. Используется форма exec (в квадратных скобках), что предпочтительно для корректной передачи
# сигналов внутри контейнера.