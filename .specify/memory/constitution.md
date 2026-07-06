<!--
Sync Impact Report
Version change: [TEMPLATE] → 1.0.0
Modified principles: N/A (initial ratification; all 5 principle slots filled for the first time)
Added sections:
  - I. Clean Architecture (Robert C. Martin)
  - II. BDD Testing Discipline (Unit, Integration, Functional)
  - III. SOLID, YAGNI, DRY
  - IV. API-First with OpenAPI Contracts
  - V. Coverage Quality Gates
  - Technology & Tooling Constraints (Section 2)
  - Quality Gates & Development Workflow (Section 3)
  - Governance
Removed sections: None (template placeholders replaced)
Templates requiring updates:
  - ✅ .specify/templates/plan-template.md (Constitution Check gate is derived dynamically from this file; no hardcoded edits needed)
  - ✅ .specify/templates/spec-template.md (Given/When/Then acceptance scenarios already align with BDD principle; no changes needed)
  - ✅ .specify/templates/tasks-template.md (Contract test tasks already map to API-First/OpenAPI principle; test phases already compatible with BDD + coverage gates)
  - ✅ .specify/templates/checklist-template.md (generic, no constitution-specific references)
Follow-up TODOs: None
-->

# Cafestock Service Constitution

## Core Principles

### I. Clean Architecture (Robert C. Martin)

The system MUST be organized following Robert C. Martin's Clean Architecture:
concentric layers of **Entities** (enterprise business rules), **Use Cases**
(application business rules), **Interface Adapters** (controllers,
presenters, gateways), and **Frameworks & Drivers** (web, database,
external agents). Dependencies MUST only point inward — outer layers may
depend on inner layers, never the reverse. Business and use-case logic
(domain module) MUST NOT import framework types (e.g., Spring, JPA,
Servlet APIs); such details are injected through interfaces (ports) defined
by the inner layers and implemented by the outer layers (adapters). Each
layer MUST be independently testable without booting the full application
context.

**Rationale**: Isolating business rules from frameworks, databases, and UI
keeps the domain stable while infrastructure evolves, and enables fast,
framework-free unit tests for the most valuable logic in the system.

### II. BDD Testing Discipline (NON-NEGOTIABLE)

Every feature MUST be verified with unit, integration, and functional tests
written using Behavior-Driven Development (BDD): scenarios expressed in
**Given/When/Then** form, describing observable behavior from the
perspective of a consumer (a unit under test, a use case, or an end user).
Unit tests MUST cover use cases and domain logic in isolation (mocked
ports). Integration tests MUST verify adapters against real or
containerized infrastructure (database, messaging, etc.). Functional tests
MUST exercise complete flows through the public API contract. Tests MUST be
written before or alongside implementation for each unit of work, and a
feature MUST NOT be considered done while any of its three test levels are
missing.

**Rationale**: BDD scenarios keep tests readable as living documentation of
system behavior, and requiring all three test levels prevents gaps where
units pass in isolation but fail when wired together or exposed to
consumers.

### III. SOLID, YAGNI, DRY

All code MUST follow SOLID principles (Single Responsibility, Open/Closed,
Liskov Substitution, Interface Segregation, Dependency Inversion). Features
and abstractions MUST NOT be built ahead of a proven, current need (YAGNI);
speculative extensibility requires explicit justification in the PR/plan.
Duplicated logic MUST be consolidated (DRY) once a third occurrence appears
or once duplication crosses a module boundary — two similar but unrelated
call sites MAY remain separate if unifying them would create a forced,
premature abstraction.

**Rationale**: These practices keep the Clean Architecture boundaries
honest — SOLID keeps modules replaceable, YAGNI keeps the use-case layer
free of speculative complexity, and DRY prevents divergent copies of
business rules from silently drifting apart.

### IV. API-First with OpenAPI Contracts

Every HTTP API MUST be designed contract-first: an OpenAPI (3.x) document
MUST exist and be approved before controller code is written, and it is the
single source of truth for request/response shapes, status codes, and
error models. Server interfaces (and, where applicable, client stubs/DTOs)
MUST be generated from the OpenAPI contract using `openapi-generator`
(configured as a Gradle build task) rather than hand-written. Hand-editing
generated code is FORBIDDEN; contract changes MUST flow through the
OpenAPI spec and regeneration. Breaking changes to a published contract
MUST bump the API's major version.

**Rationale**: Contract-first design forces API design decisions to be made
deliberately and reviewed before implementation, and generating code from
the contract guarantees the implementation can never silently drift from
what is documented and consumed by clients.

### V. Coverage Quality Gates

Automated test coverage MUST be measured with JaCoCo on every build. A
build MUST fail if: (a) any production class falls below 80% instruction
coverage, or (b) the project-wide (global) coverage falls below 80%.
JaCoCo verification rules enforcing both thresholds MUST be wired into the
Gradle `check` task so violations block merges via CI, and a human-readable
JaCoCo HTML/XML report MUST be generated on every test run.

**Rationale**: Per-class and global thresholds together prevent both
"one well-tested module hiding many untested ones" and "average coverage
looks fine while a critical class is essentially untested."

## Technology & Tooling Constraints

- **Language/Runtime**: Java 17 (Gradle toolchain), Spring Boot.
- **Build**: Gradle is the single build tool; the `openapi-generator-gradle-plugin`
  MUST be used for contract-driven code generation, and the
  `jacoco` Gradle plugin MUST be used for coverage measurement and
  enforcement.
- **Persistence**: Database access MUST be confined to the Interface
  Adapters / Frameworks & Drivers layers (e.g., Spring Data JPA
  repositories implementing domain-defined port interfaces); domain and
  use-case code MUST NOT reference JPA/Hibernate types directly.
- **API Documentation**: The OpenAPI document(s) MUST live in the
  repository under version control and MUST be kept in sync with the
  generated code on every change.

## Quality Gates & Development Workflow

- A pull request MUST NOT merge unless: the OpenAPI contract (if the change
  touches an API) is updated and code regenerated from it, unit +
  integration + functional BDD tests exist for the change, and the JaCoCo
  per-class (>80%) and global (≥80%) coverage gates pass.
- Code review MUST verify Clean Architecture layering (no inward
  dependency violations) and flag any SOLID/YAGNI/DRY violations before
  approval.
- CI MUST run the full test suite (unit, integration, functional) and the
  JaCoCo verification task on every push; a red build blocks merge.

## Governance

This constitution supersedes all other project practices, style guides, or
undocumented conventions. Any conflict between this document and existing
code or habits MUST be resolved in favor of this constitution, with the
non-compliant code scheduled for remediation.

**Amendment procedure**: Amendments are proposed via a documented change
(PR modifying this file) that includes the rationale for the change. The
Sync Impact Report at the top of this file MUST be updated on every
amendment, and dependent templates (`plan-template.md`, `spec-template.md`,
`tasks-template.md`, `checklist-template.md`) MUST be reviewed for
consistency as part of the same PR.

**Versioning policy**: This constitution follows semantic versioning:
- **MAJOR**: Backward-incompatible governance changes or removal/redefinition
  of a principle.
- **MINOR**: A new principle or materially expanded section is added.
- **PATCH**: Clarifications, wording, or non-semantic refinements.

**Compliance review**: Every PR/plan MUST pass the "Constitution Check" gate
defined in `plan-template.md` before implementation proceeds, and again
before merge. Complexity that violates a principle MUST be justified in the
plan's Complexity Tracking table or the principle MUST be revised through
the amendment procedure above — silent non-compliance is not permitted.

**Version**: 1.0.0 | **Ratified**: 2026-07-05 | **Last Amended**: 2026-07-05
