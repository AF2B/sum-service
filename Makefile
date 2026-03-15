.PHONY: run stag prod test coverage clean deps build docker-build docker-push docker-run simple-curl-sum help

RUN=run
DEV-PROFILE=stag
PROD-PROFILE=prod
TEST-PROFILE=test

REGISTRY   = ghcr.io/af2b
IMAGE      = $(REGISTRY)/sum-service
VERSION    = $(shell git describe --tags --always --dirty 2>/dev/null || echo "dev")
SHA        = $(shell git rev-parse --short HEAD 2>/dev/null || echo "unknown")
PORT       = 8080
PROFILE    = prod

run:
	@clojure -M:run

stag:
	@clojure -M:$(RUN) $(DEV-PROFILE)

prod:
	@clojure -M:$(RUN) $(PROD-PROFILE)

test:
	@clojure -M:$(TEST-PROFILE) -m kaocha.runner

coverage:
	@clojure -M:$(TEST-PROFILE) -m kaocha.runner --plugin kaocha.plugin/cloverage
	@echo "Coverage report: target/coverage/index.html"

build:
	@APP_VERSION=$(VERSION) clojure -T:build uber

clean:
	@rm -rf .cpcache target

deps:
	@clojure -P

docker-build:
	@docker build \
		--build-arg APP_VERSION=$(VERSION) \
		--build-arg PORT=$(PORT) \
		--build-arg PROFILE=$(PROFILE) \
		-t $(IMAGE):$(SHA) \
		-t $(IMAGE):$(VERSION) \
		-t $(IMAGE):latest \
		.

docker-push:
	@docker push $(IMAGE):$(SHA)
	@docker push $(IMAGE):$(VERSION)
	@docker push $(IMAGE):latest

docker-run:
	@docker run --rm \
		-p $(PORT):$(PORT) \
		-e PROFILE=$(PROFILE) \
		$(IMAGE):latest

help:
	@echo "Available targets:"
	@echo "  run          - Run the application with REPL profile (default)"
	@echo "  stag         - Run the application with staging profile"
	@echo "  prod         - Run the application with production profile"
	@echo "  test         - Run tests with test profile"
	@echo "  coverage     - Run tests and generate coverage report"
	@echo "  build        - Compile uberjar to target/app.jar"
	@echo "  clean        - Clean build artifacts (.cpcache, target)"
	@echo "  deps         - Resolve and cache dependencies"
	@echo "  docker-build - Build Docker image (tags: sha, version, latest)"
	@echo "  docker-push  - Push all Docker tags to GHCR"
	@echo "  docker-run   - Run container locally on PORT=$(PORT)"
	@echo "  help         - Display this help message"
	@echo ""
	@echo "Variables (override with make <target> VAR=value):"
	@echo "  REGISTRY=$(REGISTRY)"
	@echo "  IMAGE=$(IMAGE)"
	@echo "  VERSION=$(VERSION)"
	@echo "  SHA=$(SHA)"
	@echo "  PORT=$(PORT)"
	@echo "  PROFILE=$(PROFILE)"

simple-curl-sum:
	curl "http://localhost:8080/v1/sum?a=10&b=20"
