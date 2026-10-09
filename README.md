# Credit Simulator

Console application (Java 17, plain Java + Maven) that calculates monthly vehicle-loan installments.
Only runtime library: `jackson-databind` (JSON for the web-service call). Tests use JUnit 5.

## Requirements
- Java 17+
- Maven 3.8+ (only to build; Docker image needs neither)

## Run (step by step)
```sh
git clone <repo-url> && cd credit_simulator
./credit_simulator                     # interactive (first run builds target/credit_simulator.jar)
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

Sheets live in memory for the current session.

### File input format (`file_inputs.txt`)
Six lines, in order:
```
Mobil          # Mobil|Motor, case-insensitive
Baru           # Baru|Bekas, case-insensitive
2025           # 4-digit year
1000000000     # total loan, 1..1,000,000,000
6              # tenor, 1-6 years
500000000      # down payment
```
Note: a `Baru` vehicle needs year >= current year - 1, so update the year in the sample over time.

### Sample output
```
tahun 1 : Rp. 7,500,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 8,107,500.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 8,804,745.00/bln , Suku Bunga : 8,6%
```

## Rules implemented
- New vehicle: year >= current year - 1; DP >= 35% of loan. Used vehicle: DP >= 25%.
- Loan 1..1 billion, tenor 1-6 years, DP >= 0 and < loan.
- Base rate: Mobil 8%, Motor 9%. From year 2: even year +0.1%, odd year +0.5%.
- Installment per year = (remaining balance + balance x rate) / remaining months. The balance
  carried to the next year is what is still unpaid after 12 installments, so the loan is fully
  repaid at the end of the tenor. The task statement gives no formula; this is the chosen one.

## Design
- `model` records/enums, `factory.LoanRequestFactory` (validates and builds requests),
  `service` (`LoanCalculator`, `LoanApiClient`, `SheetManager`), `view.ConsoleView`,
  `controller.LoanController` (command registry; `show` is generated from it).
- Web service URL defaults to the mocky endpoint; override with env `CREDIT_API_URL`.

## Tests
```sh
mvn test                    # 41 unit tests (JUnit 5), no network needed
mvn verify                  # tests + build jar
sh scripts/smoke-test.sh    # end-to-end check of the launcher (needs the built jar or Maven)
```

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
