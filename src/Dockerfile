FROM openjdk:14-alpine AS server-build-1
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac *.java
RUN javac $(find .| grep .java)
EXPOSE 5000
# start up commands for the server container
CMD ["java", "DataStoreServer", "5000"]

FROM openjdk:14-alpine AS server-build-2
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac *.java
RUN javac $(find .| grep .java)
EXPOSE 5010
# start up commands for the server container
CMD ["java", "DataStoreServer", "5010"]

FROM openjdk:14-alpine AS server-build-3
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac *.java
RUN javac $(find .| grep .java)
EXPOSE 5020
# start up commands for the server container
CMD ["java", "DataStoreServer", "5020"]

FROM openjdk:14-alpine AS server-build-4
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac *.java
RUN javac $(find .| grep .java)
EXPOSE 5030
# start up commands for the server container
CMD ["java", "DataStoreServer", "5030"]

FROM openjdk:14-alpine AS server-build-5
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac *.java
RUN javac $(find .| grep .java)
EXPOSE 5040
# start up commands for the server container
CMD ["java", "DataStoreServer", "5040"]

FROM openjdk:14-alpine AS client-build
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN javac *.java
RUN javac $(find .| grep .java)




