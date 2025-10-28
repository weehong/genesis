# Quick Reference: Agent Prompts for Git Commits

## 🎯 One-Line Prompts (Copy & Paste)

### Single Commit
```
Inspect staged changes, craft a clear Conventional Commit subject, generate a concise structured body, commit the changes, and print the result.
```

### Split Commit by Module
```
Perform a soft reset of the most recent commit and reorganize all changes into multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.
```

### Split Specific Commit
```
Perform a soft reset of commit <HASH> to unstage all changes while keeping them in the working directory, then create multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.
```

---

## 📋 Common Scenarios

### Scenario 1: Just Made Changes, Want to Commit
```bash
git add <files>
```
**Prompt:** Inspect staged changes, craft a clear Conventional Commit subject, generate a concise structured body, commit the changes, and print the result.

---

### Scenario 2: Just Committed, Want to Split
**Prompt:** Perform a soft reset of the most recent commit and reorganize all changes into multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.

---

### Scenario 3: Want to Reorganize Old Commit
```bash
git log --oneline  # Find the commit hash
```
**Prompt:** Perform a soft reset of commit `abc1234` to unstage all changes while keeping them in the working directory, then create multiple separate commits grouped by module/domain, following Conventional Commit format with structured bodies.

---

### Scenario 4: Custom Grouping
**Prompt:**
```
Perform a soft reset of the most recent commit and reorganize all changes into separate commits grouped as follows:

1. <Module 1> - <description>
2. <Module 2> - <description>
3. <Module 3> - <description>

Use Conventional Commit format with structured bodies for each commit.
```

---

## 🔤 Conventional Commit Cheat Sheet

### Format
```
<type>(<scope>): <summary>

<body>
```

### Types
| Type | Use For |
|------|---------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code refactoring |
| `perf` | Performance improvement |
| `test` | Tests only |
| `docs` | Documentation |
| `chore` | Maintenance, deps, formatting |
| `ci` | CI/CD changes |
| `build` | Build system changes |

### Examples
```
feat(auth): add OAuth2 login flow
fix(payment): correct tax calculation
refactor(database): migrate to connection pooling
perf(api): add caching layer
test(checkout): add integration tests
docs(readme): update installation steps
chore(deps): upgrade Spring Boot to 3.2.0
```

---

## 🛠️ Useful Git Commands

### Before Committing
```bash
# View staged files
git diff --name-only --cached

# View staged changes
git diff --cached

# View specific file changes
git diff --cached <file>
```

### After Committing
```bash
# View recent commits
git log --oneline -10

# View commit details
git show <commit-hash>

# View commit with files
git log -1 --name-status
```

### Reorganizing Commits
```bash
# Soft reset (keep changes)
git reset --soft HEAD~1

# Unstage all
git reset HEAD

# Stage specific files
git add <files>

# Verify staged files
git diff --cached --name-only
```

---

## 📁 File Locations

All prompts are in `.github/prompts/`:

- **README.md** - Full documentation
- **conventional-commit-single.md** - Single commit prompt (detailed)
- **conventional-commit-by-module.md** - Split by module prompt (detailed)
- **commit-by-module-quick.md** - Split by module prompt (concise)
- **QUICK_REFERENCE.md** - This file

---

## 💡 Pro Tips

### When to Split Commits
- ✅ Multiple modules affected
- ✅ Mix of features and fixes
- ✅ Breaking + non-breaking changes
- ✅ Large, hard-to-review commit

### When to Keep Single Commit
- ✅ Single module affected
- ✅ Focused, atomic change
- ✅ All changes are related
- ✅ Easy to review

### Good Commit Practices
- ✅ One logical change per commit
- ✅ All tests pass
- ✅ Code is functional
- ✅ Clear, descriptive messages
- ❌ Don't mix unrelated changes
- ❌ Don't commit broken code

---

## 🎓 Learning Resources

- [Conventional Commits](https://www.conventionalcommits.org/)
- [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/)
- [Git Best Practices](https://git-scm.com/book/en/v2)

---

## 🚀 Quick Workflow

### Workflow 1: Normal Commit
```bash
# 1. Make changes
# 2. Stage changes
git add <files>

# 3. Use agent prompt
"Inspect staged changes, craft a clear Conventional Commit subject, 
generate a concise structured body, commit the changes, and print the result."

# 4. Verify
git log -1
```

### Workflow 2: Split Commit
```bash
# 1. Make changes and commit (oops, too big!)
git add .
git commit -m "big commit"

# 2. Use agent prompt
"Perform a soft reset of the most recent commit and reorganize all changes 
into multiple separate commits grouped by module/domain, following 
Conventional Commit format with structured bodies."

# 3. Verify
git log --oneline -10
git status
```

---

## 📞 Need Help?

1. Check **README.md** for detailed documentation
2. Check specific prompt files for examples
3. Use the quick prompts above
4. Customize prompts for your needs

---

**Last Updated:** 2025-10-27

