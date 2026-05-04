package evidence

import (
	"bytes"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

func TestRegisterHashEndpoint(t *testing.T) {
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/hash",
		strings.NewReader(`{"evidenceId":"evidence-1","hash":"sha256:root","timestamp":"2026-05-03T12:00:00Z","metadata":{"appVersion":"0.1.0","captureMode":"Witness","mediaType":"Video","device":{"manufacturer":"Google","model":"Pixel","androidVersion":"14","fingerprint":"fingerprint"},"location":null,"timeSource":"Device"}}`),
	)

	mux := http.NewServeMux()
	RegisterHandlers(mux, newTestStore(t))
	mux.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusAccepted {
		t.Fatalf("expected status %d, got %d", http.StatusAccepted, recorder.Code)
	}
	if !strings.Contains(recorder.Body.String(), `"accepted":true`) {
		t.Fatalf("expected accepted response, got %s", recorder.Body.String())
	}
}

func TestUploadChunkRejectsHashMismatch(t *testing.T) {
	recorder := httptest.NewRecorder()
	request := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/chunks/0",
		strings.NewReader("payload"),
	)
	request.Header.Set("X-Chunk-Hash", "sha256:not-the-hash")

	mux := http.NewServeMux()
	RegisterHandlers(mux, newTestStore(t))
	mux.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusBadRequest {
		t.Fatalf("expected status %d, got %d", http.StatusBadRequest, recorder.Code)
	}
}

func TestUploadChunkAndVerifyEndpoint(t *testing.T) {
	store := newTestStore(t)
	mux := http.NewServeMux()
	RegisterHandlers(mux, store)
	payload := []byte("encrypted chunk")

	registerRequest := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/hash",
		strings.NewReader(`{"evidenceId":"evidence-1","hash":"sha256:root","timestamp":"2026-05-03T12:00:00Z","metadata":{"appVersion":"0.1.0","captureMode":"Witness","mediaType":"Video","device":{"manufacturer":"Google","model":"Pixel","androidVersion":"14","fingerprint":"fingerprint"},"location":null,"timeSource":"Device"}}`),
	)
	mux.ServeHTTP(httptest.NewRecorder(), registerRequest)

	uploadRecorder := httptest.NewRecorder()
	uploadRequest := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/chunks/0",
		bytes.NewReader(payload),
	)
	uploadRequest.Header.Set("X-Chunk-Hash", "sha256:"+testSHA256(payload))
	mux.ServeHTTP(uploadRecorder, uploadRequest)

	if uploadRecorder.Code != http.StatusAccepted {
		t.Fatalf("expected status %d, got %d", http.StatusAccepted, uploadRecorder.Code)
	}

	verifyRecorder := httptest.NewRecorder()
	verifyRequest := httptest.NewRequest(http.MethodGet, "/api/v1/evidence/evidence-1/verify", nil)
	mux.ServeHTTP(verifyRecorder, verifyRequest)

	body := verifyRecorder.Body.String()
	if verifyRecorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, verifyRecorder.Code)
	}
	if !strings.Contains(body, `"chunkIndex":0`) {
		t.Fatalf("expected uploaded chunk in verify response, got %s", body)
	}
	if !strings.Contains(body, `"verificationState":"verified"`) ||
		!strings.Contains(body, `"uploadStatus":"received"`) ||
		!strings.Contains(body, `"encryptedBytesStored":true`) {
		t.Fatalf("expected persisted verification and upload status in verify response, got %s", body)
	}
}

