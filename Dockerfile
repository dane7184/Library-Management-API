# ---------- build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy POMs first to cache dependencies
COPY pom.xml .
COPY library-common/pom.xml    library-common/
COPY library-employee/pom.xml  library-employee/
COPY library-customer/pom.xml  library-customer/
COPY library-book/pom.xml      library-book/
COPY library-borrow/pom.xml    library-borrow/
COPY library-return/pom.xml    library-return/
COPY library-scheduler/pom.xml library-scheduler/
COPY library-report/pom.xml    library-report/
COPY library-app/pom.xml       library-app/
RUN mvn -B -q dependency:go-offline -DskipTests || true

COPY . .
RUN mvn -B -pl library-app -am clean package -DskipTests

# ---------- run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/library-app/target/library-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
