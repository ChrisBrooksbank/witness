package evidence

import (
	"bytes"
	"crypto/sha256"
	"encoding/hex"
	"net/http"
	"net/http/httptest"
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
	RegisterHandlers(mux, NewStore())
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
	RegisterHandlers(mux, NewStore())
	mux.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusBadRequest {
		t.Fatalf("expected status %d, got %d", http.StatusBadRequest, recorder.Code)
	}
}

func TestUploadChunkAndVerifyEndpoint(t *testing.T) {
	store := NewStore()
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
}

func testSHA256(payload []byte) string {
	sum := sha256.Sum256(payload)
	return hex.EncodeToString(sum[:])
}
