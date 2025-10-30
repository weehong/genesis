# Hierarchical Access Control System

## Overview

This document describes the new flexible, hierarchical access control system that replaces the rigid `BranchAccessChecker`. The new system supports the organizational hierarchy: **Company > Branch > Department > Various Modules** and can easily scale as new modules are added.

## 🎯 Problem Solved

### Before (Rigid)
- ❌ `BranchAccessChecker` only handled branch-level access
- ❌ Hard-coded logic, not reusable for other modules
- ❌ Couldn't handle the full organizational hierarchy
- ❌ Difficult to add new modules like Employee, Payroll, Inventory

### After (Flexible)
- ✅ `ResourceAccessChecker` handles any resource type
- ✅ Hierarchical validation (child access requires parent access)
- ✅ Easy to add new modules by adding one line to `ResourceType` enum
- ✅ Configurable access patterns per resource type
- ✅ Support for different access modes (READ, WRITE, DELETE, ADMIN)

## 🏗️ Architecture

### Core Components

1. **`ResourceType` Enum** - Defines all resource types and their hierarchy
2. **`AccessMode` Enum** - Defines access levels (READ, WRITE, DELETE, ADMIN)
3. **`ResourceIdentifier`** - Represents a unique resource with type and ID
4. **`ResourceHierarchyService`** - Resolves hierarchy paths and validates relationships
5. **`ResourceAccessChecker`** - Main access control service
6. **`AccessControlProperty`** - Configuration for access patterns

### Hierarchy Structure

```
Company (Level 1)
  └── Branch (Level 2) 
      └── Department (Level 3)
          ├── Employee (Level 4)
          ├── Payroll (Level 4)
          ├── Inventory (Level 4)
          ├── Schedule (Level 4)
          ├── Timesheet (Level 4)
          ├── Asset (Level 4)
          └── [Future Modules] (Level 4+)
```

## 🚀 Usage Examples

### Basic Access Control
```java
// Check if user can read a department
@PreAuthorize("@resourceAccessChecker.hasDepartmentAccess(authentication, #departmentId)")

// Check if user can write to a branch
@PreAuthorize("@resourceAccessChecker.hasAccess(authentication, T(ResourceType).BRANCH, #branchId, T(AccessMode).WRITE)")

// Check if user can delete an employee
@PreAuthorize("@resourceAccessChecker.hasAccess(authentication, T(ResourceType).EMPLOYEE, #employeeId, T(AccessMode).DELETE)")
```

### Controller Examples
```java
@RestController
@RequestMapping("/api/v1/departments/{departmentId}/employees")
public class EmployeeController {
    
    @GetMapping
    @PreAuthorize("@resourceAccessChecker.hasDepartmentAccess(authentication, #departmentId)")
    public List<Employee> findByDepartment(@PathVariable String departmentId) { ... }
    
    @PostMapping
    @PreAuthorize("@resourceAccessChecker.hasAccess(authentication, T(ResourceType).DEPARTMENT, #departmentId, T(AccessMode).WRITE)")
    public Employee create(@PathVariable String departmentId, @RequestBody EmployeeRequest request) { ... }
}
```

## 🔧 Adding New Modules

Adding a new module is incredibly simple:

### Step 1: Add to ResourceType Enum
```java
// Just add one line!
TIMESHEET(4, DEPARTMENT, true),
```

### Step 2: Use in Controller
```java
@RestController
@RequestMapping("/api/v1/departments/{departmentId}/timesheets")
public class TimesheetController {
    
    @GetMapping("/{timesheetId}")
    @PreAuthorize("@resourceAccessChecker.hasAccess(authentication, T(ResourceType).TIMESHEET, #timesheetId)")
    public Timesheet findById(@PathVariable String timesheetId) { ... }
}
```

### Step 3: Configure Access Patterns (Optional)
```yaml
app:
  access-control:
    resource-patterns:
      TIMESHEET:
        admin-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN]
        write-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN, USER]  # Users can edit their own
        read-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN, MANAGER, USER]
        delete-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN]
```

