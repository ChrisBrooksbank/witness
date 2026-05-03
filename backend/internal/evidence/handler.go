package evidence

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"io"
	"net/http"
	"strconv"
	"sync"
	"time"
)

const maxChunkBytes = 6 * 1024 * 1024

type Store struct {
	mu       sync.RWMutex
	evidence map[string]*Record
}

type Record struct {
	EvidenceID     string                      `json:"evidenceId"`
	Hash           string                      `json:"hash"`
	HashReceivedAt string                      `json:"hashReceivedAt"`
	Metadata       MetadataPayload             `json:"metadata"`
	Chunks         map[int]UploadedChunkRecord `json:"chunks"`
	Verified       bool                        `json:"verified"`
}

type UploadedChunkRecord struct {
	ChunkIndex int    `json:"chunkIndex"`
	ChunkHash  string `json:"chunkHash"`
	ReceivedAt string `json:"receivedAt"`
	SizeBytes  int64  `json:"sizeBytes"`
}

func NewStore() *Store {
	return &Store{evidence: map[string]*Record{}}
}

func RegisterHandlers(mux *http.ServeMux, store *Store) {
	mux.HandleFunc("POST /api/v1/evidence/{evidenceId}/hash", store.handleRegisterHash)
	mux.HandleFunc("POST /api/v1/evidence/{evidenceId}/chunks/{chunkIndex}", store.handleUploadChunk)
	mux.HandleFunc("GET /api/v1/evidence/{evidenceId}/verify", store.handleVerifyEvidence)
}

func (s *Store) handleRegisterHash(w http.ResponseWriter, r *http.Request) {
	evidenceID := r.PathValue("evidenceId")
	var request RegisterHashRequest
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		writeError(w, http.StatusBadRequest, "invalid JSON body")
		return
	}
	if evidenceID == "" || request.EvidenceID != evidenceID || request.Hash == "" {
		writeError(w, http.StatusBadRequest, "evidence ID and hash are required")
		return
	}

	receivedAt := time.Now().UTC().Format(time.RFC3339Nano)
	s.mu.Lock()
	s.evidence[evidenceID] = &Record{
		EvidenceID:     evidenceID,
		Hash:           request.Hash,
		HashReceivedAt: receivedAt,
		Metadata:       request.Metadata,
		Chunks:         map[int]UploadedChunkRecord{},
		Verified:       true,
	}
	s.mu.Unlock()

	writeJSON(w, http.StatusAccepted, RegisterHashResponse{
		EvidenceID:     evidenceID,
		HashReceivedAt: receivedAt,
		Accepted:       true,
	})
}

func (s *Store) handleUploadChunk(w http.ResponseWriter, r *http.Request) {
	evidenceID := r.PathValue("evidenceId")
	chunkIndex, err := strconv.Atoi(r.PathValue("chunkIndex"))
	if err != nil || chunkIndex < 0 {
		writeError(w, http.StatusBadRequest, "chunk index must be a non-negative integer")
		return
	}

	expectedHash := r.Header.Get("X-Chunk-Hash")
	if expectedHash == "" {
		writeError(w, http.StatusBadRequest, "X-Chunk-Hash header is required")
		return
	}

	body, err := io.ReadAll(http.MaxBytesReader(w, r.Body, maxChunkBytes))
	if err != nil {
		writeError(w, http.StatusRequestEntityTooLarge, "chunk exceeds maximum size")
		return
	}

	actualHash := "sha256:" + sha256Hex(body)
	if actualHash != expectedHash {
		writeError(w, http.StatusBadRequest, "chunk hash mismatch")
		return
	}

	receivedAt := time.Now().UTC().Format(time.RFC3339Nano)
	record := UploadedChunkRecord{
		ChunkIndex: chunkIndex,
		ChunkHash:  actualHash,
		ReceivedAt: receivedAt,
		SizeBytes:  int64(len(body)),
	}

	s.mu.Lock()
	evidenceRecord, ok := s.evidence[evidenceID]
	if !ok {
		evidenceRecord = &Record{
			EvidenceID: evidenceID,
			Chunks:     map[int]UploadedChunkRecord{},
		}
		s.evidence[evidenceID] = evidenceRecord
	}
	evidenceRecord.Chunks[chunkIndex] = record
	s.mu.Unlock()

	writeJSON(w, http.StatusAccepted, UploadChunkResponse{
		EvidenceID: evidenceID,
		ChunkIndex: chunkIndex,
		ChunkHash:  actualHash,
		ReceivedAt: receivedAt,
		Accepted:   true,
	})
}

func (s *Store) handleVerifyEvidence(w http.ResponseWriter, r *http.Request) {
	evidenceID := r.PathValue("evidenceId")
	s.mu.RLock()
	record, ok := s.evidence[evidenceID]
	s.mu.RUnlock()
	if !ok {
		writeError(w, http.StatusNotFound, "evidence not found")
		return
	}

	writeJSON(w, http.StatusOK, record)
}

func sha256Hex(payload []byte) string {
	sum := sha256.Sum256(payload)
	return hex.EncodeToString(sum[:])
}

func writeJSON(w http.ResponseWriter, statusCode int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	if err := json.NewEncoder(w).Encode(payload); err != nil {
		http.Error(w, "failed to encode response", http.StatusInternalServerError)
	}
}

func writeError(w http.ResponseWriter, statusCode int, message string) {
	writeJSON(w, statusCode, map[string]string{"error": message})
}
