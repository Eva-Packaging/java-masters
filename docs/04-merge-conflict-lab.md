# Merge Conflict Lab (Required)

Merge conflicts are normal in real teams.
This lab ensures you practice resolving conflicts safely.

## Shared files for conflict practice
- candidates/_shared/weekly-status.md
- candidates/_shared/conflict-practice.md

## Requirement
You must resolve at least 2 merge conflicts during the week.

## How conflicts will happen
Multiple candidates will edit the same shared files.
When you try to merge your PR, Git may show a conflict.

## How to resolve a conflict (basic steps)

### Option A: Resolve in GitHub UI
1. Open the PR
2. Click "Resolve conflicts"
3. Fix the conflicting lines
4. Mark resolved and commit

### Option B: Resolve locally
1. Update main:
   - `git checkout main`
   - `git pull origin main`

2. Go back to your branch:
   - `git checkout <your-branch>`

3. Merge main into your branch:
   - `git merge main`

4. Fix conflicts in files
5. Add and commit:
   - `git add .`
   - `git commit -m "chore: resolve merge conflict"`

6. Push:
   - `git push`

## What to write in your README (required)
In your candidate `README` add a section:

Merge Conflict Notes:
- Date:
- File:
- What happened:
- How I fixed it:
- What I learned:
