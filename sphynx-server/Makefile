include .env
export $(shell sed 's/=.*//' .env)

.PHONY: run-server connect-db proto-out
.DEFAULT_GOAL := run_server

run-server:
	docker compose down -v && \
	docker image prune -f && \
	docker compose up --build -d

connect-db:
	docker compose exec -it database psql -U $(POSTGRES_USER) -d $(POSTGRES_DB)

SOURCE = /usr/bin/protobuf-31.0/src
INPUT_DIRECTORY = ./sphynx-server/src/main/resources/proto
OUTPUT_DIRECTORY = ./sphynx-server/src/main/java
PROTO_FILES := $(wildcard $(INPUT_DIRECTORY)/*.proto)

proto-out:
	@echo "Generating protobuf and gRPC files from: $(PROTO_FILES)"
	protoc -I $(SOURCE) -I $(INPUT_DIRECTORY) \
		--java_out=$(OUTPUT_DIRECTORY) \
		--java-grpc_out=$(OUTPUT_DIRECTORY) \
		$(PROTO_FILES)
