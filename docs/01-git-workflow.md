# Git Workflow (Required)

This repo uses a Pull Request workflow.
You must not push directly to `main`.

## Daily workflow (required)

### Step 1: Sync main
- `git checkout main`
- `git pull origin main`

### Step 2: Create a new branch
Branch format:
- feature/<-your-name>/a-short-title
- fix/<-your-name>/a-short-title
- chore/<-your-name>/a-short-title

Examples:
- feature/smriti/feedback-project-create-student
- feature/malcolm/OT-02-create-order
- fix/kevin/TF-07-analytics-bug

Commands:
- git checkout -b feature/<-your-name>/<ticket>-short-title

### Step 3: Make changes only inside your folder
Allowed:
- candidates/<-your-name>/**
- candidates/_shared/** (only when assigned)

Not allowed:
- other candidates folders
- docs/ (unless coach requests)
- assignments/ (unless coach requests)

### Step 4: Commit small changes
Make small commits that represent one step.

Commit format:
- feat: ...
- fix: ...
- test: ...
- docs: ...
- chore: ...

Example:
- feat: add create task endpoint

Commands:
- `git add .`
- `git commit -m "feat: add create task endpoint"`

### Step 5: Push branch
- `git push -u origin <branch-name>`

### Step 6: Open a Pull Request
Open a PR into main and fill the PR template.

### Step 7: Resolve review comments
Update your branch and push again.

### Step 8: Merge (coach merges)
The coach will merge after review + CI passes.

## Important rules
- Do not commit generated files (target/, .idea/, etc.)
- Do not force push unless you know what you're doing
- Keep PRs small (1 feature per PR)
