package evidence

import (
	"crypto/sha256"
	"database/sql"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"time"

	_ "modernc.org/sqlite"
)

const maxChunkBytes = 6 * 1024 * 1024

const (
	evidenceStatusHashReceived  = "hash_received"
	evidenceStatusChunkReceived = "chunk_received"
	chunkStatusReceived         = "received"
	verificationStatePending    = "pending"
	verificationStateVerified   = "verified"
)

type Store struct {
	db       *sql.DB
	dataDir  string
	chunkDir string
}

type Record struct {
	EvidenceID        string                      `json:"evidenceId"`
	Hash              string                      `json:"hash"`
	HashReceivedAt    string                      `json:"hashReceivedAt"`
	Metadata          MetadataPayload             `json:"metadata"`
	Chunks            map[int]UploadedChunkRecord `json:"chunks"`
	Verified          bool                        `json:"verified"`
	UploadStatus      string                      `json:"uploadStatus"`
	VerificationState string                      `json:"verificationState"`
}

type UploadedChunkRecord struct {
	ChunkIndex           int    `json:"chunkIndex"`
	ChunkHash            string `json:"chunkHash"`
	ReceivedAt           string `json:"receivedAt"`
	SizeBytes            int64  `json:"sizeBytes"`
	UploadStatus         string `json:"uploadStatus"`
	EncryptedBytesStored bool   `json:"encryptedBytesStored"`
}

func NewStore() *Store {
	store, err := NewStoreAt(filepath.Join(os.TempDir(), "witness-backend-test-data"))
	if err != nil {
		panic(err)
	}
	return store
}

func NewStoreAt(dataDir string) (*Store, error) {
	if dataDir == "" {
		dataDir = "./data"
	}

	chunkDir := filepath.Join(dataDir, "chunks")
	if err := os.MkdirAll(chunkDir, 0o700); err != nil {
		return nil, fmt.Errorf("create data directories: %w", err)
	}

	db, err := sql.Open("sqlite", filepath.Join(dataDir, "witness.db"))
	if err != nil {
		return nil, fmt.Errorf("open sqlite database: %w", err)
	}

	store := &Store{
		db:       db,
		dataDir:  dataDir,
		chunkDir: chunkDir,
	}
	if err := store.migrate(); err != nil {
		_ = db.Close()
		return nil, err
	}
	return store, nil
}

func (s *Store) Close() error {
	return s.db.Close()
}

