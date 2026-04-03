# hyper-active-search

Minimal Hyper demo showing live search with:

- `data-on:input__debounce.180ms`
- `data-ignore-morph`
- URL-backed search state via query param `q`
- deterministic simulated server latency

The page searches a small hardcoded list of names. It is intentionally plain and unstyled so the debounce and morph behavior are easy to observe.

## Run

```bash
clojure -M:run
```

Then open <http://localhost:4000>.

## Test

```bash
clojure -M:test
```

## What To Try

- Type quickly and confirm characters are not dropped while the server response is delayed.
- Pause briefly and watch the results update after the simulated jitter.
- Click `Clear` and confirm the input clears immediately before the server response removes `q` from the URL.
