
#FROM gradle:jdk8-alpine as gradleimage
#WORKDIR /work
#RUN apt-get install -y git


FROM openjdk:11
#FROM adoptopenjdk:11-hotspot-focal

#FROM gradle:jdk8
COPY --from=python:3.6 / /
#COPY --from=gradle:jdk8-slim / /

ARG SPLEETER_VERSION=1.5.3
ENV MODEL_PATH /model

RUN mkdir -p /model
RUN apt-get update && apt-get install -y ffmpeg libsndfile1
RUN pip install musdb museval
RUN pip install spleeter==${SPLEETER_VERSION}


RUN apt-get update && apt-get install -y git zip unzip curl wget
RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get -y install xorg gtk2-engines libasound2 libgtk2.0-0

WORKDIR /work
#RUN pip install gradle
#USER root                # This changes default user to root
#RUN chown -R gradle /work/horde # This changes ownership of folder
#USER gradle              # This changes the user back to the default user "gradle"
#RUN ./gradlew build --stacktrace
RUN git clone --branch dockerintegration https://github.com/raver1975/horde.git /work/horde
WORKDIR /work/horde
RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN curl -s "https://get.sdkman.io" | bash
RUN chmod a+x "/root/.sdkman/bin/sdkman-init.sh"
RUN source "/root/.sdkman/bin/sdkman-init.sh" && sdk install gradle && gradle wrapper --gradle-version 7.3.3
#RUN /work/horde/gradlew jproRelease
#RUN source "/root/.sdkman/bin/sdkman-init.sh" && sdk install gradle && gradle wrapper --gradle-version 7.3.3
#RUN gradle jproRelease
#RUN unzip /work/horde/build/distributions/horde-jpro.zip
#RUN gradle jproRun
RUN ls /work/horde/
RUN #nohup /work/horde/gradlew jproRun & sleep infinity
ENTRYPOINT /work/horde/gradlew jproRun && /bin/bash
#ENTRYPOINT ["gradle jproRun"]
