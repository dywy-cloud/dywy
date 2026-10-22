# Guest magic-link authentication — operational & security constraints

This document summarizes the security controls behind the guest-safe authentication flow
(issue #103) so operators and contributors understand the guarantees and their limits.

## Flow overview

1. A guest opens the invitation page from the QR code and selects their identity.
2. They request a magic link (`POST /api/guest-access/invitations/{invitationAccessToken}/guests/{guestId}/magic-link`).
3. The backend validates the invitation + guest relationship and emails a link containing an opaque token.
4. The guest opens the link (`GET /api/guest-access/magic-links/{token}`); on success a short-lived
   guest **session** cookie is issued and the browser is redirected to the secured area.
5. The session authorizes RSVP/choices mutations, scoped to that invitation + guest.

## Tokens

- **Opaque & high-entropy**: the magic-link token is a random UUID (~122 bits). It carries no
  guessable structure and is never derived from guest data.
- **Hashed at rest**: only a SHA-256 digest of the token is stored (`guest_magic_link_token.token_hash`).
  A database leak therefore never exposes usable bearer tokens. A plain (unsalted) SHA-256 is sufficient
  because the token is high-entropy and not brute-forceable — unlike passwords it does not need a slow,
  salted hash. Lookups hash the incoming token and compare digests.
- **Short-lived**: TTL is configurable via `app.guest-access.magic-link-ttl-seconds` (default 900s / 15 min).
- **One-time use**: consumption atomically sets `used_at` under a `used_at IS NULL` guard; a reused token
  fails. Only one active (unused) token exists per guest — requesting a new link invalidates the previous one.

## Session

- Issued as an HttpOnly cookie (`guest_session`, a JWT), so it is not readable by JavaScript (XSS-safe).
- `Secure` and `SameSite` are configurable (`app.guest-access.session-cookie-secure`,
  `app.guest-access.session-cookie-same-site`); TTL via `app.guest-access.guest-session-ttl-seconds`
  (default 1800s / 30 min).
- Validated server-side on every `/api/guest-access/secured/**` request. `401` is returned for an
  unauthenticated request and `403` when the session is valid but the guest no longer belongs to the invitation.

## Mode (individual or group)

- The request body is optional: `{"mode": "INDIVIDUAL" | "GROUP"}`. A missing body or missing `mode` means
  `INDIVIDUAL`, exactly as before. An unknown value returns `400`.
- The mode is stored on the token (`guest_magic_link_token.mode`, default `INDIVIDUAL`). A group email also
  states that the link lets the recipient answer for the whole party.
- On a successful verification, a `GROUP` token redirects to the secured area with `?mode=group`; an
  `INDIVIDUAL` token redirects without it.
- **The redirect is a UX hint only.** The guest session is identical in both modes (`guestId` +
  `invitationId`) and authorization never depends on the `mode` query parameter.
- The `202` response stays non-enumerating in both modes.

## Abuse & enumeration controls

- **Rate limiting**: magic-link requests are throttled per client via the shared rate limiter
  (`app.auth.rate-limit.*`).
- **Non-enumerating responses**: the request endpoint always answers `202 Accepted` regardless of whether
  the invitation/guest exists or the email was sent, so it cannot be used to probe guest existence.
  A failed verification redirects generically to the guest area with `?linkStatus=invalid` (no detail leaked).

## Audit logging

Outcomes are logged without PII (only `invitationId` / `guestId` UUIDs — never email or name):

- Request: accepted, rejected (invitation not found / guest not in invitation), delivery failed.
- Verification: verified, invalid/expired/used token, invitation not found, guest not in invitation.
