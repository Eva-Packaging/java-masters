# Branching Rules (Required)

## 1) Main branch is protected
- No direct pushes to main
- No work happens on main

## 2) Branch naming rules
Use one of these prefixes:
- feature/
- fix/
- chore/

Branch format:
- `<type>/<candidate>/<assignment>-short-title`

Examples:
- feature/smriti/assignment-enrollment-endpoint
- fix/malcolm/assignment-status-transition-bug
- chore/kevin/assignment-repo-setup

## 3) Ticket IDs
Ticket IDs can be simple.
Examples:
- SP-01, SP-02
- OT-01, OT-02
- TF-01, TF-02

## 4) One feature per branch
Do not mix multiple features in one branch.
If you do, your PR review will be harder and slower.
