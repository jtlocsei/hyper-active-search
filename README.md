# hyper-active-search

Minimal Hyper demo showing two live-search patterns on the same page:

- local-only draft signal plus `$value` handoff
- synced draft signal read inside the action
- `data-on:input__debounce.30ms`
- `data-ignore-morph`
- wider search inputs for easier visual comparison
- URL-backed committed state via query params `local-q` and `synced-q`
- stochastic `1200-3000ms` simulated server latency for debounced apply actions

The page searches a small hardcoded list of names. It is intentionally plain and unstyled so the debounce and morph behavior are easy to observe.

## Run

```bash
clojure -M:run
```

The app listens on `PORT` when it is set, otherwise it defaults to `8080`.

Then open <http://localhost:8080>.

## Uberjar

Build the standalone jar:

```bash
clojure -T:build uber
```

Run it:

```bash
java -jar target/hyper-active-search-standalone.jar
```

If you need a specific port:

```bash
PORT=4000 java -jar target/hyper-active-search-standalone.jar
```

## What To Try

- Type in both demos and compare how the local-only and synced draft strategies behave while apply responses come back with random jitter.
- Open the page with `?local-q=ada&synced-q=grace` and confirm each input is seeded from its own committed URL state.
- Click `Clear` in each demo and confirm the input clears immediately while the committed query is removed without waiting for the debounced delay.