## ⚙️ Configuration

### Application Properties
```yaml
app:
  access-control:
    development-mode: false  # NEVER true in production!
    cache-enabled: true
    cache-ttl-seconds: 300
    default-access-mode: READ
    resource-patterns:
      EMPLOYEE:
        admin-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN]
        write-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN, HR_USER]
        read-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN, HR_USER, MANAGER]
        delete-roles: [SYSTEM_ADMINISTRATOR, HR_ADMIN]
```

## 🔒 Security Implementation Status

### ⚠️ CRITICAL: Company Access Validation Required

The system currently uses **secure defaults** (deny all non-admin access) until company membership validation is implemented.

**Implementation needed in `ResourceAccessChecker.validateUserCompanyAccess()`:**

#### Option 1: JWT Claims (Recommended)
```java
JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
List<String> userCompanies = jwtToken.getToken().getClaimAsStringList("company_ids");
return userCompanies.contains(companyId);
```

#### Option 2: Database Lookup
```java
return userCompanyService.hasAccessToCompany(username, Long.valueOf(companyId));
```

#### Option 3: Role-based
```java
String requiredRole = "COMPANY_" + companyId + "_USER";
return hasRole(authentication, requiredRole);
```

## 🔄 Migration Strategy

### Phase 1: Parallel Implementation
- ✅ New system created alongside existing `BranchAccessChecker`
- ✅ All components implemented and ready
- 🔄 Test with development flag enabled

### Phase 2: Gradual Migration
- 🔄 Update existing controllers one by one
- 🔄 Start with convenience methods (`hasBranchAccess`, `hasDepartmentAccess`)
- 🔄 Move to generic `hasAccess` method

### Phase 3: New Modules
- 🔄 All new modules use `ResourceAccessChecker` from day one
- 🔄 Add resource types to enum as needed
- 🔄 Extend `ResourceHierarchyService` for complex hierarchies

### Phase 4: Cleanup
- 🔄 Remove old `BranchAccessChecker`
- 🔄 Remove backward compatibility code
- 🔄 Optimize performance

## 📁 Files Created

### Core System
- `src/main/java/com/resetrix/horaion/shared/enums/ResourceType.java`
- `src/main/java/com/resetrix/horaion/shared/enums/AccessMode.java`
- `src/main/java/com/resetrix/horaion/shared/models/ResourceIdentifier.java`
- `src/main/java/com/resetrix/horaion/shared/services/ResourceHierarchyService.java`
- `src/main/java/com/resetrix/horaion/shared/services/ResourceAccessChecker.java`

### Configuration
- `src/main/java/com/resetrix/horaion/shared/properties/AccessControlProperty.java`
- `src/main/resources/application-access-control-example.yaml`

### Documentation
- `MIGRATION_EXAMPLE.md` - Shows how to migrate existing controllers
- `HIERARCHICAL_ACCESS_CONTROL_SYSTEM.md` - This comprehensive guide

## 🎉 Benefits

### ✅ Scalability
- Add new modules with one line of code
- Hierarchical validation automatically works

### ✅ Consistency
- Same security pattern across all modules
- Unified access control logic

### ✅ Flexibility
- Different access modes per operation
- Configurable access patterns per resource type

### ✅ Security
- Hierarchical validation prevents privilege escalation
- Secure defaults until proper implementation

### ✅ Maintainability
- Single service to maintain
- Clear separation of concerns
- Comprehensive logging and error handling

## 🚨 Next Steps

1. **Implement company membership validation** in `validateUserCompanyAccess()`
2. **Test the system** with development mode enabled
3. **Migrate one controller** as a proof of concept
4. **Add new modules** using the new system
5. **Gradually migrate** all existing controllers
6. **Remove old `BranchAccessChecker`** when no longer needed

The new hierarchical access control system is ready to use and will scale beautifully as your application grows! 🚀
