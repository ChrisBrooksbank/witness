package evidence

import (
	"encoding/json"
	"testing"
)

func TestRegisterHashRequestJSONContract(t *testing.T) {
	payload := RegisterHashRequest{
		EvidenceID: "evidence-1",
		Hash:       "sha256:abc",
		Timestamp:  "2026-05-03T12:00:00Z",
		Metadata: MetadataPayload{
			AppVersion:  "0.1.0",
			CaptureMode: "Witness",
			MediaType:   "Video",
			Device: DevicePayload{
				Manufacturer:   "Google",
				Model:          "Pixel",
				AndroidVersion: "14",
				Fingerprint:    "fingerprint",
			},
			TimeSource: "Network",
		},
	}

	encoded, err := json.Marshal(payload)
	if err != nil {
		t.Fatalf("marshal register hash request: %v", err)
	}

	var decoded RegisterHashRequest
	if err := json.Unmarshal(encoded, &decoded); err != nil {
		t.Fatalf("unmarshal register hash request: %v", err)
	}

	if decoded.EvidenceID != payload.EvidenceID {
		t.Fatalf("expected evidence id %q, got %q", payload.EvidenceID, decoded.EvidenceID)
	}
}
