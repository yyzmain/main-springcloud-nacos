# 该镜像基于openjdk:8-jre-alpine镜像修改而来，安装了tzdata包，处理了时区问题
FROM 10.1.12.29/library/openjdk:8-jre-alpine-tz
VOLUME /tmp
#设置环境变量
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:UTF-8
ENV LC_ALL en_US.UTF-8
EXPOSE 8180
EXPOSE 8181

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

# 从pom.xml文件中来的变量
ARG TARGET_FILE
# 从pom.xml文件中来的变量
ARG JAR_NAME
# 压缩包加入镜像中
ADD target/${TARGET_FILE} app.zip
# 执行解压后，压缩包内的文件变成根路径
RUN ["unzip","app.zip"]

# cd到应用根路径内
WORKDIR ${JAR_NAME}
ENTRYPOINT ["sh","bin/start.sh"]