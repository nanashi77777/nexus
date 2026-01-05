# Backend API View Object (VO) Design

**Version:** 1.0
**Last Updated:** 2025-07-30

This document specifies the data transfer objects (VOs) that the backend should provide to the frontend. The design
prioritizes clarity, efficiency, and security by only exposing necessary data and combining related entities into
logical structures.

---

## General Principles

Core rules governing all API responses

#### 1. Response Wrapper

All API responses, without exception, will be wrapped in the `ResultVO<T>` object. This provides a consistent structure
with a `code`, `message`, and `data` field.

#### 2. Pagination Wrapper

For any API endpoint that returns a list of items, the `data` field within `ResultVO` will be further wrapped in a
`PageResult<T>` object. This provides standardized pagination details (`records`, `total`, `pageNum`, `pageSize`, etc.).

#### 3. Field Exclusion Policy

Sensitive or internal fields are never exposed. This includes all `*DeletedAt` fields, `uaPassword`, internal system
identifiers like `rcVectorId`, and raw JSON configuration strings unless explicitly required and structured.

---

## Module 1: User & Account Management

Handles user information.

### `UserVO`

Provides essential, non-sensitive information about a user. Can be used to display the current logged-in user's details
or an author's information.

- **Fused Entities**: `UserAccountEntity`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 123456789012345678,
  "username": "test-user",
  "email": "user@example.com",
  "phone": "18812345678",
  "inviteCode": "A1B2C3D4",
  "createdAt": "2025-07-30T10:00:00"
}
```

---

## Module 2: Learning Space Management

A Learning Space is the primary container for a user's content, acting as a workspace.

### `LearningSpaceSimpleVO`

A lightweight object for displaying a list of learning spaces (e.g., on a dashboard).

- **Fused Entities**: `LearningSpaceEntity`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 234567890123456789,
  "name": "My Quantum Physics Space",
  "description": "A collection of resources about quantum mechanics.",
  "coverImageUrl": "https://example.com/path/to/image.png",
  "updatedAt": "2025-07-30T11:30:00"
}
```

### `LearningSpaceDetailVO`

Provides the full details of a single learning space.

- **Fused Entities**: `LearningSpaceEntity`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 234567890123456789,
  "name": "My Quantum Physics Space",
  "description": "A collection of resources about quantum mechanics.",
  "coverImageUrl": "https://example.com/path/to/image.png",
  "spacePrompt": "When answering questions, assume a university-level understanding of physics.",
  "createdAt": "2025-07-29T09:00:00",
  "updatedAt": "2025-07-30T11:30:00"
}
```

---

## Module 3: Resource Management

Handles user-uploaded source materials like PDFs, documents, etc.

### `ResourceSimpleVO`

A lightweight object for listing resources within a learning space.

- **Fused Entities**: `ResourceEntity`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 345678901234567890,
  "title": "Feynman Lectures on Physics.pdf",
  "sourceType": 0,
  "status": 2,
  "createdAt": "2025-07-29T14:00:00"
}
```

**Notes on Fields:**

- `sourceType`: Enum Mapping: `0` = UPLOAD, `1` = LINK, `2` = AI_GENERATED
- `status`: Enum Mapping: `0` = PENDING_PARSE, `1` = PARSING, `2` = PARSE_COMPLETED, `3` = PARSE_FAILED

### `ResourceDetailVO`

Provides full details about a single resource, including error information if parsing failed.

- **Fused Entities**: `ResourceEntity`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 345678901234567890,
  "title": "Feynman Lectures on Physics.pdf",
  "description": "The complete Feynman Lectures on Physics, Volume 1.",
  "sourceType": 0,
  "sourceUri": "/file-storage/user_123/feynman.pdf",
  "prompt": "This document contains foundational concepts in classical and quantum mechanics.",
  "status": 2,
  "parseErrorMessage": null,
  "createdAt": "2025-07-29T14:00:00",
  "updatedAt": "2025-07-29T14:05:00"
}
```

**Notes on Fields:**

- `sourceType`: Enum Mapping: `0` = UPLOAD, `1` = LINK, `2` = AI_GENERATED
- `status`: Enum Mapping: `0` = PENDING_PARSE, `1` = PARSING, `2` = PARSE_COMPLETED, `3` = PARSE_FAILED

---

## Module 4: Knowledge Base

The core module for user-managed, structured knowledge.

### `KnowledgeFolderVO`

Represents a folder in the knowledge base tree structure.

- **Fused Entities**: `KnowledgeFolderEntity`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 456789012345678901,
  "parentId": 234567890123456789,
  "name": "Core Concepts",
  "level": 1
}
```

### `KnowledgePointSummaryVO`

A summary view of a knowledge point for display in lists or trees. It combines the main point with its *current active
version* to get the title.

- **Fused Entities**: `KnowledgePointEntity` (aliased as `kp`), `KnowledgePointVersionEntity` (aliased as `kpv`)
- **JOIN Conditions**: `kp.kpCurrentVersionId = kpv.kpvId`

**JSON Structure:**

```json
{
  "id": 567890123456789012,
  "folderId": 456789012345678901,
  "title": "Wave-Particle Duality",
  "updatedAt": "2025-07-30T15:00:00"
}
```

### `KnowledgePointDetailVO`

The complete view of a single knowledge point, showing all content from its current version.

- **Fused Entities**: `KnowledgePointEntity` (aliased as `kp`), `KnowledgePointVersionEntity` (aliased as `kpv`)
- **JOIN Conditions**: `kp.kpCurrentVersionId = kpv.kpvId`

**JSON Structure:**

```json
{
  "id": 567890123456789012,
  "folderId": 456789012345678901,
  "versionId": 678901234567890123,
  "title": "Wave-Particle Duality",
  "definition": "The concept that every particle or quantum entity may be described as either a particle or a wave.",
  "explanation": "This principle states that light and matter exhibit properties of both waves and particles. For example, an electron can be diffracted like a wave, but can also be detected as a single particle.",
  "formulaOrCode": "λ = h / p",
  "example": "The double-slit experiment is a classic demonstration of wave-particle duality.",
  "difficulty": 3.5,
  "createdAt": "2025-07-30T14:45:00"
}
```

---

## Module 5: Explanation Document (AI-Generated Content)

This module handles the structured documents generated by the AI. The data is deeply nested.

### `ExplanationDocumentSummaryVO`

A lightweight object for listing all generated documents.

- **Fused Entities**: `ExplanationDocumentEntity`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 789012345678901234,
  "title": "AI-Generated Guide to Quantum Entanglement",
  "description": "An in-depth explanation of quantum entanglement, its principles, and implications.",
  "status": 2,
  "updatedAt": "2025-07-30T18:00:00"
}
```

**Notes on Fields:**

- `status`: Enum Mapping: `0` = DRAFT, `1` = AI_GENERATING, `2` = NORMAL, `3` = AI_GENERATE_FAILED

### `ExplanationDocumentDetailVO`

The main, comprehensive VO for displaying a full explanation document. This is a complex, nested object assembled by the
backend service.

- **Fused Entities**: `ExplanationDocumentEntity`, `ExplanationSectionEntity`, `ExplanationSubsectionEntity`,
  `ExplanationPointEntity`, `ExplanationRelationEntity`.
- **JOIN Conditions**: The backend service will fetch all components where the foreign key matches the document ID (
  e.g., `esExplanationDocumentId = edId`) and assemble the nested structure.

**JSON Structure:**

```json
{
  "id": 789012345678901234,
  "title": "AI-Generated Guide to Quantum Entanglement",
  "description": "An in-depth explanation of quantum entanglement, its principles, and implications.",
  "status": 2,
  "sectionOrder": [101, 102, 103],
  "sections": [
    {
      "id": 101,
      "title": "Introduction to Entanglement",
      "content": "This chapter introduces the fundamental concepts of quantum entanglement...",
      "subsections": [
        {
          "id": 201,
          "title": "What is Entanglement?",
          "content": "Quantum entanglement is a physical phenomenon that occurs when a pair or group of particles is generated..."
        },
        {
          "id": 202,
          "title": "Historical Context",
          "content": "The concept was famously described by Einstein as 'spooky action at a distance'..."
        }
      ]
    }
  ],
  "points": [
    {
      "id": 301,
      "title": "EPR Paradox",
      "definition": "A thought experiment by Einstein, Podolsky, and Rosen...",
      "explanation": "It highlighted the counter-intuitive nature of quantum mechanics.",
      "formulaOrCode": null,
      "example": null,
      "styleConfig": "{\"x\": 100, \"y\": 200, \"fill\": \"#88ddff\"}"
    }
  ],
  "relations": [
    {
      "id": 401,
      "sourcePointId": 301,
      "targetPointId": 302,
      "relationType": "ContrastsWith",
      "description": "The EPR Paradox contrasts with Bell's theorem.",
      "styleConfig": "{\"stroke\": \"#ff8888\"}"
    }
  ]
}
```

---

## Module 6: Asynchronous Task Tracking

Provides frontend visibility into the status of long-running backend jobs, like AI content generation.

### `AsyncTaskVO`

A simple object to show the status and result of a background task.

- **Fused Entities**: `AsyncTask`
- **JOIN Conditions**: N/A

**JSON Structure:**

```json
{
  "id": 890123456789012345,
  "taskType": "EXPLANATION_AI_GENERATE",
  "status": 2,
  "userFriendlyMessage": "Explanation document 'Guide to Quantum Entanglement' generated successfully.",
  "createdAt": "2025-07-30T17:50:00",
  "startedAt": "2025-07-30T17:51:00",
  "finishedAt": "2025-07-30T18:00:00"
}
```

**Notes on Fields:**

- `status`: Enum Mapping from `TaskStatusEnum`: `0` = PENDING, `1` = RUNNING, `2` = SUCCEEDED, `3` = FAILED, `4` =
  CANCELED
