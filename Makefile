.PHONY: run dev prod test coverage clean deps simple-curl-sum

APP=pedestal-integrant-demo

RUN=run
DEV-PROFILE=dev
PROD-PROFILE=prod
TEST-PROFILE=test

run:
	clojure -M:run

dev:
	clojure -M:$(RUN) $(DEV-PROFILE)

prod:
	clojure -M:$(RUN) $(PROD-PROFILE)

test:
	clojure -M:$(TEST-PROFILE) -m kaocha.runner --config-file resources/config/test.edn

coverage:
	clojure -M:$(TEST-PROFILE) -m kaocha.runner --config-file resources/config/test.edn --plugin kaocha.plugin/cloverage
	@echo "Coverage report: target/coverage/index.html"

clean:
	rm -rf .cpcache

deps:
	clojure -P

simple-curl-sum:
	curl "http://localhost:8080/v1/sum?a=10&b=20"