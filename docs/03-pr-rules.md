# Pull Request Rules (Required)

All work must be submitted via Pull Requests (PRs).

## 1) PR size rule
PRs should be small and focused.
Recommended:
- 1 feature or 1 fix per PR

## 2) What must be included in every PR
- A clear title
- A description of what changed
- How to test it
- Screenshots/logs if helpful
- A checklist (PR template)

## 3) Files you are allowed to modify
Allowed:
- candidates/<your-name>/**
- candidates/_shared/** (only when assigned)

Not allowed:
- candidates/<other-name>/**
- assignments/** (unless coach asks)
- docs/** (unless coach asks)

## 4) CI must pass
PRs must pass CI before merge.
CI runs:
- mvn test

## 5) Merge strategy
We use squash merge (recommended).
The coach will merge after review.

## 6) Minimum PR count (weekly)
To practice Git properly, each candidate must open at least 5 PRs:
- Setup PR
- 2 feature PRs
- Analytics PR
- Final polish/testing PR
