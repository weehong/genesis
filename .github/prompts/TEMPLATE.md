# Template: Custom Git Commit Prompt

Use this template to create your own custom git commit prompts.

## Basic Template

```
[ACTION] [TARGET] and [ORGANIZE/FORMAT] following [STANDARD] with [DETAILS].
```

### Examples

**Single Commit:**
```
Inspect staged changes and create a conventional commit following Conventional Commits format with structured body.
```

**Split Commits:**
```
Perform a soft reset of the most recent commit and reorganize all changes into multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.
```

---

## Detailed Template

```
[ACTION] [TARGET] [OPTIONAL: to unstage all changes while keeping them in the working directory], 
then [ORGANIZE] into [NUMBER/STRUCTURE] commits [GROUPING_STRATEGY].

[OPTIONAL: Organize the commits as follows:]
1. [Group 1] - [Description]
2. [Group 2] - [Description]
3. [Group 3] - [Description]

For each commit:
- Use [COMMIT_FORMAT]: `<type>(<scope>): <summary>`
- Types: [ALLOWED_TYPES]
- Include [BODY_STRUCTURE]:
  - [Section 1]
  - [Section 2]
  - Notes (breaking changes, follow-ups)

[OPTIONAL: Verify [VERIFICATION_CRITERIA] when done.]
```

---

## Fill-in-the-Blanks Template

### [ACTION]
Choose one:
- `Inspect`
- `Analyze`
- `Perform a soft reset of`
- `Review`
- `Examine`

### [TARGET]
Choose one:
- `staged changes`
- `the most recent commit`
- `commit <HASH>`
- `the last N commits`
- `all uncommitted changes`

### [ORGANIZE]
Choose one:
- `create a single commit`
- `reorganize all changes`
- `split into multiple commits`
- `group changes`
- `separate into commits`

### [GROUPING_STRATEGY]
Choose one or more:
- `grouped by module/domain`
- `grouped by feature`
- `grouped by type (fix/feat/refactor)`
- `one commit per file`
- `one commit per logical change`
- `following this structure: [custom structure]`

### [COMMIT_FORMAT]
Choose one:
- `Conventional Commit format`
- `Semantic commit format`
- `Custom format: [specify]`

### [ALLOWED_TYPES]
Common options:
- `feat, fix, refactor, perf, test, docs, chore, ci, build`
- `feature, bugfix, hotfix, refactor, test`
- `Custom types: [specify]`

### [BODY_STRUCTURE]
Common sections:
- `Introduces <feature>`
- `Fixes <issue>`
- `Implements core logic`
- `Data and persistence`
- `Error handling and logging`
- `Tests and fixtures`
- `Docs/Config/Build`
- `Notes (breaking changes, follow-ups)`

### [VERIFICATION_CRITERIA]
Common options:
- `all changes are committed`
- `working tree is clean`
- `all tests pass`
- `no linting errors`
- `build succeeds`

---

## Example Custom Prompts

### Example 1: Frontend Component Commits
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped by component type.

Organize the commits as follows:
1. UI Components (Button, Input, Modal, etc.)
2. Layout Components (Header, Footer, Sidebar)
3. Page Components (Home, Dashboard, Settings)
4. Utility Components (ErrorBoundary, Loading, etc.)
5. Styles and Assets
6. Tests

For each commit:
- Use Conventional Commit format: `<type>(<component>): <summary>`
- Types: feat, fix, refactor, style, test
- Include structured body with:
  - Component details (props, state, behavior)
  - Styling changes
  - Tests added/updated
  - Notes (breaking changes, follow-ups)

Verify all changes are committed and working tree is clean when done.
```

### Example 2: API Endpoint Commits
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped by API endpoint.

Organize the commits as follows:
1. Authentication endpoints (one commit)
2. User management endpoints (one commit)
3. Product endpoints (one commit)
4. Order endpoints (one commit)
5. Shared middleware and utilities (one commit)
6. Tests and documentation (one commit)

For each commit:
- Use Conventional Commit format: `<type>(api): <summary>`
- Types: feat, fix, refactor, test, docs
- Include structured body with:
  - Endpoint details (method, path, params, response)
  - Validation rules
  - Error handling
  - Tests and fixtures
  - API documentation updates
  - Notes (breaking changes, follow-ups)

Verify all changes are committed when done.
```

