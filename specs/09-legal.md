# Legal Considerations

## Overview

Witness operates in a complex legal environment. Recording laws vary by jurisdiction, and the app must inform users without providing legal advice. The project structure must also protect contributors and node operators.

## Requirements

### User Information

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| LEG-001 | Inform users recording laws vary by jurisdiction | Must | Yes |
| LEG-002 | In-app resources or links to legal guidance | Should | Yes |
| LEG-003 | Clear disclaimer: not legal advice | Must | Yes |
| LEG-004 | Users responsible for understanding local laws | Must | Yes |
| LEG-005 | Know Your Rights resources (US) | Should | Yes |

### Terms of Service

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| LEG-010 | Clear data handling policies | Must | Yes |
| LEG-011 | No warranties about legal protection | Must | Yes |
| LEG-012 | Acceptable use policy | Must | Yes |
| LEG-013 | User acknowledges risks | Must | Yes |
| LEG-014 | No liability for node operators for content | Must | Yes |

### Open Source License

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| LEG-020 | Copyleft license (GPL-3.0 or similar) | Must | Yes |
| LEG-021 | Require derivative works remain open | Must | Yes |
| LEG-022 | Clear contribution guidelines | Should | Yes |
| LEG-023 | CLA for contributors (optional) | Could | No |

### Organizational Structure

| ID | Requirement | Priority | MVP |
|----|-------------|----------|-----|
| LEG-030 | Consider fiscal sponsor or nonprofit | Should | No |
| LEG-031 | Legal review of federation model | Should | No |
| LEG-032 | DMCA/takedown policy for nodes | Should | Yes |
| LEG-033 | Node operator agreement template | Should | Yes |

## Acceptance Criteria

- [ ] AC-LEG-1: App displays legal disclaimer on first launch
- [ ] AC-LEG-2: Terms of service accessible from settings
- [ ] AC-LEG-3: Know Your Rights link available in app
- [ ] AC-LEG-4: LICENSE file present in repository (GPL-3.0)
- [ ] AC-LEG-5: Privacy policy accessible from app and website

## Technical Notes

### Legal Disclaimer

```kotlin
@Composable
fun LegalDisclaimerDialog(
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Cannot dismiss */ },
        title = { Text(stringResource(R.string.legal_disclaimer_title)) },
        text = {
            Text(stringResource(R.string.legal_disclaimer_body))
            // "Recording laws vary by jurisdiction. This app does not
            //  provide legal advice. You are responsible for understanding
            //  the laws in your area. By using this app, you acknowledge
            //  these risks and agree to the Terms of Service."
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text(stringResource(R.string.i_understand))
            }
        }
    )
}
```

### Recording Laws Summary (Informational)

```markdown
## One-Party Consent States (US)
Recording is legal if you are a party to the conversation.
Examples: New York, Texas, Florida

## Two-Party/All-Party Consent States (US)
All parties must consent to recording.
Examples: California, Washington, Illinois

## Public Space Exception
Generally, recording in public spaces where there is no
reasonable expectation of privacy is permitted.

## Note
This is general information, not legal advice. Consult
a lawyer for guidance specific to your situation.
```

### Privacy Policy Elements

- What data is collected (minimal: anonymous ID, evidence metadata)
- How data is stored (encrypted, federated)
- Who can access data (only authorized by user)
- Data retention (user-controlled)
- No selling of data
- No advertising tracking
- Contact information for questions

### Terms of Service Elements

- Acceptance of terms
- Permitted use
- Prohibited use (harassment, false evidence, etc.)
- Disclaimer of warranties
- Limitation of liability
- User responsibility for legal compliance
- Changes to terms
- Governing law

### Node Operator Agreement

Key provisions:

1. **Data handling**: Store encrypted data, cannot decrypt
2. **Availability**: Best-effort uptime commitment
3. **Takedown**: Process for legal takedown requests
4. **No content moderation**: Operators do not review content
5. **Replication**: Agreement to replicate to peer nodes
6. **Liability**: Limited liability shield
7. **Termination**: Process for leaving network

### DMCA Safe Harbor

For US-based nodes, consider DMCA safe harbor provisions:

1. Designate DMCA agent
2. Implement takedown procedure
3. Terminate repeat infringers
4. No knowledge of infringing material

## Open Questions

| Question | Status |
|----------|--------|
| Specific open source license? | Leaning GPL-3.0 |
| Fiscal sponsor organization? | Research needed |
| International legal review? | Post-MVP, for key markets |
| Node operator liability structure? | Legal review needed |
| Content moderation policy? | Minimal - user-controlled access |
