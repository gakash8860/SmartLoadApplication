# SmartLoad Optimization API

A stateless REST service that selects the optimal combination of shipment orders
for a truck while respecting weight, volume, route, hazmat, and time-window constraints.

The service maximizes total carrier payout using a Dynamic Programming
bitmask approach (n ≤ 22).

---

## Tech Stack
- Java 21
- Spring Boot
- In-memory computation only (no database)
- Docker & Docker Compose

---

## Features
- Maximizes payout (integer cents only)
- Enforces truck capacity (weight & volume)
- Route compatibility (same origin → destination)
- Time-window validation
- Hazmat isolation handling
- Deterministic performance (< 800ms for n=22)
- Stateless with optional in-memory memoization cache

---

## How to Run

```bash
git clone https://github.com/gakash8860/SmartLoadApplication
cd smartload
docker compose up --build
