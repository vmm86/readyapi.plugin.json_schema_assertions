# ReadyAPI JSON Schema Assertions plugin

This repository contains source code for the custom **JSON Schema Assertions** plugin for [ReadyAPI](https://smartbear.com/product/ready-api/overview/). You may use this plugin to validate a request or response JSON body of a certain test case with the given JSON schema.

## Plugin info

* Author: [vmm86](https://github.com/vmm86)
* Plugin version: 1.0.0
* JSON schema library: [Everit JSON Schema](https://github.com/everit-org/json-schema)
* JSON schema library version: 1.11.1

## Requirements

The plugin requires ReadyAPI version 2.6 or later.

## Working with the plugin

### Build

Clone this repository, make sure you have *Maven* installed and execute the following command:

```bash
mvn clean install
```

The installation *.jar file will be created in `target` folder (by default), having some necessary dependencies gathered in `lib` folder by Maven Assembly plugin.

### Installation

To install the plugin:

1. In ReadyAPI, switch to the **Integrations** tab.
2. Open plugin *.jar installation file and click **Install**.
3. Confirm that you want to install the plugin.

To learn how to install the plugin from a file in ReadyAPI, check out [ReadyAPI documentation](https://support.smartbear.com/readyapi/docs/integrations/managing.html).

### Usage

Request / response assertions can be added to HTTP or REST request step steps of ReadyAPI SoapUI functional tests. There can be **only one** request / response assertion be added to any particular request test step - either one request assertion or one response assertion or both of them, but not more.

There are two settings of any such assertion:

* `JSON Schema URL` (required) - URL to get the JSON schema.
* `JSON Path` (optional) - JSON Path query to get some specific portion of request / response JSON body for validation.

JSON Schema URL is required, therefore you can not add a new assertion leaving this parameter unfilled. JSON Path is optional, therefore you may leave it blank, so the whole request / response JSON body will be validated.

In case of assertion errors you will see two error messages beyond the assertion - a general error message and a more detailed JSON failure report.
