# Agent Prompt: Single Conventional Commit

## Objective
Inspect staged changes and create a single conventional commit with a clear subject and structured body.

## One-Line Prompt

```
Inspect staged changes, craft a clear Conventional Commit subject, generate a concise structured body, commit the changes, and print the result.
```

## Detailed Prompt

```
Inspect the staged changes in git, analyze the modifications, and create a conventional commit with:

1. **Subject line** following Conventional Commits format:
   - Format: `<type>(<scope>): <summary>`
   - Type: feat, fix, refactor, perf, test, docs, chore, ci, build
   - Scope: module/domain name (short and meaningful)
   - Summary: concise description (<72 chars, imperative mood)

2. **Structured body** with relevant sections:
   - Introduces <feature> (if applicable)
   - Fixes <issue> (if applicable)
   - Implements core logic (if applicable)
   - Data and persistence (if applicable)
   - Error handling and logging (if applicable)
   - Tests and fixtures (if applicable)
   - Docs/Config/Build (if applicable)
   - Notes
     - Breaking changes: <none | description>
     - Follow-ups: <none | description>

3. **Commit and verify**:
   - Execute the commit
   - Show the result with: `git --no-pager log -1 --pretty=medium --name-status`
```

## Steps

### 1. Inspect Staged Changes
```bash
# View staged files
git diff --name-only --cached

# View staged changes summary
git diff --cached --stat

# View key file changes
git diff --cached <important-files>
```

### 2. Analyze Changes
Identify:
- **Type of change**: feature, fix, refactor, etc.
- **Scope**: which module/domain is affected
- **Impact**: breaking changes, new APIs, bug fixes
- **Related changes**: tests, docs, configs

### 3. Craft Subject Line

**Format:**
```
<type>(<scope>): <summary>
```

**Examples:**
- `feat(auth): add OAuth2 login flow`
- `fix(payment): correct tax calculation for EU countries`
- `refactor(database): migrate to connection pooling`
- `perf(api): add caching layer for product queries`
- `test(checkout): add integration tests for payment flow`
- `docs(api): update authentication endpoint documentation`
- `chore(deps): upgrade Spring Boot to 3.2.0`

### 4. Generate Structured Body

**Template:**
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

- Docs/Config/Build
  - <docs updated>, <config keys/env vars>, <build/CI scripts>

- Notes
  - Breaking changes: <none | description>
  - Follow-ups: <next steps>
```

**Example:**
```
feat(auth): add OAuth2 login flow

- Introduces OAuth2 authentication API
  - POST /api/v1/auth/oauth2/login
  - Input: { provider, code, redirectUri } with validation: required fields, supported providers
  - Output: { accessToken, refreshToken, expiresIn }

- Implements core logic
  - OAuth2Service with provider integration (Google, GitHub)
  - TokenService for JWT generation and validation
  - External deps: OAuth2 provider APIs (real)

- Data and persistence
  - Added oauth2_tokens table for refresh token storage
  - Migration V5__add_oauth2_tokens.sql

- Error handling and logging
  - Returns 401 for invalid credentials
  - Returns 400 for unsupported providers
  - Logs authentication attempts (no PII)

- Tests and fixtures
  - OAuth2ServiceTest with mocked provider responses
  - Integration tests for login flow
  - Fixtures for Google and GitHub OAuth2 responses

- Notes
  - Breaking changes: none
  - Follow-ups: add Microsoft provider support
```

### 5. Execute Commit

**Single-line body:**
```bash
git commit -m "feat(auth): add OAuth2 login flow"
```

**Multi-line body:**
```bash
git commit -m "feat(auth): add OAuth2 login flow" \
  -m "- Introduces OAuth2 authentication API" \
  -m "  - POST /api/v1/auth/oauth2/login" \
  -m "  - Input: { provider, code, redirectUri }" \
  -m "  - Output: { accessToken, refreshToken, expiresIn }" \
  -m "" \
  -m "- Implements core logic" \
  -m "  - OAuth2Service with provider integration" \
  -m "" \
  -m "- Notes" \
  -m "  - Breaking changes: none" \
  -m "  - Follow-ups: add Microsoft provider"
```

### 6. Verify Result

```bash
# View the commit
git --no-pager log -1 --pretty=medium --name-status

# View commit message only
git log -1 --pretty=format:"%B"
```

## Conventional Commit Types

| Type | Description | Example |
|------|-------------|---------|
| `feat` | New feature | `feat(api): add user search endpoint` |
| `fix` | Bug fix | `fix(auth): correct token expiration check` |
| `refactor` | Code refactoring | `refactor(db): extract query builder` |
| `perf` | Performance improvement | `perf(cache): implement Redis caching` |
| `test` | Add/update tests | `test(api): add integration tests` |
| `docs` | Documentation | `docs(readme): update installation steps` |
| `chore` | Maintenance | `chore(deps): update dependencies` |
| `ci` | CI/CD changes | `ci(github): add automated testing` |
| `build` | Build system | `build(webpack): optimize bundle size` |
| `style` | Code style/formatting | `style(eslint): fix linting errors` |

## Scope Guidelines

**Good scopes:**
- Module names: `auth`, `payment`, `user`, `product`
- Domain areas: `api`, `db`, `ui`, `cli`
- Component names: `header`, `footer`, `sidebar`
- Feature areas: `checkout`, `search`, `analytics`

**Avoid:**
- Too generic: `app`, `code`, `stuff`
- Too specific: `UserAuthenticationService`, `payment-gateway-stripe-integration`
- File names: `user.js`, `payment.service.ts`

## Best Practices

### Subject Line
- ✅ Use imperative mood: "add feature" not "added feature"
- ✅ Keep under 72 characters
- ✅ Don't end with a period
- ✅ Be specific and descriptive
- ❌ Don't use vague terms: "fix bugs", "update code"

### Body
- ✅ Explain **what** and **why**, not **how**
- ✅ Include breaking changes prominently
- ✅ Reference issue numbers if applicable
- ✅ Keep lines under 80 characters
- ❌ Don't repeat the subject line
- ❌ Don't include unnecessary details

### Commits
- ✅ One logical change per commit
- ✅ All tests should pass
- ✅ Code should be functional
- ❌ Don't mix unrelated changes
- ❌ Don't commit broken code

## Quick Templates

### Feature
```
feat(<scope>): <add/implement> <feature>

- Introduces <feature>
  - <API/UI/CLI details>
- Implements core logic
  - <key components>
- Tests and fixtures
  - <test coverage>
- Notes
  - Breaking changes: none
  - Follow-ups: <if any>
```

### Bug Fix
```
fix(<scope>): <correct/resolve> <issue>

- Fixes <problem>
  - <what was wrong>
  - <how it's fixed>
- Tests and fixtures
  - <regression tests>
- Notes
  - Breaking changes: none
  - Follow-ups: none
```

### Refactoring
```
refactor(<scope>): <extract/simplify/reorganize> <component>

- Implements core logic
  - <refactoring details>
  - <benefits>
- Tests and fixtures
  - <updated tests>
- Notes
  - Breaking changes: none
  - Follow-ups: none
```

### Documentation
```
docs(<scope>): <update/add> <documentation>

- Docs/Config/Build
  - <what was documented>
  - <where to find it>
- Notes
  - Breaking changes: none
  - Follow-ups: none
```