func (s *Store) StorageWritable() bool {
	if err := s.db.Ping(); err != nil {
		return false
	}
	checkedAt := time.Now().UTC().Format(time.RFC3339Nano)
	if _, err := s.db.Exec(
		`INSERT INTO health_checks (checked_at) VALUES (?)`,
		checkedAt,
	); err != nil {
		return false
	}
	_, _ = s.db.Exec(`DELETE FROM health_checks WHERE checked_at = ?`, checkedAt)

	testFile, err := os.CreateTemp(s.chunkDir, ".writable-*")
	if err != nil {
		return false
	}
	name := testFile.Name()
	if err := testFile.Close(); err != nil {
		_ = os.Remove(name)
		return false
	}
	return os.Remove(name) == nil
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

	metadataJSON, err := json.Marshal(request.Metadata)
	if err != nil {
		writeError(w, http.StatusBadRequest, "invalid metadata")
		return
	}

	receivedAt := time.Now().UTC().Format(time.RFC3339Nano)
	_, err = s.db.Exec(
		`INSERT INTO evidence (
			evidence_id,
			hash,
			hash_received_at,
			metadata_json,
			verified,
			upload_status,
			verification_state
		)
		 VALUES (?, ?, ?, ?, 1, ?, ?)
		 ON CONFLICT(evidence_id) DO UPDATE SET
			hash = excluded.hash,
			hash_received_at = excluded.hash_received_at,
			metadata_json = excluded.metadata_json,
			verified = excluded.verified,
			upload_status = CASE
				WHEN evidence.upload_status = ? THEN evidence.upload_status
				ELSE excluded.upload_status
			END,
			verification_state = excluded.verification_state`,
		evidenceID,
		request.Hash,
		receivedAt,
		string(metadataJSON),
		evidenceStatusHashReceived,
		verificationStateVerified,
		evidenceStatusChunkReceived,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to store evidence hash")
		return
	}

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

	chunkPath, err := s.writeChunkFile(evidenceID, chunkIndex, body)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to store encrypted chunk")
		return
	}

	receivedAt := time.Now().UTC().Format(time.RFC3339Nano)
	tx, err := s.db.Begin()
	if err != nil {
		_ = os.Remove(chunkPath)
		writeError(w, http.StatusInternalServerError, "failed to start chunk metadata transaction")
		return
	}
	committed := false
	defer func() {
		if !committed {
			_ = tx.Rollback()
			_ = os.Remove(chunkPath)
		}
	}()

	_, err = tx.Exec(
		`INSERT INTO evidence (evidence_id, metadata_json, upload_status, verification_state)
		 VALUES (?, '{}', ?, ?)
		 ON CONFLICT(evidence_id) DO NOTHING`,
		evidenceID,
		evidenceStatusChunkReceived,
		verificationStatePending,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to initialize evidence record")
		return
	}

	_, err = tx.Exec(
		`UPDATE evidence
		 SET upload_status = ?
		 WHERE evidence_id = ?`,
		evidenceStatusChunkReceived,
		evidenceID,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to update evidence status")
		return
	}

	_, err = tx.Exec(
		`INSERT INTO chunks (evidence_id, chunk_index, chunk_hash, received_at, size_bytes, file_path, upload_status)
		 VALUES (?, ?, ?, ?, ?, ?, ?)
		 ON CONFLICT(evidence_id, chunk_index) DO UPDATE SET
			chunk_hash = excluded.chunk_hash,
			received_at = excluded.received_at,
			size_bytes = excluded.size_bytes,
			file_path = excluded.file_path,
			upload_status = excluded.upload_status`,
		evidenceID,
		chunkIndex,
		actualHash,
		receivedAt,
		len(body),
		chunkPath,
		chunkStatusReceived,
	)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to store chunk metadata")
		return
	}
	if err := tx.Commit(); err != nil {
		writeError(w, http.StatusInternalServerError, "failed to commit chunk metadata")
		return
	}
	committed = true

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
	record, err := s.loadRecord(evidenceID)
	if errors.Is(err, sql.ErrNoRows) {
		writeError(w, http.StatusNotFound, "evidence not found")
		return
	}
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to load evidence")
		return
	}

	writeJSON(w, http.StatusOK, record)
}

func (s *Store) migrate() error {
	_, err := s.db.Exec(`
		CREATE TABLE IF NOT EXISTS evidence (
			evidence_id TEXT PRIMARY KEY,
			hash TEXT NOT NULL DEFAULT '',
			hash_received_at TEXT NOT NULL DEFAULT '',
			metadata_json TEXT NOT NULL DEFAULT '{}',
			verified INTEGER NOT NULL DEFAULT 0,
			upload_status TEXT NOT NULL DEFAULT 'pending',
			verification_state TEXT NOT NULL DEFAULT 'pending'
		);
		CREATE TABLE IF NOT EXISTS chunks (
			evidence_id TEXT NOT NULL,
			chunk_index INTEGER NOT NULL,
			chunk_hash TEXT NOT NULL,
			received_at TEXT NOT NULL,
			size_bytes INTEGER NOT NULL,
			file_path TEXT NOT NULL,
			upload_status TEXT NOT NULL DEFAULT 'received',
			PRIMARY KEY (evidence_id, chunk_index)
		);
		CREATE TABLE IF NOT EXISTS health_checks (
			checked_at TEXT NOT NULL
		);
	`)
	if err != nil {
		return fmt.Errorf("migrate sqlite database: %w", err)
	}
	if err := s.addColumnIfMissing("evidence", "upload_status", "TEXT NOT NULL DEFAULT 'pending'"); err != nil {
		return err
	}
	if err := s.addColumnIfMissing("evidence", "verification_state", "TEXT NOT NULL DEFAULT 'pending'"); err != nil {
		return err
	}
	if err := s.addColumnIfMissing("chunks", "upload_status", "TEXT NOT NULL DEFAULT 'received'"); err != nil {
		return err
	}
	return nil
}

