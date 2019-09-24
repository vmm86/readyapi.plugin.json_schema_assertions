# CHANGELOG

The changelog format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2019-09-24
### Added
* Basic assertion logic for request / response JSON body
* 2 assertions to add - `JsonSchemaRequestAssertion` and `JsonSchemaResponseAssertion` 
* JSON schema is to be obtained by URL - `http(s)://` or `file://`
* An optional JSON Path value to validate not the whole request / response JSON body, but some of its inner contents.
