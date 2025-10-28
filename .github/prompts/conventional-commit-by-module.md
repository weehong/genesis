# Agent Prompt: Conventional Commit by Module/Domain

## Objective
Perform a soft reset of the most recent commit and reorganize all changes into multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.

## Prerequisites
- Changes should already be committed (will be reset and reorganized)
- Working directory should be clean except for the commit to be reorganized

## Steps

### 1. Soft Reset and Unstage
```bash
# Reset the most recent commit (or specify commit hash)
git reset --soft HEAD~1

# Unstage all changes
git reset HEAD
```

### 2. Identify Module Groups
Analyze the changed files and group them by:
- **Module/Domain** (e.g., company, constraint, branch, authentication, shared)
- **Functional Area** (e.g., controllers, services, validators, tests)
- **Type of Change** (e.g., fixes, features, refactoring, tests)

Common grouping patterns:
- One commit per module for related changes
- Separate commits for core logic vs tests
- Separate commits for breaking changes
- Database migrations in their own commit

### 3. Create Commits by Module

For each module group, stage and commit files:

```bash
# Stage files for the module
git add <module-files>

# Create commit with structured message
git commit -m "<type>(<scope>): <summary>" \
  -m "- <Section 1>" \
  -m "  - <detail>" \
  -m "" \
  -m "- <Section 2>" \
  -m "  - <detail>" \
  -m "" \
  -m "- Notes" \
  -m "  - Breaking changes: <none | description>" \
  -m "  - Follow-ups: <none | description>"
```

### 4. Conventional Commit Format

#### Subject Line
```
<type>(<scope>): <summary>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `refactor` - Code refactoring (no functional change)
- `perf` - Performance improvement
- `test` - Adding or updating tests
- `docs` - Documentation changes
- `chore` - Maintenance tasks (build, dependencies, formatting)
- `ci` - CI/CD changes
- `build` - Build system changes

**Scope:** Module or domain name (e.g., company, constraint, shared, db)

**Summary:** Concise description (<72 chars, imperative mood)

#### Body Structure
Use these sections as applicable:

```
- Introduces <feature surface> (API/CLI/job/UI)
  - <method/route/command/name>
  - Input: <args/payload> with validation: <rules>
  - Output/side effects: <response/format or state change>

- Fixes <issue/problem>
  - <description of what was wrong>
  - <description of the fix>

- Implements core logic
  - <modules/classes/functions> with <responsibilities>
  - External deps: <services/libs> (mocked/real)

- Data and persistence
  - <models/schemas/migrations> changes

- Error handling and logging
  - <strategy/shape/codes>
  - <edge cases covered>

- Tests and fixtures
  - <test suites/types> and <fixtures/helpers> added/updated
  - <coverage areas>

- Docs/Config/Build
  - <docs updated>, <config keys/env vars>, <build/CI scripts>

- Notes
  - Breaking changes: <none | description>
  - Follow-ups: <next steps>
```

### 5. Verify Results

```bash
# View commit history
git log --oneline --graph -<N>

# View detailed commit with files
git --no-pager log --pretty=medium --name-status -<N>

# Verify working tree is clean
git status
```

## Example Module Groupings

### Backend Application (Java/Spring Boot)
1. **Module-specific commits** (one per module):
   - `fix(company): correct package name and add validation`
   - `fix(constraint): correct package name and remove unused path variable`
   - `fix(branch): correct package name and remove unused path variable`
   - `fix(authentication): correct package name and add exception tests`

2. **Shared/infrastructure commit**:
   - `refactor(shared): improve security, logging, and data handling`

3. **Database commit**:
   - `chore(db): format SQL migration file`
   - `feat(db): add user preferences schema`

4. **Test-only commit** (if substantial):
   - `test(integration): add end-to-end API tests`

### Frontend Application (React/Vue/Angular)
1. **Feature commits**:
   - `feat(auth): add login form with validation`
   - `feat(dashboard): add user analytics widget`

2. **Component commits**:
   - `refactor(ui): extract reusable button components`
   - `fix(forms): correct validation error display`

3. **State management**:
   - `refactor(store): migrate to Redux Toolkit`

4. **Styling/assets**:
   - `chore(styles): update theme colors and spacing`

### Full-stack Monorepo
1. **Backend commits** (grouped by module)
2. **Frontend commits** (grouped by feature/component)
3. **Shared commits** (types, utilities, configs)
4. **Infrastructure commits** (Docker, CI/CD, scripts)

## Best Practices

### Commit Granularity
- ✅ **DO:** Group related changes within a module
- ✅ **DO:** Keep commits focused on a single domain/module
- ✅ **DO:** Separate breaking changes into their own commits
- ❌ **DON'T:** Mix unrelated modules in one commit
- ❌ **DON'T:** Create too many tiny commits (use judgment)

### Commit Messages
- ✅ **DO:** Use imperative mood ("add feature" not "added feature")
- ✅ **DO:** Be specific in the summary
- ✅ **DO:** Document breaking changes clearly
- ✅ **DO:** Include follow-up actions if needed
- ❌ **DON'T:** Use vague summaries like "fix bugs" or "update code"
- ❌ **DON'T:** Omit important context from the body

### File Organization
- ✅ **DO:** Stage files carefully for each commit
- ✅ **DO:** Verify staged files before committing (`git diff --cached --name-only`)
- ✅ **DO:** Keep test files with their corresponding module
- ❌ **DON'T:** Split a single logical change across multiple commits

## Quick Reference Commands

```bash
# Reset last commit, keep changes
git reset --soft HEAD~1 && git reset HEAD

# Stage specific files
git add <file1> <file2> <directory/>

# View staged files
git diff --cached --name-only

# View staged changes
git diff --cached

# Commit with multi-line message
git commit -m "line 1" -m "line 2" -m "line 3"

# View recent commits
git log --oneline -10

# View commit details
git show <commit-hash>

# Amend last commit (if needed)
git commit --amend

# Interactive rebase (for further reorganization)
git rebase -i HEAD~<N>
```

## Template for Quick Copy-Paste

```bash
# 1. Reset and unstage
git reset --soft HEAD~1
git reset HEAD

# 2. Create commits by module
git add <module-files>
git commit -m "<type>(<scope>): <summary>" \
  -m "- Fixes/Introduces/Implements <what>" \
  -m "  - <detail 1>" \
  -m "  - <detail 2>" \
  -m "" \
  -m "- Tests and fixtures" \
  -m "  - <test details>" \
  -m "" \
  -m "- Notes" \
  -m "  - Breaking changes: <none | description>" \
  -m "  - Follow-ups: <none | description>"

# 3. Verify
git log --oneline --graph -10
git status
```

## Notes
- This approach creates a clean, reviewable commit history
- Each commit can be reviewed, cherry-picked, or reverted independently
- Breaking changes are clearly documented and isolated
- Follows industry best practices (Conventional Commits specification)
- Makes git history more navigable and meaningful

