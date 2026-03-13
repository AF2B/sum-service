SUM SERVICE - Developer Guide
==============================

## Overview

REST API for summing two integers. Built on top of the borba mini-framework:
a set of shared Integrant components that handle all service infrastructure
so each service only writes domain code.

  borba/borba-runtime-component   config loading, ig/init, shutdown hook, -main entrypoint
  borba/borba-server-component    Pedestal HTTP server lifecycle (ig/init-key :server/http)
  borba/borba-routes-component    route expansion + Integrant glue (ig/init-key :http/routes)


## How the Mini-Framework Works

Each microservice depends on those three components via deps.edn git deps.
The service itself only needs to declare:

  1. What its domain handlers are       (handlers/http/routes.clj)
  2. What its interceptors do           (handlers/http/interceptors.clj)
  3. What its business logic does       (handlers/business/*.clj)
  4. What its Malli schemas are         (specs/*.clj)
  5. Which routes are active per env    (system/{profile}.edn)

Everything else — server startup, config loading, route expansion, shutdown
— is handled by the borba components.


## Project Structure

  src/
  |
  +-- config.edn                    Entry point: #profile selector → system/{profile}.edn
  |
  +-- com/borba/sum_service/
      |
      +-- core.clj                  Side-effect loader: requires all ig/init-key namespaces
      |                             borba-runtime-component reads :service/core-ns and
      |                             requires this ns before ig/init runs.
      |
      +-- components/
      |   base.edn                  Abstract dependency declarations (#ig/ref per component)
      |   stag.edn                  #include base.edn  (extend here for stag-specific overrides)
      |   prod.edn                  #include base.edn
      |   test.edn                  #include base.edn
      |
      +-- system/
      |   projection.clj            Integrant folding functions (reserved for future use)
      |   base.edn                  Common ig/system config + :components #profile selector
      |   stag.edn                  #deep-merge [base.edn + stag overrides]  (active routes, ports…)
      |   prod.edn                  #deep-merge [base.edn + prod overrides]
      |   test.edn                  #deep-merge [base.edn + test overrides]
      |
      +-- handlers/
      |   +-- business/
      |   |   math.clj              Pure domain logic — no HTTP, no side effects
      |   |
      |   +-- http/
      |       interceptors.clj      Pedestal interceptors: parse-query, sum, response
      |       routes.clj            ig/init-key :service/handlers — maps handler keys to interceptors
      |
      +-- specs/
      |   math.clj                  Malli schemas: SumInput, SumResult
      |
      +-- util/
          predicates.clj            Shared predicates
          validation.clj            Shared validation helpers

  test/
  +-- com/borba/sum_service/handlers/business/
  |   math_test.clj                 Unit tests for pure business logic
  +-- flows/
      sum_flow_test.clj             HTTP integration tests (state-flow + response-for)

  tests.edn                         Kaocha runner config (coverage output path)
  deps.edn                          Dependencies + :run and :test aliases
  Makefile                          Developer tasks


## Config Loading (how #profile + borba-runtime-component work together)

  make stag  →  clojure -M:run stag

  borba.runtime.main/-main receives "stag" and:
    1. Reads src/config.edn with {:profile :stag}
       #profile resolves to:  #include "…/system/stag.edn"
    2. system/stag.edn expands via #deep-merge [system/base.edn + stag overrides]
       system/base.edn has :components #profile → resolves components/stag.edn
       components/stag.edn includes components/base.edn (#ig/ref declarations)
    3. Resulting map has :service/core-ns, :service/default-profile, :ig/system
    4. runtime requires :service/core-ns  (= com.borba.sum-service.core)
       this triggers all ig/init-key registrations as a side effect
    5. runtime calls (ig/init (:ig/system config))
       Integrant initialises :service/handlers → :http/routes → :server/http in order

Aero reader tags in use:
  #or [#env PORT 8080]          read env var PORT, fallback to 8080
  #ig/ref :key                  Integrant dependency reference
  #profile {:stag … :prod …}   select value based on active profile
  #include "relative/path.edn"  inline another EDN file (path relative to current file)
  #deep-merge [m1 m2 …]         deep-merge a vector of maps (registered in borba-runtime-component)


## Anatomy of a Request

  HTTP GET /v1/sum?a=10&b=20
       |
       v
  [parse-query]      :enter  reads :params, parses a and b as Long,
                             assoc :math/input {:a 10 :b 20} into ctx
       |
       v
  [sum-interceptor]  :enter  calls (math/sum ctx[:math/input]),
                             assoc :math/result {:result 30} into ctx
       |
       v
  [response-inter.]  :leave  builds {:status 200
                                     :headers {"Content-Type" "application/json"}
                                     :body "{\"result\":30}"}


## How to Add a New Endpoint

Example: GET /v1/multiply?a=3&b=4

### 1 — Spec  (specs/multiply.clj)

  (ns com.borba.sum-service.specs.multiply)

  (def MultiplyInput  [:map [:a int?] [:b int?]])
  (def MultiplyResult [:map [:result int?]])

### 2 — Business logic  (handlers/business/multiply.clj)

  (ns com.borba.sum-service.handlers.business.multiply
    (:require [malli.core :as m]
              [com.borba.sum-service.specs.multiply :as schema]))

  (defn multiply [{:keys [a b] :as input}]
    (when-not (m/validate schema/MultiplyInput input)
      (throw (ex-info "Invalid input" {:input input})))
    {:result (* a b)})

### 3 — Interceptor  (handlers/http/interceptors.clj — add to existing file)

  (def multiply-interceptor
    {:name  ::multiply
     :enter (fn [ctx]
              (assoc ctx :math/result
                     (multiply/multiply (:math/input ctx))))})

  The existing parse-query and response-interceptor are reused as-is.

### 4 — Register the handler  (handlers/http/routes.clj)

  (defmethod ig/init-key :service/handlers [_ _]
    {:math/sum      [i/parse-query i/sum-interceptor      i/response-interceptor]
     :math/multiply [i/parse-query i/multiply-interceptor i/response-interceptor]})

### 5 — Activate the route per profile  (system/stag.edn and system/prod.edn)

  Inside the #deep-merge overrides:

  {:ig/system
   {:http/routes
    {:routes [["/v1/sum"      :get :math/sum]
              ["/v1/multiply" :get :math/multiply]]}}}


## Testing

Two layers:

  Unit (test/com/borba/sum_service/handlers/business/)
    Plain clojure.test on pure functions. No HTTP, no server, no Integrant.

  Integration (test/flows/)
    state-flow + Pedestal's response-for. Full interceptor chain in-memory.

    Pattern:
      1. require borba.routes.component and routes ns (side-effect: registers ig/init-key methods)
      2. (ig/init-key :service/handlers {}) → builds the handlers map
      3. (ig/init-key :http/routes {:routes [...] :handlers handlers}) → builds the route set
      4. build-service-fn wraps that into an in-memory Pedestal service
      5. flow/flow sequences requests via (ptest/response-for service-fn method path)
      6. Assertions inside (flow/return (is ...))

  make test      run all tests
  make coverage  run tests + HTML report at target/coverage/index.html


## Developer Commands

  make deps      download/resolve all deps
  make stag      start service with staging profile  (port 8080)
  make prod      start service with production profile
  make test      run all tests
  make coverage  tests + coverage report
  make clean     remove .cpcache

  curl "http://localhost:8080/v1/sum?a=10&b=20"


## Creating a New Borba Service

  1. Create a new repo with the same layout as this one
  2. Rename all occurrences of sum-service → your-service  (namespaces + file paths)
  3. Add the three borba deps to deps.edn with their current git tags/shas:
       borba/borba-runtime-component
       borba/borba-server-component
       borba/borba-routes-component
  4. Replace handlers/business/math.clj and specs/math.clj with your domain
  5. Register your handlers in routes.clj (:service/handlers)
  6. Add your routes to system/stag.edn and system/prod.edn
  7. make stag  —  it should start with no further changes

  Config, server lifecycle, route expansion: already handled by the components.


## Publishing a New Component Version

  1. Make changes in the component repo
  2. git tag vX.Y.Z && git push origin vX.Y.Z
  3. Copy the commit sha of the tag:  git rev-parse vX.Y.Z
  4. Update :git/tag and :git/sha in each service's deps.edn
  5. make deps  (in each service)
