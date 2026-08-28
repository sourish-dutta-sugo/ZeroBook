# Contributing to ZeroBook

Thanks for helping improve ZeroBook. This project is kept intentionally small and focused, so contributions are easiest to review when they stay scoped to one clear change.

## Before you start

- Open an issue first for larger changes so we can align on scope.
- Keep Android Studio settings and local build artifacts out of commits.
- Work on `develop` for active changes. Keep `master` as the stable branch.
- Use a branch name that describes the work, such as `feature/quick-sale` or `fix/ledger-balance`.

## Branch model

- `master` is for stable, releasable code.
- `develop` is for active integration work.
- `feature/*` branches are for one feature at a time.
- `fix/*` branches are for one bug fix at a time.

## Local setup

1. Clone the repository.
2. Open it in Android Studio.
3. Sync Gradle and make sure the project builds.
4. Run the app on a device or emulator before opening a pull request.

## Commit messages

Use Conventional Commits whenever possible:

- `feat: add GST split on quick sale`
- `fix: correct ledger balance rounding`
- `docs: update setup instructions`
- `chore: refresh CI workflow`

## Pull request checklist

- The project builds locally.
- Relevant tests pass.
- The change stays focused on one task.
- Any user-facing behavior changes are explained in the PR description.
- README, docs, or changelog updates are included when needed.

## Notes

- Do not commit `.idea/`, `.vscode/`, log files, or local machine secrets.
- If a change touches app behavior, include the reasoning and the test coverage you added.
