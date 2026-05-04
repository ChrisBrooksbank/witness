package health

import (
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/witness-org/witness/backend/internal/evidence"
)

func TestHealthEndpoint(t *testing.T) {
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(http.MethodGet, "/health", nil)
	store := newTestStore(t)

	NewHandlerWithStore(store).ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, recorder.Code)
	}

	body := recorder.Body.String()
	if !strings.Contains(body, `"status":"ok"`) {
		t.Fatalf("expected ok health response, got %s", body)
	}
}

func TestVersionEndpoint(t *testing.T) {
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(http.MethodGet, "/api/v1/version", nil)
	store := newTestStore(t)

	NewHandlerWithStore(store).ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, recorder.Code)
	}

	body := recorder.Body.String()
	if !strings.Contains(body, `"version":"0.1.0"`) {
		t.Fatalf("expected version response, got %s", body)
	}
}

func newTestStore(t *testing.T) *evidence.Store {
	t.Helper()
	store, err := evidence.NewStoreAt(t.TempDir())
	if err != nil {
		t.Fatalf("create test store: %v", err)
	}
	t.Cleanup(func() {
		if err := store.Close(); err != nil {
			t.Fatalf("close test store: %v", err)
		}
	})
	return store
}
