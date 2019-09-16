package com.github.vmm86.readyapi.plugin.json_schema_assertions.assertions;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.HttpMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStepInterface;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.plugins.auto.PluginTestAssertion;
import com.eviware.soapui.support.JsonUtil;
import org.json.JSONObject;

@PluginTestAssertion(
    id = "JsonSchemaResponseAssertion",
    label = "Json Schema Response Assertion",
    category = AssertionCategoryMapping.STATUS_CATEGORY,
    description = "Validate response message JSON body with a given JSON Schema"
)
public class JsonSchemaResponseAssertion extends JsonSchemaBaseAssertion implements ResponseAssertion {
    public JsonSchemaResponseAssertion(TestAssertionConfig assertionConfig, Assertable modelItem) {
        super(
            assertionConfig, modelItem,
            true, false, false, true,
            JsonSchemaResponseAssertion.class
        );
        ID = "JsonSchemaResponseAssertion";
        LABEL = "Json Schema Response Assertion";
        DESCRIPTION = "Validate response message JSON body with a given JSON Schema";
        ORDER = 41;
    }

    JSONObject getJsonObject(HttpMessageExchange messageExchange) {
        JSONObject jsonObject = new JSONObject(messageExchange.getResponseContent());
        return jsonObject.optJSONObject("result");
    }

    boolean ifMessageIsSuitableForAssertion(MessageExchange messageExchange) {
        return messageExchange != null && messageExchange.hasResponse();
    }

    protected String internalAssertResponse(
        MessageExchange messageExchange,
        SubmitContext context
    ) throws AssertionException {
        return internalAssert(messageExchange);
    }

//    public AssertionStatus assertResponse(MessageExchange messageExchange, SubmitContext context) {
//        return super.assertResponse(messageExchange, context);
//    }

    protected String internalAssertProperty(
        TestPropertyHolder source,
        String propertyName,
        MessageExchange messageExchange,
        SubmitContext context
    ) throws AssertionException {
        return null;
    }

    public static class Factory extends AbstractTestAssertionFactory {
        private static final String CATEGORY = AssertionCategoryMapping.STATUS_CATEGORY;

        public Factory() {
            super(ID, LABEL, JsonSchemaResponseAssertion.class);
        }

        public String getCategory() {
            return CATEGORY;
        }

        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return JsonSchemaResponseAssertion.class;
        }

        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(ID, LABEL, DESCRIPTION, ORDER);
        }

        public boolean canAssert(Assertable assertable) {
            try {
                boolean canAssert = super.canAssert(assertable);
                boolean suitableTestStep = assertable.getTestStep() instanceof HttpTestRequestStepInterface;
                boolean hasJsonBody = JsonUtil.seemsToBeJson(assertable.getAssertableContent());
                return canAssert && suitableTestStep && hasJsonBody;
            } catch (Throwable te) {
                String assertionClassName = getClass().getSimpleName();
                String assertableClassName = assertable.getClass().getSimpleName();
                log.trace(
                        assertionClassName + " assertion is not applicable for " + assertableClassName, te
                );
                return false;
            }
        }
    }
}
