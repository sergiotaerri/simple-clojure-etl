# clojure-simple-etl

## About

The project is a data-driven [ETL](https://en.wikipedia.org/wiki/Extract,_transform,_load) script done with clojure, to use it, one should populate the `queries` hashmap with the adequate information for each destiny table to be conceived. The information needed consists:

- `:src` An SQLvec containing the necessary data for the the transform and load steps.
- `:sg-nk-keys` An array of strings containing the they keys that should differentiate rows in the _target_ table
- `:transform-fns` An array of functions to transform the data (transaction table-map-entry data-to-be-processed) -> (processed data)

Each key pair proceeds to be executed in the order the _tables(hash-map keys)_ are declared, where each represent a destination table.

This project also has docker-compose utilities to bring up a development source and destination postgresql database.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed and a postgres server OR
You will need [docker][2] installed.

[1]: https://github.com/technomancy/leiningen
[2]: https://github.com/docker

## Running locally for development

1. The folder `docker/` contains 2 shell script where you should place a initiation script for the databases
2. Running `make src-b target-b` should bring up the development database containers
3. Then bringing up a repl normally with `lein repl` or your editor.
