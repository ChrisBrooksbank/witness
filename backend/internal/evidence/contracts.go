package evidence

type RegisterHashRequest struct {
	EvidenceID string          `json:"evidenceId"`
	Hash       string          `json:"hash"`
	Timestamp  string          `json:"timestamp"`
	Metadata   MetadataPayload `json:"metadata"`
}

type MetadataPayload struct {
	AppVersion  string           `json:"appVersion"`
	CaptureMode string           `json:"captureMode"`
	MediaType   string           `json:"mediaType"`
	Device      DevicePayload    `json:"device"`
	Location    *LocationPayload `json:"location"`
	TimeSource  string           `json:"timeSource"`
}

type DevicePayload struct {
	Manufacturer   string `json:"manufacturer"`
	Model          string `json:"model"`
	AndroidVersion string `json:"androidVersion"`
	Fingerprint    string `json:"fingerprint"`
}

type LocationPayload struct {
	Latitude       float64  `json:"latitude"`
	Longitude      float64  `json:"longitude"`
	Altitude       *float64 `json:"altitude"`
	AccuracyMeters float32  `json:"accuracyMeters"`
	Provider       string   `json:"provider"`
}

type RegisterHashResponse struct {
	EvidenceID     string `json:"evidenceId"`
	HashReceivedAt string `json:"hashReceivedAt"`
	Accepted       bool   `json:"accepted"`
}

type UploadChunkResponse struct {
	EvidenceID string `json:"evidenceId"`
	ChunkIndex int    `json:"chunkIndex"`
	ChunkHash  string `json:"chunkHash"`
	ReceivedAt string `json:"receivedAt"`
	Accepted   bool   `json:"accepted"`
}
