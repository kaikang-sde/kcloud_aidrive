# ================================================
# 构建阶段：使用 Maven 构建 Spring Boot 项目
# ================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# 复制源码到容器中
COPY . .

# 使用完整构建参数构建 Spring Boot 可执行 JAR 包
RUN mvn clean package \
    -DskipTests=true \
    -Dmaven.test.skip=true \
    -Dmaven.javadoc.skip=true \
    -Dmaven.compile.fork=true \
    -Dmaven.compiler.source=21 \
    -Dmaven.compiler.target=21 \
    -Dmaven.compiler.forceJavacCompilerUse=true \
    -Dmaven.compiler.showWarnings=true \
    -Dmaven.compiler.showDeprecation=true

# ================================================
# 运行阶段：使用精简版 JRE 镜像运行 Spring Boot 应用
# ================================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 复制构建好的 JAR 包
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# 设置默认时区为 UTC（建议用于服务器环境）
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置 JVM 启动参数（适配 1GB–2GB EC2 实例）
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:MetaspaceSize=128m \
    -XX:MaxMetaspaceSize=256m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/dump \
    -Xlog:gc*:file=/app/gc.log \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0"

# 暴露应用端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]