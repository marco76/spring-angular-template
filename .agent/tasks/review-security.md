# Review Security

Use this checklist when changing auth, feature flags, data access, or APIs.

## Checks

- Are protected endpoints authenticated?
- Are authorization decisions enforced in Spring, not only Angular?
- Are feature flags enforced in the backend when behavior is protected?
- Are secrets absent from committed files?
- Are logs free of tokens, passwords, and sensitive personal data?
- Are DTOs exposing only intended fields?
- Are validation rules present for incoming requests?
- Are CORS and proxy settings appropriate for the environment?

## Done Means

- The backend owns the security decision.
- The frontend improves UX but is not the only guard.

