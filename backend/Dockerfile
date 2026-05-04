FROM golang:1.25-bookworm AS build

WORKDIR /src/backend
COPY backend/go.mod backend/go.sum ./
RUN go mod download

COPY backend ./
RUN CGO_ENABLED=0 go build -trimpath -ldflags="-s -w" -o /out/witness-node ./cmd/server

FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --home-dir /nonexistent witness \
    && mkdir -p /data \
    && chown witness:witness /data

USER witness
WORKDIR /app
COPY --from=build /out/witness-node /app/witness-node

ENV WITNESS_ADDR=:8080
ENV WITNESS_DATA_DIR=/data
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD ["curl", "--fail", "http://127.0.0.1:8080/health"]

CMD ["/app/witness-node"]
