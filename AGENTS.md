## Project

OpenMAIC (Open Multi-Agent Interactive Classroom) — a Next.js 16 / React 19 app that turns a topic or document into an AI-driven multi-agent classroom (slides, quizzes, interactive HTML scenes, PBL), with TTS, whiteboard, and export to `.pptx`/`.html`.

## Package manager & runtime

- pnpm (>=10) is required; Node >=20.9.
- `pnpm install` runs a `postinstall` that builds the two workspace packages in `packages/` (`mathml2omml`, `pptxgenjs`). If types from those packages look missing, rerun `pnpm install`.

## Common commands

```bash
pnpm dev                   # Next dev server
pnpm build && pnpm start   # Production build
pnpm lint                  # eslint (use --fix to autofix)
pnpm check                 # prettier --check (pnpm format to write)
pnpm test                  # vitest (tests/**/*.test.ts)
pnpm test -- <path>        # run a single vitest file
pnpm test:e2e              # Playwright (spawns dev server on :3002)
pnpm test:e2e:ui           # Playwright UI
pnpm check:i18n-keys       # validates locale key parity
npx tsc --noEmit           # type check (no build script for this)
pnpm eval:whiteboard       # eval harness (eval/whiteboard-layout)
pnpm eval:outline-language # eval harness (eval/outline-language)
```

Pre-PR checklist (from CONTRIBUTING.md): `pnpm format`, `pnpm lint --fix`, `npx tsc --noEmit`. Commits follow Conventional Commits (`feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `ci`, `perf`, `style`).

## Architecture

The app is a Next.js App Router project. Three layers matter most:

1. **Generation pipeline** (`lib/generation/`) — turns a user prompt + attachments into a classroom. `generation-pipeline.ts` / `pipeline-runner.ts` orchestrate: outline → scene list → per-scene builders. Prompts live in `lib/generation/prompts/`. `interactive-post-processor.ts` rewrites generated HTML for interactive scenes. Scene shapes and pipeline contracts are in `pipeline-types.ts`.

2. **Runtime orchestration** (`lib/orchestration/`) — drives the *live* classroom (AI teacher + AI classmate agents, TTS, whiteboard actions, discussion). `director-graph.ts` is a LangGraph (`@langchain/langgraph`) state machine; `director-prompt.ts` + `prompt-builder.ts` assemble the system prompt; `tool-schemas.ts` defines the actions the director can emit (speak, draw, advance, etc.); `registry/` holds per-scene-type handlers. `ai-sdk-adapter.ts` bridges LangGraph to the Vercel AI SDK streaming response.

3. **Providers** (`lib/ai/providers.ts`, `lib/ai/llm.ts`) — unified LLM provider layer over `@ai-sdk/openai`, `@ai-sdk/anthropic`, `@ai-sdk/google`, plus DeepSeek/MiniMax/Grok/Doubao/GLM/Ollama/any OpenAI-compatible endpoint. Configuration comes from env vars **or** `server-providers.yml`. Separate provider pools exist for TTS, image, video, and PDF parsing (see `app/api/verify-*` routes).

API routes under `app/api/` are the server boundary. The important ones: `generate/` and `generate-classroom/` (pipeline entry), `chat/` (director streaming), `classroom/` (classroom persistence), `classroom-media/` and `proxy-media/` (assets), `parse-pdf/`, `transcription/`, `web-search/`, `access-code/` (auth gate, see `components/access-code-guard.tsx` and `middleware.ts`).

Client state is Zustand (`lib/store/`): `stage.ts` (active classroom/scene), `settings.ts` (+`settings-validation.ts`), `canvas.ts` + `whiteboard-history.ts`, `snapshot.ts` (classroom persistence via Dexie/IndexedDB in `lib/storage/`).

Rendering: scenes render through `components/scene-renderers/` and `components/slide-renderer/`; interactive scenes run in sandboxed iframes (`lib/store/widget-iframe.ts`). The whiteboard uses ProseMirror (`lib/prosemirror/`) for text + a custom canvas layer. Exports: `.pptx` via the workspace `pptxgenjs` package + `lib/export/`; math rendering uses KaTeX/Temml and `mathml2omml` (workspace package) for Office-compatible formulas.

Workspace packages in `packages/` are consumed as `workspace:*`. Edit their source and rerun `pnpm install` (or their own `npm run build`) to propagate.

## i18n

All user-facing strings must be internationalized. Locales live under `lib/i18n/locales/` with `react-i18next`; see `lib/i18n/TRANSLATION_GUIDE.md`. Run `pnpm check:i18n-keys` to verify key parity across languages before sending a PR.

## Testing notes

- Vitest unit tests are in `tests/` mirroring `lib/` (e.g. `tests/ai/`, `tests/export/`, `tests/server/`). `tests/setup-env.ts` injects test env.
- Playwright specs are in `e2e/tests/` and run against port 3002. CI builds first; local runs reuse `pnpm dev`.
- Eval harnesses under `eval/` are standalone `tsx` runners — not part of `pnpm test`.

## Other

- `android/` is a separate Samsung-tablet kiosk shell; ignore unless working on mobile/kiosk.
- `middleware.ts` enforces the access-code gate when `ACCESS_CODE` is set.
- `docker-compose.yml` + `Dockerfile` provide a self-host path.

