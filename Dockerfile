# ---------- build ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy the wrapper and build scripts first to cache dependencies
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY library-common/build.gradle.kts    library-common/
COPY library-employee/build.gradle.kts  library-employee/
COPY library-customer/build.gradle.kts  library-customer/
COPY library-book/build.gradle.kts      library-book/
COPY library-borrow/build.gradle.kts    library-borrow/
COPY library-return/build.gradle.kts    library-return/
COPY library-scheduler/build.gradle.kts library-scheduler/
COPY library-report/build.gradle.kts    library-report/
COPY library-app/build.gradle.kts       library-app/
RUN chmod +x gradlew && ./gradlew --no-daemon -q dependencies > /dev/null || true

COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon :library-app:bootJar -x test

# ---------- run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/library-app/build/libs/library-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
