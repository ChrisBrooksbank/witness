package health

import (
	"encoding/json"
	"net/http"
	"os"

	"github.com/witness-org/witness/backend/internal/evidence"
)

const version = "0.1.0"

type response struct {
	Status          string `json:"status"`
	Version         string `json:"version,omitempty"`
	StorageWritable *bool  `json:"storageWritable,omitempty"`
}

func NewHandler() http.Handler {
	dataDir := os.Getenv("WITNESS_DATA_DIR")
	store, err := evidence.NewStoreAt(dataDir)
	if err != nil {
		panic(err)
	}
	return NewHandlerWithStore(store)
}

func NewHandlerWithStore(store *evidence.Store) http.Handler {
	mux := http.NewServeMux()
	mux.HandleFunc("GET /health", handleHealth(store))
	mux.HandleFunc("GET /api/v1/version", handleVersion)
	evidence.RegisterHandlers(mux, store)
	return mux
}

func handleHealth(store *evidence.Store) http.HandlerFunc {
	return func(w http.ResponseWriter, _ *http.Request) {
		storageWritable := store.StorageWritable()
		if !storageWritable {
			writeJSON(w, http.StatusServiceUnavailable, response{
				Status:          "error",
				StorageWritable: &storageWritable,
			})
			return
		}
		writeJSON(w, http.StatusOK, response{Status: "ok"})
	}
}

func handleVersion(w http.ResponseWriter, _ *http.Request) {
	writeJSON(w, http.StatusOK, response{Status: "ok", Version: version})
}

func writeJSON(w http.ResponseWriter, statusCode int, payload response) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	if err := json.NewEncoder(w).Encode(payload); err != nil {
		http.Error(w, "failed to encode response", http.StatusInternalServerError)
	}
}
