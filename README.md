# Description

This library will add support of single test context for all tests:

- docker containers runner
- application server instance
- some tools config: RestAsured, WireMock etc
- etc

Don't use reloadable context it doesn't work! (problem with entrypoint for custom management of jupiter threads)
