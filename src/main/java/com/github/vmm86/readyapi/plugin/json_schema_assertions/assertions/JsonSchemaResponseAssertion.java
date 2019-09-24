package com.github.vmm86.readyapi.plugin.json_schema_assertions.assertions;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.HttpMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStepInterface;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.plugins.auto.PluginTestAssertion;
import com.eviware.soapui.support.JsonUtil;

@PluginTestAssertion(
    id = "JsonSchemaResponseAssertion",
    label = "Json Schema Response Assertion",
    category = AssertionCategoryMapping.STATUS_CATEGORY,
    description = "Validate response JSON with a given JSON Schema (drafts 4 to 7 supported)"
)
public class JsonSchemaResponseAssertion extends JsonSchemaAssertion implements RequestAssertion, ResponseAssertion {
    public JsonSchemaResponseAssertion(TestAssertionConfig assertionConfig, Assertable modelItem) {
        super(
            assertionConfig, modelItem,
            true, false, false, true
        );
        ID = "JsonSchemaResponseAssertion";
        LABEL = "Json Schema Response Assertion";
        DESCRIPTION = "Validate response JSON with a given JSON Schema (drafts 4 to 7 supported)";
        ORDER = 41;
    }

    String getJsonObjectString(HttpMessageExchange messageExchange) {
        return messageExchange.getResponseContent();
    }

    boolean ifMessageIsSuitableForAssertion(MessageExchange messageExchange) {
        return messageExchange != null && messageExchange.hasResponse();
    }

    protected String internalAssertRequest(
        MessageExchange messageExchange,
        PropertyExpansionContext context
    ) throws AssertionException {
//        log.debug("Response -> internalAssertRequest");
        return null;
    }

    public Assertable.AssertionStatus assertRequest(
        MessageExchange messageExchange,
        PropertyExpansionContext context
    ) {
//        log.debug("Response -> assertRequest");
        return null;
    }

    protected String internalAssertResponse(
        MessageExchange messageExchange,
        SubmitContext context
    ) throws AssertionException {
//        log.debug("Response -> internalAssertResponse");
        return internalAssert(messageExchange);
    }

    public Assertable.AssertionStatus assertResponse(
        MessageExchange messageExchange,
        SubmitContext context
    ) {
//        log.debug("Response -> assertResponse");
        return super.assertResponse(messageExchange, context);
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
