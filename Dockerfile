# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Resolves dependencies and compiles the uberjar.
# Uses the official Clojure image to avoid installing the toolchain manually.
FROM clojure:tools-deps-alpine AS builder

ARG APP_VERSION=dev

WORKDIR /app

# Copy only the dependency manifest first.
# This layer is cached independently and only invalidated when deps.edn changes,
# which avoids re-downloading the entire dependency tree on every source change.
#
# `clojure -P` resolves all app deps (main + aliases).
# `clojure -P -T:build` resolves the tools.build git dep separately.
# Neither command requires build.clj to be present yet.
COPY deps.edn .
RUN clojure -P && clojure -P -T:build

# Copy source and compile. This layer is invalidated on any source change.
COPY . .
RUN APP_VERSION=${APP_VERSION} clojure -T:build uber


# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Minimal JRE image — no build tooling, no Clojure CLI, no source code.
# Final image is typically ~200 MB vs ~600 MB for a full JDK image.
FROM eclipse-temurin:21-jre-alpine AS runtime

# Build-time arguments control the runtime environment defaults.
# They can be overridden at `docker build` time or via environment variables
# at `docker run` time (ENV values become the fallback defaults).
ARG APP_VERSION=dev
ARG PORT=8080
ARG PROFILE=prod

ENV APP_VERSION=${APP_VERSION}
ENV PORT=${PORT}
ENV PROFILE=${PROFILE}

# Non-root user for security hardening.
RUN addgroup -S app && adduser -S app -G app
USER app

WORKDIR /app

COPY --from=builder /app/target/app.jar app.jar

EXPOSE ${PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
