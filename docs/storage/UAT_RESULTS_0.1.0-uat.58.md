# UAT.58 - Terminal input and layout

## Changes

- Screen focus and text-field focus now change together. Clicking inventory slots returns typing to search; Tab switches between search and quantity.
- Terminal key presses stay in text input. Escape closes the terminal, or returns from the crafting preview.
- Removed the heading overlapping the first storage row. Moved capacity counters away from pagination and constrained their text width.
- Vertically aligned search and quantity text within their controls.
- Crafting preview has numbered output steps, separated from its heading. Hovering a step lists its input quantities.

## Verification

- Storage unit tests: 13 passed, no failures or errors.
- Client regression test covers inventory hotkey, quantity 120, Tab, lost focus, text selection, and crafting-preview dismissal with JEI installed.
- The barrel preview is a client-side rendering fixture, not an additional end-to-end crafting assertion.
- Fabric TestInput sends key events without modifiers; the selection test supplies an explicit Ctrl+A event.
- Storage client regression test: passed with JEI 30.28.0.193.
- Terminal and 120-barrel preview screenshots inspected at 854x480; the corrected rows, text fields, counters and preview heading do not overlap. Other GUI scales and the full live modpack still need user acceptance testing.
- Machines and Integration server game tests passed during bundle validation.
- All 53 Storage server game tests and the Autocrafter client test passed.
- Packaging reuses the server/client checks already completed in this session; the final Storage client check runs against the final focus implementation.

## Installation

All four shipped modules use 0.1.0-uat.58. Install the matching set together.
The live Modrinth profile was running during development; no live JARs were replaced.