func (s *Store) addColumnIfMissing(table string, column string, definition string) error {
	exists, err := s.columnExists(table, column)
	if err != nil {
		return err
	}
	if exists {
		return nil
	}
	if _, err := s.db.Exec(fmt.Sprintf("ALTER TABLE %s ADD COLUMN %s %s", table, column, definition)); err != nil {
		return fmt.Errorf("add %s.%s column: %w", table, column, err)
	}
	return nil
}

func (s *Store) columnExists(table string, column string) (bool, error) {
	rows, err := s.db.Query(fmt.Sprintf("PRAGMA table_info(%s)", table))
	if err != nil {
		return false, fmt.Errorf("read %s schema: %w", table, err)
	}
	defer rows.Close()

	for rows.Next() {
		var cid int
		var name string
		var dataType string
		var notNull int
		var defaultValue sql.NullString
		var primaryKey int
		if err := rows.Scan(&cid, &name, &dataType, &notNull, &defaultValue, &primaryKey); err != nil {
			return false, fmt.Errorf("scan %s schema: %w", table, err)
		}
		if name == column {
			return true, nil
		}
	}
	if err := rows.Err(); err != nil {
		return false, fmt.Errorf("iterate %s schema: %w", table, err)
	}
	return false, nil
}

func (s *Store) loadRecord(evidenceID string) (Record, error) {
	var record Record
	var metadataJSON string
	var verified int
	err := s.db.QueryRow(
		`SELECT evidence_id, hash, hash_received_at, metadata_json, verified, upload_status, verification_state
		 FROM evidence
		 WHERE evidence_id = ?`,
		evidenceID,
	).Scan(
		&record.EvidenceID,
		&record.Hash,
		&record.HashReceivedAt,
		&metadataJSON,
		&verified,
		&record.UploadStatus,
		&record.VerificationState,
	)
	if err != nil {
		return Record{}, err
	}

	if err := json.Unmarshal([]byte(metadataJSON), &record.Metadata); err != nil {
		return Record{}, fmt.Errorf("decode metadata: %w", err)
	}
	record.Verified = verified == 1
	record.Chunks = map[int]UploadedChunkRecord{}

	rows, err := s.db.Query(
		`SELECT chunk_index, chunk_hash, received_at, size_bytes, upload_status, file_path
		 FROM chunks
		 WHERE evidence_id = ?
		 ORDER BY chunk_index`,
		evidenceID,
	)
	if err != nil {
		return Record{}, err
	}
	defer rows.Close()

	for rows.Next() {
		var chunk UploadedChunkRecord
		var filePath string
		if err := rows.Scan(
			&chunk.ChunkIndex,
			&chunk.ChunkHash,
			&chunk.ReceivedAt,
			&chunk.SizeBytes,
			&chunk.UploadStatus,
			&filePath,
		); err != nil {
			return Record{}, err
		}
		chunk.EncryptedBytesStored = fileExists(filePath)
		record.Chunks[chunk.ChunkIndex] = chunk
	}
	if err := rows.Err(); err != nil {
		return Record{}, err
	}
	return record, nil
}

func (s *Store) writeChunkFile(evidenceID string, chunkIndex int, body []byte) (string, error) {
	evidenceDir := filepath.Join(s.chunkDir, sha256Hex([]byte(evidenceID)))
	if err := os.MkdirAll(evidenceDir, 0o700); err != nil {
		return "", err
	}

	finalPath := filepath.Join(evidenceDir, fmt.Sprintf("%06d.bin", chunkIndex))
	tempFile, err := os.CreateTemp(evidenceDir, fmt.Sprintf("%06d-*.tmp", chunkIndex))
	if err != nil {
		return "", err
	}
	tempPath := tempFile.Name()

	if _, err := tempFile.Write(body); err != nil {
		_ = tempFile.Close()
		_ = os.Remove(tempPath)
		return "", err
	}
	if err := tempFile.Sync(); err != nil {
		_ = tempFile.Close()
		_ = os.Remove(tempPath)
		return "", err
	}
	if err := tempFile.Close(); err != nil {
		_ = os.Remove(tempPath)
		return "", err
	}
	if err := os.Rename(tempPath, finalPath); err != nil {
		_ = os.Remove(tempPath)
		return "", err
	}
	return finalPath, nil
}

func fileExists(path string) bool {
	info, err := os.Stat(path)
	return err == nil && !info.IsDir()
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
