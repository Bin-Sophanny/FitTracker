# Optional: Dependency Cleanup

## Current Dependencies

The `package.json` still includes the `web3` dependency which was used for blockchain functionality.

### Dependencies to Consider Removing

**`web3` (^4.0.2)**
- Status: **NOT USED** (blockchain service removed)
- Action: Can be safely removed
- Command: `npm uninstall web3`

### All Current Dependencies (In Use)

✅ **axios** - HTTP client for inter-service communication
✅ **bcryptjs** - Password hashing for user authentication
✅ **cors** - Cross-origin resource sharing middleware
✅ **dotenv** - Environment variable management
✅ **express** - Web framework
✅ **express-http-proxy** - API Gateway service proxying
✅ **firebase-admin** - Firebase authentication integration
✅ **jsonwebtoken** - JWT token generation and verification
✅ **mongoose** - MongoDB object modeling
✅ **morgan** - HTTP request logging middleware

### Unused Dependencies (Optional Cleanup)

⚠️ **web3** - Blockchain library (UNUSED - blockchain removed)
⚠️ **jest** - Testing framework (not currently used, but configured)

## Recommendation

If you want to reduce bundle size for production:

```bash
# Optional: Remove web3 dependency
npm uninstall web3

# Optional: Remove jest if not using tests
npm uninstall jest
```

This would reduce:
- Installation time
- Bundle size
- Potential security vulnerabilities from unused packages

## Decision

For now, keeping these packages is **low risk** and they don't impact production performance significantly.

If you decide to remove them:
1. Ensure no code references them
2. Test locally first
3. Update package-lock.json
4. Push changes to repository

## Current Status

✅ Backend is production-ready with current dependencies
⚠️ Optional cleanup available if needed for optimization
