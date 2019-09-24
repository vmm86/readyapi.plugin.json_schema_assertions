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
    id = "JsonSchemaRequestAssertion",
    label = "Json Schema Request Assertion",
    category = AssertionCategoryMapping.STATUS_CATEGORY,
    description = "Validate request JSON with a given JSON Schema (drafts 4 to 7 supported)"
)
public class JsonSchemaRequestAssertion extends JsonSchemaAssertion implements RequestAssertion, ResponseAssertion {
    public JsonSchemaRequestAssertion(TestAssertionConfig assertionConfig, Assertable modelItem) {
        super(
            assertionConfig, modelItem,
            true, false, false, false
        );
        ID = "JsonSchemaRequestAssertion";
        LABEL = "Json Schema Request Assertion";
        DESCRIPTION = "Validate request JSON with a given JSON Schema (drafts 4 to 7 supported)";
        ORDER = 40;
    }

    String getJsonObjectString(HttpMessageExchange messageExchange) {
        return messageExchange.getRequestContent();
    }

    boolean ifMessageIsSuitableForAssertion(MessageExchange messageExchange) {
        return messageExchange != null && messageExchange.hasRequest(true);
    }

    protected String internalAssertRequest(
        MessageExchange messageExchange,
        PropertyExpansionContext context
    ) throws AssertionException {
//        log.debug("Request -> internalAssertRequest");
        return internalAssert(messageExchange);
    }

    public Assertable.AssertionStatus assertRequest(
        MessageExchange messageExchange,
        PropertyExpansionContext context
    ) {
//        log.debug("Request -> assertRequest");
        return super.assertRequest(messageExchange, context);
    }

    protected boolean appliesToRequest(MessageExchange messageExchange) {
        return true;
    }

    protected String internalAssertResponse(
        MessageExchange messageExchange,
        SubmitContext submitContext
    ) throws AssertionException {
//        log.debug("Request -> internalAssertResponse");
        return internalAssert(messageExchange);
    }

    public Assertable.AssertionStatus assertResponse(
        MessageExchange messageExchange,
        SubmitContext context
    ) {
//        log.debug("Request -> assertResponse");
        return super.assertResponse(messageExchange, context);
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
