(ns build
  "Build script for sum-service.

  Produces a self-contained uberjar suitable for containerized deployment.
  The output artifact is always named `app.jar` under `target/`, regardless
  of version, so the Dockerfile COPY instruction remains stable.

  Usage
  -----
  Resolve deps only (CI cache warm-up):
    clojure -T:build deps

  Build uberjar:
    APP_VERSION=1.2.3 clojure -T:build uber

  Clean build artifacts:
    clojure -T:build clean

  Environment Variables
  ---------------------
  APP_VERSION : semantic version injected at build time (default: \"dev\").
                Embedded into the JAR manifest for runtime introspection.
  "
  (:require [clojure.tools.build.api :as b]))

(def ^:private lib     'com.borba/sum-service)
(def ^:private version (or (System/getenv "APP_VERSION") "dev"))
(def ^:private class-dir "target/classes")
(def ^:private uber-file "target/app.jar")
(def ^:private basis    (b/create-basis {:project "deps.edn"}))

(defn clean
  "Remove all build artifacts under target/."
  [_]
  (b/delete {:path "target"}))

(defn deps
  "Resolve and cache all dependencies declared in deps.edn.
  Useful as a standalone step to warm the CI dependency cache
  before compiling source."
  [_]
  (b/create-basis {:project "deps.edn"}))

(defn uber
  "Compile Clojure sources and produce a self-contained uberjar.

  Invariants
  ----------
  - Output is always `target/app.jar`
  - Main entry point is `borba.runtime.main`
  - APP_VERSION is embedded into the MANIFEST.MF under Implementation-Version
  "
  [_]
  (clean nil)
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis     basis
                  :src-dirs  ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis     basis
           :main      'borba.runtime.main
           :manifest  {"Implementation-Title"   (name lib)
                       "Implementation-Version" version}}))
