# Quick Prompt: Organize Commits by Module

## One-Line Prompt

```
Perform a soft reset of the most recent commit and reorganize all changes into multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.
```

## Detailed Prompt

```
Perform a soft reset of the most recent commit (or commit <HASH>) to unstage all changes while keeping them in the working directory, then create multiple separate commits grouped by module/domain.

Organize the commits as follows:

1. **Group by module/domain** - One commit per module (e.g., company, constraint, branch, authentication, shared)
2. **Separate infrastructure** - Database migrations, build configs, CI/CD in separate commits
3. **Isolate breaking changes** - If a module has breaking changes, consider separating them

For each commit:
- Use Conventional Commit format: `<type>(<scope>): <summary>`
- Types: feat, fix, refactor, perf, test, docs, chore, ci, build
- Include structured body with sections:
  - Introduces/Fixes/Implements <what>
  - Tests and fixtures
  - Notes (breaking changes, follow-ups)

Example structure:
```
fix(company): correct package name and add image validation

- Fixes exception handler package reference
  - CompanyExceptionHandler: com.resetrix.genesis.modules.company → com.resetrix.horaion.modules.company

- Introduces image validation for file uploads
  - @ValidImage annotation with configurable maxSize (default 5MB) and allowedTypes (JPEG, PNG, GIF)
  - Applied to CompanyRequest.logo field

- Tests and fixtures
  - CompanyServiceUpdateByUuidTest: fixed logo file attachment in test cases

- Notes
  - Breaking changes: none
  - Follow-ups: none
```

Verify all changes are committed and working tree is clean when done.
```

## Usage Examples

### Example 1: Basic Usage
```
Perform a soft reset of the most recent commit and reorganize all changes into multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.
```

### Example 2: With Specific Commit
```
Perform a soft reset of commit abc1234 to unstage all changes while keeping them in the working directory, then create multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.
```

### Example 3: With Custom Grouping
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped as follows:

1. Frontend components (one commit per major component)
2. Backend API endpoints (one commit per module)
3. Database migrations (single commit)
4. Tests (one commit per module)
5. Documentation and configuration (single commit)

Use Conventional Commit format with structured bodies for each commit.
```

### Example 4: With Specific Module List
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped by these modules:

1. User module (authentication, authorization, user management)
2. Product module (catalog, inventory, pricing)
3. Order module (cart, checkout, payment)
4. Shared utilities (helpers, constants, validators)
5. Database and infrastructure

Use Conventional Commit format with structured bodies for each commit.
```

## Quick Reference

### Conventional Commit Types
- `feat` - New feature
- `fix` - Bug fix
- `refactor` - Code refactoring
- `perf` - Performance improvement
- `test` - Tests only
- `docs` - Documentation
- `chore` - Maintenance (build, deps, formatting)
- `ci` - CI/CD changes
- `build` - Build system changes

### Body Sections (use as applicable)
- Introduces <feature>
- Fixes <issue>
- Implements core logic
- Data and persistence
- Error handling and logging
- Tests and fixtures
- Docs/Config/Build
- Notes (breaking changes, follow-ups)

### Verification Commands
```bash
git log --oneline --graph -10
git status
```

