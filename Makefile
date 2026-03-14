.PHONY: run stag prod test coverage clean deps simple-curl-sum help

RUN=run
DEV-PROFILE=stag
PROD-PROFILE=prod
TEST-PROFILE=test

run:
	@clojure -M:run

stag:
	@clojure -M:$(RUN) $(DEV-PROFILE)

prod:
	@clojure -M:$(RUN) $(PROD-PROFILE)

test:
	@clojure -M:$(TEST-PROFILE) -m kaocha.runner

coverage:
	clojure -M:$(TEST-PROFILE) -m kaocha.runner --plugin kaocha.plugin/cloverage
	@echo "Coverage report: target/coverage/index.html"

clean:
	@rm -rf .cpcache

deps:
	@clojure -P

help:
	@echo "Available targets:"
	@echo "  run       - Run the application with REPL profile (default)"
	@echo "  stag      - Run the application with staging profile"
	@echo "  prod      - Run the application with production profile"
	@echo "  test      - Run tests with test profile"
	@echo "  coverage   - Run tests and generate coverage report"
	@echo "  clean     - Clean build artifacts"
	@echo "  deps      - Print project dependencies"
	@echo "  help      - Display this help message"

simple-curl-sum:
	curl "http://localhost:8080/v1/sum?a=10&b=20"