### Example 3: Database Migration Commits
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped by database change type.

Organize the commits as follows:
1. Schema changes (tables, columns, indexes)
2. Data migrations (seed data, transformations)
3. Model/Entity updates (ORM mappings)
4. Repository/DAO updates (queries, methods)
5. Tests for database changes

For each commit:
- Use Conventional Commit format: `<type>(db): <summary>`
- Types: feat, fix, refactor, test
- Include structured body with:
  - Migration details (up/down scripts)
  - Schema changes (tables, columns, constraints)
  - Data impact (existing data handling)
  - Rollback strategy
  - Tests for migrations
  - Notes (breaking changes, follow-ups)

Verify all migrations are tested and reversible when done.
```

### Example 4: Microservice Commits
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped by service.

Organize the commits as follows:
1. Auth Service changes
2. User Service changes
3. Product Service changes
4. Order Service changes
5. Shared libraries/utilities
6. API Gateway changes
7. Infrastructure/deployment configs
8. Integration tests

For each commit:
- Use Conventional Commit format: `<type>(<service>): <summary>`
- Types: feat, fix, refactor, perf, test, chore
- Include structured body with:
  - Service changes (endpoints, business logic)
  - Inter-service communication updates
  - Database/cache changes
  - Configuration updates
  - Tests and fixtures
  - Notes (breaking changes, deployment order, follow-ups)

Verify all services can be deployed independently when done.
```

### Example 5: Bug Fix Commits
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped by bug severity and module.

Organize the commits as follows:
1. Critical bugs (security, data loss)
2. High priority bugs (broken features)
3. Medium priority bugs (degraded UX)
4. Low priority bugs (minor issues)
5. Regression tests for all fixes

For each commit:
- Use Conventional Commit format: `fix(<module>): <summary>`
- Include structured body with:
  - Bug description (what was broken)
  - Root cause analysis
  - Fix implementation
  - Regression tests added
  - Related issues/tickets
  - Notes (breaking changes, follow-ups)

Verify all tests pass when done.
```

---

## Customization Checklist

When creating a custom prompt, consider:

- [ ] **Action**: What should the agent do? (inspect, reset, reorganize)
- [ ] **Target**: What to work with? (staged changes, commits, files)
- [ ] **Grouping**: How to organize? (by module, feature, type)
- [ ] **Format**: What commit format? (Conventional Commits, custom)
- [ ] **Structure**: What body sections? (standard, custom)
- [ ] **Verification**: What to check? (tests, build, clean tree)
- [ ] **Context**: Any project-specific requirements?

---

## Tips for Effective Prompts

### Be Specific
❌ "Organize commits"
✅ "Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped by module/domain"

### Provide Structure
❌ "Split into commits"
✅ "Organize the commits as follows: 1. Module A, 2. Module B, 3. Tests"

### Specify Format
❌ "Use good commit messages"
✅ "Use Conventional Commit format: `<type>(<scope>): <summary>`"

### Include Verification
❌ "Create commits"
✅ "Create commits and verify all changes are committed and working tree is clean"

### Add Context
❌ "Group by module"
✅ "Group by module (auth, payment, user, product) with tests in separate commits"

---

## Testing Your Prompt

1. **Test with small changes first**
   - Try your prompt on a simple commit
   - Verify the agent understands the structure

2. **Iterate and refine**
   - Adjust based on results
   - Add more specificity if needed

3. **Document examples**
   - Save successful prompts
   - Note what works well

4. **Share with team**
   - Create a team library of prompts
   - Standardize commit practices

---

## Saving Your Custom Prompts

Create a new file in `.github/prompts/`:

```bash
# Create your custom prompt file
touch .github/prompts/my-custom-prompt.md

# Edit and add your prompt
# Follow the structure of existing prompts

# Update README.md to include your new prompt
```

---

**Happy Committing! 🚀**

