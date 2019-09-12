# ReadyAPI JSON Schema Assertions plugin

This repository contains source code for the JSON Schema Assertions plugin for ReadyAPI. Use this plugin to validate a certain test case's request or response body by the given JSON schema.

## Plugin info

* Author: [vmm86](https://github.com/vmm86)
* Plugin version: 1.0.0
* JSON schema library: [Everit JSON Schema](https://github.com/everit-org/json-schema) 1.11.1

## Requirements

The plugin requires ReadyAPI version 2.6 or later.

## Working with the plugin

### Build the plugin

Clone this repository, make sure you have *Maven* installed and execute the following command:

```bash
mvn clean install
```

To learn how to install the plugin from a file in ReadyAPI, see [ReadyAPI documentation](https://support.smartbear.com/readyapi/docs/integrations/managing.html).

### Install the plugin

To install the plugin:

1. In ReadyAPI, switch to the **Integrations** tab.
2. Open plugin *.jar installation file and click **Install**.
3. Confirm that you want to install the plugin.
