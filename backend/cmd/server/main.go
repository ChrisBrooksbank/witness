package main

import (
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/witness-org/witness/backend/internal/evidence"
	"github.com/witness-org/witness/backend/internal/health"
)

const defaultAddr = ":8080"
const defaultDataDir = "./data"
const shutdownTimeout = 10 * time.Second

func main() {
	addr := os.Getenv("WITNESS_ADDR")
	if addr == "" {
		addr = defaultAddr
	}
	dataDir := os.Getenv("WITNESS_DATA_DIR")
	if dataDir == "" {
		dataDir = defaultDataDir
	}

	store, err := evidence.NewStoreAt(dataDir)
	if err != nil {
		slog.Error("failed to initialize storage", "dataDir", dataDir, "error", err)
		os.Exit(1)
	}
	defer func() {
		if err := store.Close(); err != nil {
			slog.Error("failed to close storage", "error", err)
		}
	}()

	server := &http.Server{
		Addr:    addr,
		Handler: health.NewHandlerWithStore(store),
	}

	slog.Info("starting witness node", "addr", addr, "dataDir", dataDir)

	serverErr := make(chan error, 1)
	go func() {
		serverErr <- server.ListenAndServe()
	}()

	shutdownSignals := make(chan os.Signal, 1)
	signal.Notify(shutdownSignals, os.Interrupt, syscall.SIGTERM)

	select {
	case err := <-serverErr:
		if err != nil && !errors.Is(err, http.ErrServerClosed) {
			slog.Error("server stopped unexpectedly", "error", err)
			os.Exit(1)
		}
	case signal := <-shutdownSignals:
		slog.Info("stopping witness node", "signal", signal.String())
		ctx, cancel := context.WithTimeout(context.Background(), shutdownTimeout)
		defer cancel()
		if err := server.Shutdown(ctx); err != nil {
			slog.Error("failed to stop server cleanly", "error", err)
			os.Exit(1)
		}
		if err := <-serverErr; err != nil && !errors.Is(err, http.ErrServerClosed) {
			slog.Error("server stopped unexpectedly", "error", err)
			os.Exit(1)
		}
		slog.Info("witness node stopped")
	}
}
