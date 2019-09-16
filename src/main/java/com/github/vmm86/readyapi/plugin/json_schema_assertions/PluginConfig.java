package com.github.vmm86.readyapi.plugin.json_schema_assertions;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

@PluginConfiguration(
    groupId = "com.github.vmm86.readyapi.plugin",
    name = "JSON Schema Assertions Plugin",
    description = "ReadyAPI plugin to validate request or response body with the given JSON schema",
    version = "1.0.0",
    infoUrl = "https://github.com/vmm86/readyapi.plugin.json_schema_assertions"
)
public class PluginConfig extends PluginAdapter {
    public void initialize() {
        super.initialize();
    }
}
