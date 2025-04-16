# Instaleague

An example single page application (SPA) using [Instant](https://instantdb.com) with
ClojureScript/[Replicant](https://replicant.fun).

## Objective of the application

The app is used for creating ad hoc leagues for games, e.g. table tennis or pool. The app will
generate the fixtures for a set of players, with or without return matches, and the players can
themselves register scores and results, in a multiplayer fashion, allowing everybody to follow the
live game and league results.

## Architecture

Persistent data is stored in Instant, and transient state is stored in an atom (can we utilize the
Instant for transient state as well? See questions below).

The app consists of [pages](./src/instaleague/page.cljs) that each define the following properties
- `:id` a unique identifier of the page, e.g. `:players`
- `:route` the route template in the form of a vector of path components and parameters, e.g.
`["players", :player/id]` which maps to `/players/42`
- `:query` a function that takes the matched route parameters and the app state and return an
[instaql](https://www.instantdb.com/docs/instaql) query
- `:render` a function that takes the query result, either `:loading`, `:error` or the returned
data, along with the route parameters and app state

## Questions

- Can Instant also serve as a local and transient database? Or can we create a separate database
  using the underlying triple store in Instant, utilizing the same query mechanisms?
- Is it possible to provide queries to Instant as data, instead of the opaque structures that are
  provided via the `tx` proxies?
- Is it possible to query the database using Datalog queries?

## Prerequisites

- nodejs / npm

## Technologies

- **Instant** https://instantdb.com
- **Replicant** https://replicant.fun
- **reitit** https://github.com/metosin/reitit
- **shadow-cljs** https://github.com/thheller/shadow-cljs
- **tailwindcss** https://tailwindcss.com/
- **daisyUI** https://daisyui.com/

## Getting started

Setup a database in [Instant](https://www.instantdb.com/dash) and add the app-id to `.env`:

```
APP_ID=<your-app-id>
```

Start the app:

- run `npm start`
- open the browser at http://localhost:9090
- connect to the repl from your editor of choice

## Using Emacs

To be able to connect to the running repl, make sure that `cider-nrepl` and `refactor-nrepl` is
present in your local `~/.shadow-cljs/config.edn` (otherwise
[Cider](https://github.com/clojure-emacs/cider) will not function properly):

```
{:dependencies [[cider/cider-nrepl "0.55.0"]
                [refactor-nrepl/refactor-nrepl "3.10.0"]]}
```

(Use versions compatible with your installed version of Cider)

Then in Emacs (with Clojure and Cider set up) run `M-x cider-connect-cljs`.

Note: you can instead do `M-x cider-jack-in-cljs` and Cider will automatically add the
aforementioned dependencies. Just make sure to run `npm watch:css` instead of `npm start`.

## Using IntelliJ / Cursive

Open `deps.edn` using File / Open... and choose to open as a project. Next, add a run configuration:

- Type: Clojure REPL / Remote
- Name: whatever, e.g. "Remote REPL"
- Connection type: nREPL
- Use port from localhost
- Click "Run"

If you get the error "Cannot find port from REPL port file", then that means that you don't have a
running build yet. Start it with `npm start`.

The repl will be a `clj` repl (to the server that runs the cljs build). You can change to the `cljs`
repl by evaluating the following:

```clj
(shadow.cljs.devtools.api/repl :app)
```

(or just `(cljs-repl)` which is defined in `dev/shadow/user.clj`)

Try to evaluate something in the repl, e.g. `(+ 1 2)`. If you get `No available JS runtime.`, then
it just means that you haven't opened the app in the browser yet.

## General tips

Anywhere in the cljs files you can call `(tap> whatever)` and have the value of `whatever` printed
as a navigable structure in the shadow-cljs inspector at http://localhost:9630/inspect.
