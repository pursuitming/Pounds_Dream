# ---- 构建阶段 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先复制 pom 文件，利用 Docker 缓存层
COPY pom.xml .
COPY pounds-dream-server/pom.xml ./pounds-dream-server/pom.xml

# 下载依赖（这一层会被缓存，除非 pom 变动）
RUN mvn dependency:go-offline -pl pounds-dream-server -am -B

# 复制源码并打包
COPY pounds-dream-server/src ./pounds-dream-server/src
RUN mvn clean package -pl pounds-dream-server -am -DskipTests -B

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /build/pounds-dream-server/target/*.jar app.jar

# 创建上传文件目录
RUN mkdir -p /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
