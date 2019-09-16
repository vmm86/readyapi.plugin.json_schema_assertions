package com.github.vmm86.readyapi.plugin.json_schema_assertions.assertions;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.submit.HttpMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

import io.swagger.util.Json;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class JsonSchemaBaseAssertion extends WsdlMessageAssertion {
    private static final String JSON_SCHEMA_URL_FIELD = "jsonSchemaUrl";
    private static final String JSON_SCHEMA_URL_FIELD_NAME = "JSON Schema URL";
    private static final int JSON_SCHEMA_URL_FIELD_WIDTH = 40;
    private final Map<URL, Schema> cachedSchemas = new HashMap<URL, Schema>();
    static Logger log;
    static String ID;
    static String LABEL;
    static String DESCRIPTION;
    static Integer ORDER;
    private String jsonSchemaUrl;
    private XFormDialog dialog;

    JsonSchemaBaseAssertion(
            TestAssertionConfig assertionConfig, Assertable modelItem,
            Boolean cloneable, Boolean configurable, Boolean multiple, Boolean requiresResponseContent,
            Class<? extends JsonSchemaBaseAssertion> childClass
    ) {
        super(assertionConfig, modelItem, cloneable, configurable, multiple, requiresResponseContent);
        log = LoggerFactory.getLogger(childClass);
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        jsonSchemaUrl = reader.readString(JSON_SCHEMA_URL_FIELD, "");
    }

    public boolean isConfigurable() {
        return true;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();
        values.put(JSON_SCHEMA_URL_FIELD_NAME, jsonSchemaUrl);
        values = dialog.show(values);
        if (dialog.getReturnValue() == 1) {
            jsonSchemaUrl = values.get(JSON_SCHEMA_URL_FIELD_NAME);
            cachedSchemas.clear();
            setConfiguration(createConfiguration());
            return true;
        } else {
            return false;
        }
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(LABEL);
        XForm mainForm = builder.createForm("Basic");
        mainForm.addTextField(
            JSON_SCHEMA_URL_FIELD_NAME,
            "JSON Schema Definition URL",
            XForm.FieldType.URL
        ).setWidth(JSON_SCHEMA_URL_FIELD_WIDTH);
        dialog = builder.buildDialog(
            builder.buildOkCancelActions(),
            "Specify JSON Schema URL",
            UISupport.OPTIONS_ICON
        );
    }

    private XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        return builder.add(JSON_SCHEMA_URL_FIELD, jsonSchemaUrl).finish();
    }

    abstract JSONObject getJsonObject(HttpMessageExchange messageExchange);

    abstract boolean ifMessageIsSuitableForAssertion(MessageExchange messageExchange);

    private URL getJsonSchemaUrlObject() throws MalformedURLException {
        return new URL(jsonSchemaUrl); // PropertyExpander.expandProperties()
    }

    private Schema getJsonSchema() throws IOException {
        URL jsonSchemaUrlObject = getJsonSchemaUrlObject();
        Schema jsonSchema = cachedSchemas.get(jsonSchemaUrlObject);

        if (jsonSchema == null) {
            JSONObject schemaObject = new JSONObject(Json.mapper().readTree(jsonSchemaUrlObject).toString());
            SchemaLoader schemaLoader = SchemaLoader.builder().schemaJson(schemaObject).build();
            jsonSchema = schemaLoader.load().build();
            cachedSchemas.put(jsonSchemaUrlObject, jsonSchema);
        }
        return jsonSchema;
    }

    private boolean validateMessage(JSONObject jsonObject, Schema jsonSchema) throws AssertionException {
        try {
            log.info("jsonObject: " + jsonObject.toString());
            log.info("jsonSchema: " + jsonSchema.toString());
            jsonSchema.validate(jsonObject);
            return true;
        } catch (ValidationException ve) {
            throw new AssertionException(
                new AssertionError(
                    "JSON Schema validation failed: " + ve.getErrorMessage()
                )
            );
        } catch (Exception e) {
            throw new AssertionException(
                new AssertionError(
                    "JSON Schema validation failed: " + e.getMessage()
                )
            );
        }
    }

    String internalAssert(MessageExchange messageExchange) throws AssertionException {
        String result = "";

        boolean nonEmptyJsonSchemaUrl = jsonSchemaUrl != null;
        boolean isClassCompliant = messageExchange instanceof HttpMessageExchange;
        boolean suitableForAssertion = ifMessageIsSuitableForAssertion(messageExchange);

        log.info(
            "nonEmptyJsonSchemaUrl: " + nonEmptyJsonSchemaUrl +
            ", isClassCompliant: " + isClassCompliant +
            ", suitableForAssertion: " + suitableForAssertion
        );

        if (nonEmptyJsonSchemaUrl && isClassCompliant && suitableForAssertion) {
            JSONObject jsonObject = getJsonObject((HttpMessageExchange)messageExchange);
            try {
                Schema jsonSchema = getJsonSchema();
                boolean validated = validateMessage(jsonObject, jsonSchema);
                if (validated) {
                    result = "JSON object is valid according to JSON schema";
                }
            } catch (IOException e) {
                throw new AssertionException(new AssertionError(e.getMessage()));
            }
        } else {
            if (!nonEmptyJsonSchemaUrl) {
                result = "JSON Schema URL is empty";
            }
            if (!isClassCompliant) {
                result = "messageExchange in not an instance of HttpMessageExchange";
            }
            if (!suitableForAssertion) {
                result = "messageExchange request in empty";
            }
            throw new AssertionException(new AssertionError(result));
        }

        return result;
    }
}
