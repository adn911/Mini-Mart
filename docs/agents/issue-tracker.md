# Issue tracker: Local Markdown

Issues and PRDs for this repo live as markdown files in `.scratch/`.

## Conventions

- One feature per directory: `.scratch/<feature-slug>/`
- The PRD is `.scratch/<feature-slug>/PRD.md`
- Implementation issues are `.scratch/<feature-slug>/issues/<NN>-<slug>.md`, numbered from `01`
- Triage state is recorded as a `Status:` line near the top of each issue file
- Comments and conversation history append to the bottom of the file under a `## Comments` heading

## When a skill says "publish to the issue tracker"

Create a new file under `.scratch/<feature-slug>/`, creating the directory if needed.

## When a skill says "fetch the relevant ticket"

Read the file at the referenced path. The user will normally pass the path or the issue number directly.

## Wayfinding operations

Used by `/wayfinder`. The map is a file with one child file per ticket.

- Map: `.scratch/<effort>/map.md` — the notes, decisions-so-far, and fog body.
- Child ticket: `.scratch/<effort>/issues/NN-<slug>.md`, numbered from `01`, with the question in the body.
- A `Type:` line records the ticket type: `research`, `prototype`, `grilling`, or `task`.
- A `Status:` line records `claimed` or `resolved`.
- Blocking is recorded with a `Blocked by: NN, NN` line near the top.
- Frontier means scanning `.scratch/<effort>/issues/` for open, unblocked, and unclaimed files; first by number wins.
- Claim means setting `Status: claimed` and saving before work starts.
- Resolve means appending the answer under an `## Answer` heading, setting `Status: resolved`, then appending a context pointer to the map's decisions-so-far.
