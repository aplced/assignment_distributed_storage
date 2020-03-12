# Via Key Value Service

## Tasks

### Task 1
Implement Key-Value store with web-based API with the following end-points (for simplicity use HTTP GET for all endpoints):

1. `/set?k={k}&v={v}`
    - Set key `k` with value `v`
    - `k` is a string not bigger than 64 chars
    - `v` is a string not bigger than 256 chars
1. `/get?k={k}`
    - Gets value with key `k`
    - Should return 404 if the key is missing
1. `/rm?k={k}`
    - Removes key `k`
    - If the key is missing should return an error
1. `/clear`
    - Removes all keys and values
1. `/is?k={k}`
    - Returns HTTP 200 if the key exists
    - Returns HTTP 404 if the key is missing 

### Tasks 2

1. `/getKeys` 
    - Should return all the keys in the store
2. `/getValues`
    - Should return all the values in the store
3. `/getAll`
    - Should return all pairs of key and value
4. Try to implement the above requests for very big stores
    - What will be the performance for millions of keys and values?

### Task 3
1. Use a database for storing the keys and values. Could be CockroachDB or MongoDB. 
*NB: Provide docker-compose or another single-command to start the solution*

### Task 4 (hard) - Distributed KV Store
For the following task the store should be in-memory per service. No external storage is allowed.
1. Implement a mesh/cluster of KV services that are consistant and everyone has all the keys and values all the time
    - Implement a new end-point for the server to join a mesh of servers.
    - Implement a feature of removing a node from the mesh
2. How you are going solve merge conflicts in distributed transaction?
    - Propose a solution
3. Implement multi active behavior for all nodes in the cluster.
    - Having a load balancer in front of the cluster should work as expected.
4. Implement stress test and testing service

## Acceptable languages
Go, Node.js, Java, Python, C++.
Preferred language: Go

## Criteria

The following are taken into consideration
 * Clean code
 * Proper comments in code (quality, not quantity)
 * Architecture
 * Documentation
 * Tests
 * Linters, CI configuration, .gitignore, test coverage reporting, etc are a bonus
 * Single command to start the solution
 * Single commands and/or documentation for other functionality (e.g. running tests, running linter)
 
