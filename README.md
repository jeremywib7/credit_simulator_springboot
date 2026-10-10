# Credit Simulator

Console application (Java 17, plain Java + Maven) that calculates monthly vehicle-loan installments.
Only runtime library: `jackson-databind` (JSON for the web-service call). Tests use JUnit 5.

## Requirements
- Java 17+
- Nothing else to install for Maven: the bundled Maven Wrapper (./mvnw) downloads it on first build.
  Docker image needs neither Java nor Maven on the host.

## Run (step by step)
```sh
git clone <repo-url> && cd credit_simulator
./credit_simulator                     # interactive (builds target/credit_simulator.jar if missing or outdated)
./credit_simulator file_inputs.txt     # from file
bin/credit_simulator file_inputs.txt   # same, via bin/
```
Exit code is `0` on success and `1` when the file input is invalid.

### Interactive commands
Type `show` to list them at any time.

| Command | Purpose |
|---|---|
| `input` | Ask vehicle type, condition, year, total loan, tenor, DP, then print installments |
| `load` | Call the web service (mock API), then calculate automatically |
| `show` | Print all available commands |
| `save` | Save last calculation to the active sheet |
| `switch <name>` | Switch to a sheet (created if missing); each sheet keeps its own results |
| `list` | Print the results saved in the active sheet |
| `exit` | Quit |

Sheets live in memory for the current session only (assumption: the task does not ask for persistence).

### File input format (`file_inputs.txt`)
Six lines, in order:
```
Mobil          # Mobil|Motor, case-insensitive
Bekas          # Baru|Bekas, case-insensitive
2020           # 4-digit year
1000000000     # total loan, 1..1,000,000,000
6              # tenor, 1-6 years
500000000      # down payment
```
Year must be <= the current year; a `Baru` vehicle also needs year >= current year - 1.
The bundled sample uses `Bekas`/2020 so it stays valid over time.

### Sample output
```
tahun 1 : Rp. 7,500,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 8,107,500.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 8,804,745.00/bln , Suku Bunga : 8,6%
```

## Rules implemented
- New vehicle: year >= current year - 1; DP >= 35% of loan. Used vehicle: DP >= 25%.
- Year <= current year. Loan 1..1 billion, tenor 1-6 years, DP >= 0 and < loan.
- Base rate: Mobil 8%, Motor 9%. From year 2: even year +0.1%, odd year +0.5%.
- Installment per year = (remaining balance + balance x rate) / remaining months. The balance
  carried to the next year is what is still unpaid after 12 installments, so the loan is fully
  repaid at the end of the tenor.

### Worked example
Price 100,000,000, DP 25,000,000 (used vehicle), Mobil, tenor 3 years, principal 75,000,000:

| Year | Principal | Rate | Total (principal x (1 + rate)) | Monthly (total / remaining months) |
|---|---|---|---|---|
| 1 | 75,000,000 | 8% | 81,000,000 | 81,000,000 / 36 = 2,250,000.00 |
| 2 | 54,000,000 | 8.1% | 58,374,000 | 58,374,000 / 24 = 2,432,250.00 |
| 3 | 29,187,000 | 8.6% | 31,697,082 | 31,697,082 / 12 = 2,641,423.50 |

Next-year principal = total - 12 x monthly. The sample numbers in the task statement
(3,500,000 / 3,501,000 / 3,505,000) are illustrative and cannot be derived from the inputs.

## Project layout
```
credit_simulator            launcher at project root (delegates to bin/credit_simulator)
mvnw, mvnw.cmd, .mvn/      Maven Wrapper
bin/credit_simulator        builds the jar on first run, then runs it
scripts/smoke-test.sh       end-to-end check of the launcher
src/main/java/com/credit    controller, factory, model, service, view, exception
src/test/java/com/credit    JUnit 5 tests, same package layout
Dockerfile, .github/workflows/ci.yml
```

## Design
Layers (MVC + Command + Factory), no framework:

| Package | Role |
|---|---|
| `model` | Immutable records/enums: `LoanRequest`, `VehicleType`, `VehicleCondition`, `YearlyInstallment`, `Calculation` |
| `factories` | `LoanRequestFactory` parses raw text and builds a valid `LoanRequest` |
| `validator` | `LoanValidator` holds the business rules (year, loan cap, tenor, DP minimum) |
| `constant` | `LoanRules` (numbers) and `ErrorMessage` (all user-facing messages) |
| `service` | `LoanCalculator`, `SheetManager`, and `LoanDataProvider` (implemented by `LoanApiClient`) |
| `command` | One class per menu command behind the `Command` interface; `CommandRegistry` drives dispatch and `show` |
| `controller` | `LoanController` wires commands, dispatches input, runs file mode |
| `view` | `ConsoleView` is the only class that reads stdin / writes stdout |
| `config` | `AppConfig` resolves the API URL |

Adding a command = add a `Command` class and register it in `LoanController`; `show` picks it up automatically.

Web service URL comes from `application.properties` (`api.url`, bundled in the jar). Override it,
highest priority first: env `CREDIT_API_URL`, then `credit_simulator.properties` in the working
directory (git-ignored), e.g. `api.url=http://127.0.0.1:8765/loan.json`.
## Tests
```sh
./mvnw test                 # unit tests (JUnit 5), no network needed for the API flow
./mvnw verify               # tests + build jar
sh scripts/smoke-test.sh    # end-to-end check of the launcher (needs the built jar or Maven)
```

## Troubleshooting
- `load` prints `PKIX path building failed` or an HTTP error: the mock endpoint in the task statement
  (`run.mocky.io`) may no longer be served. It was observed answering with a self-signed
  `CN=lolcatho.st` certificate and HTTP 404, so this is not a local trust problem. Point the app at
  any server that returns the JSON from the task statement:
  a `credit_simulator.properties` file next to where you run the app, containing
  `api.url=http://127.0.0.1:8765/loan.json` (serve `scripts/mock/loan.json` with
  `python -m http.server 8765 --directory scripts/mock`). The `load` flow itself is covered
  by `LoanApiClientTest` against a local HTTP server.
- IDE shows `class file for com.fasterxml.jackson.core.Versioned not found`: Jackson was added as
  a plain jar instead of through Maven. Remove manual libraries and reload the Maven project.
- Maven cannot download dependencies: use `./mvnw -o` (offline) if everything is already cached.

## Docker
```sh
docker build -t <dockerhub-user>/credit_simulator .
docker run --rm -it <dockerhub-user>/credit_simulator                       # interactive
docker run --rm <dockerhub-user>/credit_simulator /app/file_inputs.txt      # file input
```

## CI/CD
`.github/workflows/ci.yml`: on push/PR runs `mvn verify` and the smoke test; on push to `main`
or a `v*` tag builds and pushes the image to Docker Hub. Add repository secrets
`DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`.