func TestVerifyEndpointSurvivesStoreRestart(t *testing.T) {
	dataDir := t.TempDir()
	payload := []byte("encrypted chunk")

	store, err := NewStoreAt(dataDir)
	if err != nil {
		t.Fatalf("create store: %v", err)
	}
	mux := http.NewServeMux()
	RegisterHandlers(mux, store)

	registerRequest := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/hash",
		strings.NewReader(`{"evidenceId":"evidence-1","hash":"sha256:root","timestamp":"2026-05-03T12:00:00Z","metadata":{"appVersion":"0.1.0","captureMode":"Witness","mediaType":"Video","device":{"manufacturer":"Google","model":"Pixel","androidVersion":"14","fingerprint":"fingerprint"},"location":null,"timeSource":"Device"}}`),
	)
	mux.ServeHTTP(httptest.NewRecorder(), registerRequest)

	uploadRequest := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/chunks/0",
		bytes.NewReader(payload),
	)
	uploadRequest.Header.Set("X-Chunk-Hash", "sha256:"+testSHA256(payload))
	mux.ServeHTTP(httptest.NewRecorder(), uploadRequest)
	chunkPath := filepath.Join(dataDir, "chunks", testSHA256([]byte("evidence-1")), "000000.bin")
	if _, err := os.Stat(chunkPath); err != nil {
		t.Fatalf("expected chunk file at %s: %v", chunkPath, err)
	}

	if err := store.Close(); err != nil {
		t.Fatalf("close store: %v", err)
	}

	reopened, err := NewStoreAt(dataDir)
	if err != nil {
		t.Fatalf("reopen store: %v", err)
	}
	t.Cleanup(func() {
		if err := reopened.Close(); err != nil {
			t.Fatalf("close reopened store: %v", err)
		}
	})

	verifyMux := http.NewServeMux()
	RegisterHandlers(verifyMux, reopened)
	verifyRecorder := httptest.NewRecorder()
	verifyRequest := httptest.NewRequest(http.MethodGet, "/api/v1/evidence/evidence-1/verify", nil)
	verifyMux.ServeHTTP(verifyRecorder, verifyRequest)

	body := verifyRecorder.Body.String()
	if verifyRecorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, verifyRecorder.Code)
	}
	if !strings.Contains(body, `"hash":"sha256:root"`) || !strings.Contains(body, `"chunkIndex":0`) {
		t.Fatalf("expected persisted evidence and chunk in verify response, got %s", body)
	}
	if !strings.Contains(body, `"encryptedBytesStored":true`) {
		t.Fatalf("expected persisted encrypted chunk file in verify response, got %s", body)
	}
}

func TestHashRegistrationAfterChunkPreservesReceivedUploadStatus(t *testing.T) {
	store := newTestStore(t)
	mux := http.NewServeMux()
	RegisterHandlers(mux, store)
	payload := []byte("encrypted chunk")

	uploadRequest := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/chunks/0",
		bytes.NewReader(payload),
	)
	uploadRequest.Header.Set("X-Chunk-Hash", "sha256:"+testSHA256(payload))
	mux.ServeHTTP(httptest.NewRecorder(), uploadRequest)

	registerRequest := httptest.NewRequest(
		http.MethodPost,
		"/api/v1/evidence/evidence-1/hash",
		strings.NewReader(`{"evidenceId":"evidence-1","hash":"sha256:root","timestamp":"2026-05-03T12:00:00Z","metadata":{"appVersion":"0.1.0","captureMode":"Witness","mediaType":"Video","device":{"manufacturer":"Google","model":"Pixel","androidVersion":"14","fingerprint":"fingerprint"},"location":null,"timeSource":"Device"}}`),
	)
	mux.ServeHTTP(httptest.NewRecorder(), registerRequest)

	verifyRecorder := httptest.NewRecorder()
	verifyRequest := httptest.NewRequest(http.MethodGet, "/api/v1/evidence/evidence-1/verify", nil)
	mux.ServeHTTP(verifyRecorder, verifyRequest)

	if verifyRecorder.Code != http.StatusOK {
		t.Fatalf("expected status %d, got %d", http.StatusOK, verifyRecorder.Code)
	}
	var record Record
	if err := json.NewDecoder(verifyRecorder.Body).Decode(&record); err != nil {
		t.Fatalf("decode verify response: %v", err)
	}
	if record.UploadStatus != evidenceStatusChunkReceived {
		t.Fatalf("expected evidence upload status %q, got %q", evidenceStatusChunkReceived, record.UploadStatus)
	}
	if record.VerificationState != verificationStateVerified {
		t.Fatalf("expected verification state %q, got %q", verificationStateVerified, record.VerificationState)
	}
	if !record.Chunks[0].EncryptedBytesStored {
		t.Fatalf("expected chunk bytes to be stored")
	}
}

func testSHA256(payload []byte) string {
	sum := sha256.Sum256(payload)
	return hex.EncodeToString(sum[:])
}

func newTestStore(t *testing.T) *Store {
	t.Helper()
	store, err := NewStoreAt(t.TempDir())
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
