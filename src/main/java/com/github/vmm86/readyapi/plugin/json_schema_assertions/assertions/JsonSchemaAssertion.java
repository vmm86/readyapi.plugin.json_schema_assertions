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
import com.eviware.x.form.validators.RequiredValidator;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormTextField;

import static com.jayway.jsonpath.JsonPath.using;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider;
import com.jayway.jsonpath.spi.mapper.JsonOrgMappingProvider;

import io.swagger.util.Json;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class JsonSchemaAssertion extends WsdlMessageAssertion {
    private static final String JSON_SCHEMA_URL_FIELD = "jsonSchemaUrl";
    private static final String JSON_SCHEMA_URL_FIELD_NAME = "JSON Schema URL";
    private static final int JSON_SCHEMA_URL_FIELD_WIDTH = 40;
    private static final String JSON_PATH_FIELD = "jsonPath";
    private static final String JSON_PATH_FIELD_NAME = "JSON Path";
    private static final int JSON_PATH_FIELD_WIDTH = 40;
    static final Logger log = LoggerFactory.getLogger(JsonSchemaAssertion.class);
    static String ID;
    static String LABEL;
    static String DESCRIPTION;
    static Integer ORDER;
    private final Map<URL, Schema> cachedSchemas = new HashMap<>();
    private final String loggingHeader;
    private XFormDialog dialog;
    private String jsonSchemaUrl;
    private String jsonPath;

    private static final Configuration ORG_JSON_CONFIGURATION = Configuration
        .builder()
        .mappingProvider(new JsonOrgMappingProvider())
        .jsonProvider(new JsonOrgJsonProvider())
        .build();

    JsonSchemaAssertion(
        TestAssertionConfig assertionConfig, Assertable modelItem,
        Boolean cloneable, Boolean configurable, Boolean multiple, Boolean requiresResponseContent
    ) {
        super(assertionConfig, modelItem, cloneable, configurable, multiple, requiresResponseContent);

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        jsonSchemaUrl = reader.readString(JSON_SCHEMA_URL_FIELD, "");
        jsonPath = reader.readString(JSON_PATH_FIELD, "");

        loggingHeader = "[" +
            modelItem.getTestStep().getTestCase().getTestSuite().getProject().getName() + "/" +
            modelItem.getTestStep().getTestCase().getTestSuite().getName() + "/" +
            modelItem.getTestStep().getTestCase().getName() + "/" +
            modelItem.getTestStep().getName() + "/" +
            getClass().getSimpleName() +
        "] ";

//        log.debug(loggingHeader + "jsonSchemaUrl -> " + jsonSchemaUrl + " jsonPath -> " + jsonPath);
    }

    public String getName() {
        return getClass().getSimpleName() + " (" + jsonSchemaUrl + ", " + jsonPath + ")";
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
        values.put(JSON_PATH_FIELD_NAME, jsonPath);
        values = dialog.show(values);
        if (dialog.getReturnValue() == 1) {
            jsonSchemaUrl = values.get(JSON_SCHEMA_URL_FIELD_NAME);
            cachedSchemas.clear();
            jsonPath = values.get(JSON_PATH_FIELD_NAME);
            setConfiguration(createConfiguration());
            return true;
        } else {
            return false;
        }
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder(LABEL);
        XForm mainForm = builder.createForm("Basic");

        XFormTextField jsonSchemaURLField = mainForm.addTextField(
            JSON_SCHEMA_URL_FIELD_NAME,
            "JSON Schema Definition URL",
            XForm.FieldType.URL
        );
        jsonSchemaURLField.addFormFieldValidator(
            new RequiredValidator("JSON Schema URL should not be empty")
        );
        jsonSchemaURLField.setWidth(JSON_SCHEMA_URL_FIELD_WIDTH);

        XFormTextField jsonPathField = mainForm.addTextField(
            JSON_PATH_FIELD_NAME,
            "JSON Path expression to filter data for assertion if needed (optional)",
            XForm.FieldType.TEXT
        );
        jsonPathField.setWidth(JSON_PATH_FIELD_WIDTH);

        dialog = builder.buildDialog(
            builder.buildOkCancelActions(),
            "Specify JSON Schema assertion options",
            UISupport.OPTIONS_ICON
        );
    }

    private XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add(JSON_SCHEMA_URL_FIELD, jsonSchemaUrl);
        builder.add(JSON_PATH_FIELD, jsonPath);
        return builder.finish();
    }

    abstract String getJsonObjectString(HttpMessageExchange messageExchange);

    private Object getJsonObject(HttpMessageExchange messageExchange) throws AssertionException {
        try {
            return using(ORG_JSON_CONFIGURATION)
                .parse(getJsonObjectString(messageExchange))
                .read(!jsonPath.isEmpty() ? jsonPath : "$");
        } catch (IllegalArgumentException | JsonPathException e) {
            String errorMessage = "JSON object is invalid or empty -> " +
                e.getClass().getSimpleName() + " -> " + e.getMessage();
            log.error(loggingHeader + errorMessage);
            throw new AssertionException(new AssertionError(errorMessage));
        }
    }

    private Schema getJsonSchema() throws AssertionException {
        try {
            URL jsonSchemaUrlObject = new URL(jsonSchemaUrl);
            Schema jsonSchema = cachedSchemas.get(jsonSchemaUrlObject);
            if (jsonSchema == null) {
                JSONObject schemaObject = new JSONObject(
                    Json.mapper().readTree(jsonSchemaUrlObject).toString()
                );
                jsonSchema = SchemaLoader.builder().schemaJson(schemaObject).build().load().build();
                cachedSchemas.put(jsonSchemaUrlObject, jsonSchema);
            }
            return jsonSchema;
        } catch (Exception e) {
            String errorMessage = "JSON Schema is invalid or empty -> " +
                e.getClass().getSimpleName() + " -> " + e.getMessage();
            log.error(loggingHeader + errorMessage);
            log.debug(Arrays.toString(e.getStackTrace()));
            throw new AssertionException(new AssertionError(errorMessage));
        }
    }

    abstract boolean ifMessageIsSuitableForAssertion(MessageExchange messageExchange);

    private void validateMessage(Schema jsonSchema, Object jsonObject) throws AssertionException {
        try {
            jsonSchema.validate(jsonObject);
        } catch (ValidationException ve) {
            String errorMessage = ve.getClass().getSimpleName() + " -> " + ve.getErrorMessage();
            String jsonFailureReport = "JSON failure report -> " + ve.toJSON().toString();
            log.error(loggingHeader + errorMessage);
            log.error(loggingHeader + jsonFailureReport);
            throw new AssertionException(
                new AssertionError[]{
                    new AssertionError(errorMessage),
                    new AssertionError(jsonFailureReport)
                }
            );
        } catch (Exception e) {
            String errorMessage = e.getClass().getSimpleName() + " -> " + e.getMessage();
            log.error(loggingHeader + errorMessage);
            log.debug(Arrays.toString(e.getStackTrace()));
            throw new AssertionException(new AssertionError(errorMessage));
        }
    }

    String internalAssert(MessageExchange messageExchange) throws AssertionException {
        Schema jsonSchema = getJsonSchema();
        log.debug(loggingHeader + "JSON schema -> " + jsonSchema);

        boolean messageExchangeCompliant = messageExchange instanceof HttpMessageExchange;
        if (!messageExchangeCompliant) {
            String errorMessage = "messageExchange in not an instance of HttpMessageExchange";
            log.error(loggingHeader + errorMessage);
            throw new AssertionException(new AssertionError(errorMessage));
        }

        boolean suitableForAssertion = ifMessageIsSuitableForAssertion(messageExchange);
        if (!suitableForAssertion) {
            String errorMessage = "messageExchange data is not suitable for assertion";
            log.error(loggingHeader + errorMessage);
            throw new AssertionException(new AssertionError(errorMessage));
        }

        Object jsonObject = getJsonObject((HttpMessageExchange) messageExchange);
        log.debug(loggingHeader + "JSON object -> " + jsonObject.toString());

        validateMessage(jsonSchema, jsonObject);

        String result = "Successful JSON Schema validation";
        log.info(loggingHeader + result);

        return result;
    }
}
