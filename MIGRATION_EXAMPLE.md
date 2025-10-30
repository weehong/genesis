# Migration Example: DepartmentController Security Annotations

This document shows how to migrate from the rigid `BranchAccessChecker` to the flexible `ResourceAccessChecker`.

## Current Implementation (Rigid)

```java
@RestController
@RequestMapping(value = "/api/v1/branches/{branchId}/departments")
public class DepartmentController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @branchAccessChecker.hasAccess(authentication, #branchId)")
    public Page<DepartmentResponse> findAllByBranchId(
        @PathVariable String branchId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        // Implementation...
    }

    @GetMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @branchAccessChecker.hasAccess(authentication, #branchId)")
    public DepartmentResponse findById(@PathVariable String branchId, @PathVariable Long id) {
        // Implementation...
    }
}
```

## New Implementation (Flexible)

### Option 1: Using Branch Access (Backward Compatible)
```java
@RestController
@RequestMapping(value = "/api/v1/branches/{branchId}/departments")
public class DepartmentController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @resourceAccessChecker.hasBranchAccess(authentication, #branchId)")
    public Page<DepartmentResponse> findAllByBranchId(
        @PathVariable String branchId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        // Implementation...
    }

    @GetMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).DEPARTMENT, #id)")
    public DepartmentResponse findById(@PathVariable String branchId, @PathVariable Long id) {
        // Implementation...
    }
}
```

### Option 2: Using Generic Resource Access (Most Flexible)
```java
@RestController
@RequestMapping(value = "/api/v1/branches/{branchId}/departments")
public class DepartmentController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).BRANCH, #branchId)")
    public Page<DepartmentResponse> findAllByBranchId(
        @PathVariable String branchId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {
        // Implementation...
    }

    @GetMapping(value = "/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).DEPARTMENT, #id)")
    public DepartmentResponse findById(@PathVariable String branchId, @PathVariable Long id) {
        // Implementation...
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).BRANCH, #branchId, T(com.resetrix.horaion.shared.enums.AccessMode).WRITE)")
    public DepartmentResponse create(
        @PathVariable String branchId,
        @Valid @RequestBody DepartmentRequest request) {
        // Implementation...
    }

    @DeleteMapping("/{id:[0-9]+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).DEPARTMENT, #id, T(com.resetrix.horaion.shared.enums.AccessMode).DELETE)")
    public void deleteById(
        @PathVariable String branchId,
        @PathVariable Long id,
        @RequestParam(defaultValue = "false") boolean soft) {
        // Implementation...
    }
}
```

## Future Module Examples

### Employee Controller (New Module)
```java
@RestController
@RequestMapping(value = "/api/v1/departments/{departmentId}/employees")
public class EmployeeController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).DEPARTMENT, #departmentId)")
    public Page<EmployeeResponse> findByDepartment(@PathVariable String departmentId) {
        // Implementation...
    }

    @GetMapping("/{employeeId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).EMPLOYEE, #employeeId)")
    public EmployeeResponse findById(@PathVariable String employeeId) {
        // Implementation...
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).DEPARTMENT, #departmentId, T(com.resetrix.horaion.shared.enums.AccessMode).WRITE)")
    public EmployeeResponse create(
        @PathVariable String departmentId,
        @Valid @RequestBody EmployeeRequest request) {
        // Implementation...
    }
}
```

### Payroll Controller (New Module)
```java
@RestController
@RequestMapping(value = "/api/v1/departments/{departmentId}/payroll")
public class PayrollController {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'SYSTEM_OWNER', 'PRIVILEGED_SYSTEM_USER', 'PAYROLL_USER') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).DEPARTMENT, #departmentId)")
    public Page<PayrollResponse> findByDepartment(@PathVariable String departmentId) {
        // Implementation...
    }

    @PostMapping("/process")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'PAYROLL_ADMIN') and @resourceAccessChecker.hasAccess(authentication, T(com.resetrix.horaion.shared.enums.ResourceType).DEPARTMENT, #departmentId, T(com.resetrix.horaion.shared.enums.AccessMode).ADMIN)")
    public PayrollProcessResponse processPayroll(@PathVariable String departmentId) {
        // Implementation...
    }
}
```

## Migration Strategy

### Phase 1: Parallel Implementation
1. Keep existing `@branchAccessChecker` annotations working
2. Add new `ResourceAccessChecker` service alongside
3. Test new service with development flag enabled

### Phase 2: Gradual Migration
1. Update one controller at a time
2. Use convenience methods first (`hasBranchAccess`, `hasDepartmentAccess`)
3. Gradually move to generic `hasAccess` method

### Phase 3: New Modules
1. All new modules use `ResourceAccessChecker` from the start
2. Add new resource types to `ResourceType` enum
3. Extend `ResourceHierarchyService` for new resource types

### Phase 4: Cleanup
1. Remove old `BranchAccessChecker` when no longer used
2. Remove backward compatibility code
3. Optimize and refine access patterns

## Benefits of Migration

### ✅ Consistency
- Same security pattern across all modules
- Consistent access control logic

### ✅ Flexibility
- Support for different access modes (READ, WRITE, DELETE, ADMIN)
- Easy to add new resource types

### ✅ Hierarchical Validation
- Automatic parent resource validation
- Department access validates branch and company access

### ✅ Extensibility
- Easy to add new modules
- Configurable access patterns

### ✅ Maintainability
- Single service to maintain
- Clear separation of concerns
