# Guest area flow

This document describes the end-to-end journey of a wedding guest through the public frontend
(`frontend/public`), from scanning their invitation to submitting their RSVP. It complements
[Guest magic-link authentication](guest-magic-link-security.md) (security details) and
[Deezer integration](deezer-integration.md) (song choice sync).

## Routes (`frontend/public/src/router/index.ts`)

| Path | Name | Purpose |
| --- | --- | --- |
| `/` | `guest-access-home` | Landing page; entry point when no invitation token is present. |
| `/guest-access/:token` | `guest-access-invitation` | Resolves and displays an invitation from its access token (QR code target). |
| `/guest-access/secured-area` | `guest-access-secured-area` | The guest's private area (RSVP + song choice), gated by a magic-link session. |

## Step-by-step flow

1. **Scan the QR code / open the invitation link.** The guest opens
   `/guest-access/:token`. The frontend calls
   `GET /api/guest-access/invitations/{token}` to resolve the invitation and lists the guests
   covered by it. This endpoint is `permitAll` — access is authorized purely by **possession of
   the token**, not by a role.
2. **Select identity & request a magic link.** The guest picks their name and requests a magic
   link: `POST /api/guest-access/invitations/{token}/guests/{guestId}/magic-link-requests`. The
   backend validates the invitation/guest relationship and emails a link containing an opaque,
   one-time token (see [Email delivery](email-delivery.md) for the transport).
   For an invitation with several guests, an **Answer for everyone** button switches the list to
   "choose who receives the link": the request then carries the body `{"mode":"GROUP"}`. Without
   it (single guest, or the button not used) the request has no body and stays individual.
3. **Open the magic link.** `GET /api/guest-access/magic-links/{token}` verifies the token; on
   success it issues a short-lived, HttpOnly `guest_session` cookie and redirects the browser to
   `/guest-access/secured-area`.
4. **Secured area.** `GuestAccessSecuredAreaView` calls `GET /api/guest-access/secured/me` to
   confirm the session and greet the guest. If the session is missing/invalid, the guest is
   prompted to restart from the landing page.
5. **RSVP + song choice.** The guest submits their answer through `GuestRsvpForm`, backed by:
   - `GET /api/guest-access/secured/rsvp` — loads any previously saved answer.
   - `POST /api/guest-access/secured/rsvp` — saves the attendance, and for attending guests a
     mandatory meal plus an optional song.
   - `GET /api/guest-access/secured/song-search?q=...` — Deezer-backed autocomplete used to pick
     the song (see [Deezer integration](deezer-integration.md)).
6. **Answer for everyone (group mode).** When the magic link was requested in `GROUP` mode, the
   redirect to the secured area adds `?mode=group` and `GuestAccessSecuredAreaView` renders
   `GuestGroupRsvpForm` instead of `GuestRsvpForm`. Both forms share the attendance/meal/song
   fieldsets (`GuestRsvpFields`).
   - `GET /api/guest-access/secured/invitation/rsvps` — lists every guest of the session's
     invitation with their first name and their answer (`null` until they answered).
   - A horizontal menu shows one tab per guest with a ✓ (answer complete) or ● (answer to
     complete) marker; selecting a tab shows that guest's fieldsets without losing the others'
     answers.
   - A single submit, enabled once every guest is complete and at least one answer changed,
     calls
     `POST /api/guest-access/secured/invitation/rsvps` and saves all the guests at once (all or
     none); the response is the same list as the `GET`.
   - The mode comes only from the link: a link requested in `GROUP` mode always opens the group
     form, and the individual form has no way to switch to it. `?mode=group` stays in the URL, so
     a refresh keeps the group form. The mode is only a UX hint: the backend authorizes every
     request from the session, never from the query parameter.

All `/api/guest-access/secured/**` calls require the `guest_session` cookie and are scoped to that
guest + invitation; see [Guest magic-link authentication](guest-magic-link-security.md) for the
full security model (token/session lifetimes, rate limiting, audit logging).

