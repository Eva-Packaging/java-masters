# Coding Standards (Required)

These standards keep your code readable and reviewable.

## 1) General rules
- Use meaningful names
- Keep methods small (prefer under 40 lines)
- Avoid deep nesting
- Prefer early returns for validation

## 2) Spring layering
- Controller: HTTP only
- Service: business logic
- Repository: database access only

## 3) Dependency injection
- Prefer constructor injection
- Avoid field injection unless assignment requires it

## 4) Error handling
- Use a global exception handler
- Return consistent error responses

## 5) Logging
- Use slf4j logging (no System.out.println)
- Log important events (create/update/revoke/etc.)

## 6) Testing
- Test business rules in the service layer
- Keep tests simple and readable
