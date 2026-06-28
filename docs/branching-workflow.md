# Branching Workflow

## Branch roles

- `main`: stable milestone releases only
- `develop`: integration branch for completed features
- `feature/*`: isolated implementation branches

## Delivery flow

```text
feature/* -> develop -> main
```

1. Create a feature branch from the latest `develop`.
2. Implement one focused capability.
3. Compile, run automated tests, and verify endpoints manually.
4. Update documentation to describe verified behavior.
5. Review the complete staged diff.
6. Commit and push the feature branch.
7. Open and merge a pull request into `develop`.
8. Retain the feature branch after merging.
9. Merge `develop` into `main` only after all milestone acceptance criteria pass.

Direct commits to `main` are not part of this workflow.
