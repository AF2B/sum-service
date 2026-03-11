.PHONY: run dev prod test clean deps simple-curl-sum

APP=pedestal-integrant-demo

DEV-PROFILE=dev
PROD-PROFILE=prod
TEST-PROFILE=test

run:
	clojure -M:run

dev:
	clojure -M:$(DEV-PROFILE)

prod:
	clojure -M:$(PROD-PROFILE)

test:
	clojure -M:$(TEST-PROFILE)

clean:
	rm -rf .cpcache

deps:
	clojure -P

simple-curl-sum:
	curl "http://localhost:8080/sum?a=10&b=20"