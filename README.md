# Car Park

A small REST service for a car park: vehicles check in, check out and get a bill for the time they stayed.

## Requirements

- Java 25
- No local Maven needed, the project ships with the Maven wrapper

## Running

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. The service starts on `http://localhost:8080`.

## Tests

```bash
./mvnw test
```

## API

### Check occupancy

`GET /parking`

```json
{ "availableSpaces": 1, "occupiedSpaces": 1 }
```

### Check in

`POST /parking`

```bash
curl -X POST http://localhost:8080/parking \
  -H "Content-Type: application/json" \
  -d '{"vehicleReg": "LON 11111", "vehicleType": 1}'
```

| `vehicleType` | Vehicle |
|---|---|
| 1 | small |
| 2 | medium |
| 3 | large |

| Status | When |
|---|---|
| 200 | Vehicle parked, returns `vehicleReg` and `timeIn` |
| 400 | `vehicleReg` is blank or `vehicleType` is not 1-3 |
| 409 | No free space, or the vehicle is already parked |

```json
{ "vehicleReg": "LON 11111", "timeIn": "2026-11-22T12:00:00Z" }
```

### Check out

`POST /parking/bill`

```bash
curl -X POST http://localhost:8080/parking/bill \
  -H "Content-Type: application/json" \
  -d '{"vehicleReg": "LON 11111"}'
```

| Status | When |
|---|---|
| 200 | Returns the bill |
| 400 | `vehicleReg` is blank |
| 404 | The vehicle is not parked |

```json
{
  "vehicleReg": "LON 11111",
  "vehicleCharge": 12.00,
  "timeIn": "2026-11-22T12:00:00Z",
  "timeOut": "2026-11-22T12:10:00Z"
}
```

### Errors

Errors use the [Problem Details](https://www.rfc-editor.org/rfc/rfc9457) format. The reason is in `detail`:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "vehicleType must be between 1 and 3",
  "instance": "/parking"
}
```

## Charging

- Small vehicles pay £1 per minute, medium £2, large £3.
- Only whole minutes count, so 4 minutes 59 seconds is 4 minutes.
- Every full 5 minutes adds £1.

| Vehicle | 4 min | 5 min | 10 min |
|---|---|---|---|
| small | £4 | £6 | £12 |
| medium | £8 | £11 | £22 |
| large | £12 | £16 | £32 |

## Project structure

```
com.gs5.carpark
├── domain        CarPark, value objects (Vehicle, RegistrationNumber, Bill, ...), use case interfaces
├── application   use case implementations
├── adapter       REST controller and error handling
└── ApplicationConfig   wires the beans together
```

The domain has no Spring dependencies.

## Design notes

- **Capacity** is two spaces, set in `CarPark`.
- **State lives in memory.** It is lost on restart, and the service only works as a single instance.
- **Thread safety.** All changes to `CarPark` go through one lock, and occupancy is read as a single snapshot, so concurrent requests cannot double-book a space or see an inconsistent count.
- **Expected outcomes are values, not exceptions.** A full car park or an unknown vehicle comes back as an empty `Optional` and the controller turns it into 409 or 404.
- **Validation happens twice.** The API rejects bad input with 400 before it reaches the domain. The domain guards its own invariants (non-blank registration, check-out not before check-in); if one of those fails, it is a bug and the service returns 500.
- **409 does not say why** the check-in was refused, because the domain does not yet tell "full" apart from "already parked".
