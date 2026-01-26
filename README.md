# Cohort Packaging Repository

Welcome! This repository is shared across all candidates to practice:

- Git basics (clone, commit, push)
- Branching strategy
- Pull Requests (PRs)
- Code review
- Merge conflicts and conflict resolution
- Building a Spring Boot REST API project using an assigned design spec

## How this repo is organized

- docs/  
  Shared instructions for Git workflow, PR rules, merge conflict practice, and coding standards.

- assignments/  
  Candidate design specs and the grading rubric.

- candidates/  
  Each candidate has their own isolated workspace under `candidates/<name>/app/`.

- scripts/  
  Helper scripts for running tests (todo).

- .github/  
  PR template, CODEOWNERS, and CI workflow.

## Rules (high level)

1. Do not push directly to `main`.
2. Always create a branch and open a Pull Request.
3. Only modify files inside your folder: `candidates/<your-name>/`
4. Everyone must update the shared status file daily:
   - `candidates/_shared/weekly-status.md`
5. Everyone must complete merge conflict practice:
   - `candidates/_shared/conflict-practice.md`

## Quick start (candidate)

1. Read your design spec in `assignments/<your-name>/design-spec.md`
2. Create your branch
3. Work only inside `candidates/<your-name>/app/`
4. Push your branch and open a PR

See: [docs/01-git-workflow.md](./docs/01-git-workflow.md)
