# Instaleague

A toy app for running ad hoc leagues for sports and games, e.g table
tennis and pool, using [Instant](https://instantdb.com) and
[ClojureScript](https://clojurescript.org/).

## Prerequisites

- nodejs / npm

## Notable tools

- [Instant](https://instantdb.com)
- [Replicant](https://replicant.fun)
- [shadow-cljs](https://github.com/thheller/shadow-cljs)
- [tailwindcss](https://tailwindcss.com/)
- [daisyUI](https://daisyui.com/)

## Getting started

Setup a database in [Instant](https://www.instantdb.com/dash) and add
the app-id to `.env`:

```
APP_ID=<your-app-id>
```

Start the app:

- run `npm start`
- open the browser at http://localhost:9090
- connect to the repl from your editor of choice

## Using Emacs

Make sure that `cider-nrepl` and `refactor-nrepl` is present in your
local `~/.shadow-cljs/config.edn`. This will ensure that
[Cider](https://github.com/clojure-emacs/cider) will function properly
when connecting to the repl instead of jacking in:

```
{:dependencies [[cider/cider-nrepl "0.55.0"]
                [refactor-nrepl/refactor-nrepl "3.10.0"]]}
```

(Use versions compatible with your installed version of Cider)

Then in Emacs (with Clojure and Cider set up) run `M-x
cider-connect-cljs`.

## Using IntelliJ / Cursive

Open `deps.edn` using File / Open... and choose to open as a
project. Next, add a run configuration:

- Type: Clojure REPL / Remote
- Name: whatever, e.g. "Remote REPL"
- Connection type: nREPL
- Use port from localhost
- Click "Run"

If you get the error "Cannot find port from REPL port file", then that
means that you don't have a running build yet. Start it with `npm
start`.

The repl will be a `clj` repl (to the server that runs the cljs
build). You can change to the `cljs` repl by evaluating the following:

```clj
(shadow.cljs.devtools.api/repl :app)
```

(or just `(cljs-repl)` which is defined in `dev/shadow/user.clj`)

Try to evaluate something in the repl, e.g. `(+ 1 2)`. If you get `No
available JS runtime.`, then it just means that you haven't opened the
app in the browser yet.

## General tips

Anywhere in the cljs files you can call `(tap> whatever)` and have the
value of `whatever` printed as a navigable structure in the
shadow-cljs inspector at http://localhost:9630/inspect.
