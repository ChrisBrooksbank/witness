package main

import (
	"errors"
	"log/slog"
	"net/http"
	"os"

	"github.com/witness-org/witness/backend/internal/health"
)

const defaultAddr = ":8080"

func main() {
	addr := os.Getenv("WITNESS_ADDR")
	if addr == "" {
		addr = defaultAddr
	}

	server := &http.Server{
		Addr:    addr,
		Handler: health.NewHandler(),
	}

	slog.Info("starting witness node", "addr", addr)
	if err := server.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		slog.Error("server stopped unexpectedly", "error", err)
		os.Exit(1)
	}
}
