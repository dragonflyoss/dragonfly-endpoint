FROM gradle:latest as builder

WORKDIR /app

RUN git clone https://github.com/dragonflyoss/dragonfly-endpoint.git

RUN git checkout origin/feat：udpate_workflow

WORKDIR /app/dragonfly-endpoint

RUN gradle shadowJar

FROM pytorch/torchserve:latest

WORKDIR /home/model-server

RUN mkdir plugins-path

COPY --from=builder /app/dragonfly-endpoint/build/libs/dragonfly_endpoint-1.0-all.jar plugins-path/

EXPOSE 8080
EXPOSE 8081
EXPOSE 8082

CMD ["torchserve", "--start", "--ncs", "--model-store", "/home/model-server/model-store", "--plugins-path", "/home/model-server/plugins-path"]
