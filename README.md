# SeleniumProjects

Java 17 Selenium tests using TestNG and Cucumber, with Maven profiles for the
different suites.

## Project structure

```text
src/
├── main/
│   ├── java/SeleniumFramworkDesign/
│   │   ├── base/                 # Shared Selenium components
│   │   ├── pages/                # Page objects
│   │   └── utils/                # Reporting helpers
│   └── resources/config/         # Shared framework defaults
└── test/
    ├── java/SeleniumFramworkDesign/
    │   ├── base/                 # Test setup, listeners, retry
    │   ├── runners/              # TestNG/Cucumber runner
    │   ├── stepDefinitions/ui/   # UI Cucumber steps
    │   ├── tests/                # TestNG UI tests
    │   └── utils/                # Test data helper
    └── resources/
        ├── features/ui/          # Cucumber UI features
        └── suites/               # TestNG suite XML files
test-data/
├── dev/
└── qa/
```

This is a UI-only Selenium project; the API folders from the reference layout
are intentionally not included.

## Prerequisites

- JDK 17 or later
- Maven 3.9 or later
- Chrome, Edge, or Firefox for local runs

WebDriverManager downloads the matching browser driver. The selected browser
itself must be installed on the machine running the tests.

## Local configuration

Copy `.env.example` to `.env` and set the environment-specific login and test
data values. `.env` is ignored by Git and must never be committed. Test datasets
are stored separately in `test-data/qa/` and `test-data/dev/`; `APP_ENV=qa` or
`APP_ENV=dev` selects the corresponding dataset. Each environment uses its own
`*_TEST_USER_EMAIL`, `*_TEST_USER_PASSWORD`, and product settings. Missing
environment credentials or product data fail with a clear configuration error.

The QA and Dev base URL values currently point to the same application URL.
Set `BROWSER` to `chrome`, `edge`, or `firefox`, and `HEADLESS` to `true` or
`false`.

Run suites from the project root:

```powershell
mvn -P Purchase test
mvn -P Regression test
mvn -P ErrorValidationTest test
mvn -P CucumberTests test
mvn -P LinkValidation test
```

Cucumber scenarios use PicoContainer for per-scenario context, Cucumber hooks
for driver lifecycle and failure screenshots, and one TestNG retry per failed
scenario. To run one Cucumber scenario by name:

```powershell
mvn -P CucumberTests test "-Dcucumber.filter.name=Scenario name"
```

Maven properties can override corresponding environment values, for example:

```powershell
mvn -P Purchase test "-Dapp.env=qa" "-Dbrowser=chrome" "-Dheadless=true"
```

## CI credentials and reports

- **GitHub Actions:** add repository Actions secrets named
  `QA_TEST_USER_EMAIL` and `QA_TEST_USER_PASSWORD`. Pull requests run a
  compile/test-compile validation without credentials; pushes and manual
  dispatch run the QA Purchase UI suite. Reports and screenshots are uploaded
  as workflow artifacts. To manually run Purchase tests on Chrome, Firefox,
  and Edge, start the **Manual cross-browser UI tests** workflow from the
  Actions tab.
- **Jenkins:** create Secret Text credentials with IDs
  `qa-test-user-email`, `qa-test-user-password`, `dev-test-user-email`, and
  `dev-test-user-password`. The selected `APP_ENV` determines which pair is
  used. Configure the Pipeline job to load this repository's `Jenkinsfile`.
  Browser binaries must be installed on its agent.

### Jenkins Freestyle job

For a separate Windows Freestyle job, configure choice parameters named
`APP_ENV` (`qa`, `dev`), `BROWSER` (`chrome`, `edge`, `firefox`), `HEADLESS`
(`true`, `false`), and `TEST_SUITE` (`Purchase`, `Regression`,
`ErrorValidationTest`, `CucumberTests`, `LinkValidation`). Bind the selected
environment's Jenkins email and password credentials to
`<APP_ENV>_TEST_USER_EMAIL` and `<APP_ENV>_TEST_USER_PASSWORD`; for example,
the QA bindings are `QA_TEST_USER_EMAIL` and `QA_TEST_USER_PASSWORD`.

Use an **Execute Windows batch command** build step:

```bat
set "QA_BASE_URL=https://rahulshettyacademy.com/client"
set "DEV_BASE_URL=https://rahulshettyacademy.com/client"
mvn --batch-mode --no-transfer-progress -DfailIfNoTests=true -Dapp.env=%APP_ENV% -Dbrowser=%BROWSER% -Dheadless=%HEADLESS% -P %TEST_SUITE% test
```

Publish `target/surefire-reports/TEST-*.xml` with the JUnit test-result
publisher, and archive `target/surefire-reports/**,target/cucumber.html,report/**,reportss/**`.
Use `HEADLESS=true` when Jenkins runs as a Windows service without an
interactive desktop session.

TestNG/Surefire reports are written under `target/surefire-reports/`, the
Cucumber HTML report under `target/cucumber.html`, and a uniquely named Extent
HTML report is generated under `report/` for each TestNG or Cucumber test
execution. Failure screenshots are saved under `reportss/`. CI pipelines
archive both output directories.

Run `mvn -P LinkValidation test` to check HTTP(S) links on the authenticated
product dashboard. Purchase tests also save product-card element screenshots
under `reportss/`.
