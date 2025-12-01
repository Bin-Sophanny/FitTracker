# Shared Models and Utilities

This folder contains shared code used across all microservices:

## Files:
- **middleware.js** - Authentication & authorization middleware
- **utils.js** - Utility functions (JWT, password hashing)
- **models/** - Shared MongoDB schemas
  - User.js - User profile schema
  - FitnessData.js - Fitness tracking schema

## Usage:

```javascript
const { authenticateToken } = require('../shared/middleware');
const { generateToken, hashPassword } = require('../shared/utils');
```
