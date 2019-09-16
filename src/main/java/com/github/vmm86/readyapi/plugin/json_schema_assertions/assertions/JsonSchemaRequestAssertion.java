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
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.plugins.auto.PluginTestAssertion;
import com.eviware.soapui.support.JsonUtil;
import org.json.JSONObject;

@PluginTestAssertion(
    id = "JsonSchemaRequestAssertion",
    label = "Json Schema Request Assertion",
    category = AssertionCategoryMapping.STATUS_CATEGORY,
    description = "Validate request message JSON body with a given JSON Schema"
)
public class JsonSchemaRequestAssertion extends JsonSchemaBaseAssertion implements RequestAssertion, ResponseAssertion {
    public JsonSchemaRequestAssertion(TestAssertionConfig assertionConfig, Assertable modelItem) {
        super(
            assertionConfig, modelItem,
            true, false, false, false,
            JsonSchemaRequestAssertion.class
        );
        ID = "JsonSchemaRequestAssertion";
        LABEL = "Json Schema Request Assertion";
        DESCRIPTION = "Validate request message JSON body with a given JSON Schema";
        ORDER = 40;
    }

    JSONObject getJsonObject(HttpMessageExchange messageExchange) {
        JSONObject jsonObject = new JSONObject(messageExchange.getRequestContent());
        return jsonObject.optJSONObject("params");
    }

    boolean ifMessageIsSuitableForAssertion(MessageExchange messageExchange) {
        return messageExchange != null && messageExchange.hasRequest(true);
    }

    protected String internalAssertRequest(
        MessageExchange messageExchange,
        PropertyExpansionContext context
    ) throws AssertionException {
        return internalAssert(messageExchange);
    }

    public Assertable.AssertionStatus assertRequest(
        MessageExchange messageExchange,
        PropertyExpansionContext context
    ) {
        return super.assertRequest(messageExchange, context);
    }

    /**
     * internalAssertResponse should not be using in request assertion.
     */
    protected String internalAssertResponse(
        MessageExchange messageExchange,
        SubmitContext submitContext
    ) throws AssertionException {
        return null;
    }

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
            super(ID, LABEL, JsonSchemaRequestAssertion.class);
        }

        public String getCategory() {
            return CATEGORY;
        }

        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return JsonSchemaRequestAssertion.class;
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
