package com.digitalpetri.opc.xmlda.client;

import java.time.Clock;
import java.util.Locale;
import java.util.Optional;

import org.opcfoundation.xmlda.Browse;
import org.opcfoundation.xmlda.BrowseResponse;
import org.opcfoundation.xmlda.GetProperties;
import org.opcfoundation.xmlda.GetPropertiesResponse;
import org.opcfoundation.xmlda.GetStatus;
import org.opcfoundation.xmlda.GetStatusResponse;
import org.opcfoundation.xmlda.Read;
import org.opcfoundation.xmlda.ReadResponse;
import org.opcfoundation.xmlda.RequestOptions;
import org.opcfoundation.xmlda.Subscribe;
import org.opcfoundation.xmlda.SubscribeResponse;
import org.opcfoundation.xmlda.SubscriptionCancel;
import org.opcfoundation.xmlda.SubscriptionCancelResponse;
import org.opcfoundation.xmlda.SubscriptionPolledRefresh;
import org.opcfoundation.xmlda.SubscriptionPolledRefreshResponse;
import org.opcfoundation.xmlda.Write;
import org.opcfoundation.xmlda.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import static com.digitalpetri.opc.xmlda.client.SoapAction.BROWSE;
import static com.digitalpetri.opc.xmlda.client.SoapAction.GET_PROPERTIES;
import static com.digitalpetri.opc.xmlda.client.SoapAction.GET_STATUS;
import static com.digitalpetri.opc.xmlda.client.SoapAction.READ;
import static com.digitalpetri.opc.xmlda.client.SoapAction.SUBSCRIBE;
import static com.digitalpetri.opc.xmlda.client.SoapAction.SUBSCRIPTION_CANCEL;
import static com.digitalpetri.opc.xmlda.client.SoapAction.SUBSCRIPTION_POLLED_REFRESH;
import static com.digitalpetri.opc.xmlda.client.SoapAction.WRITE;
import static org.opcfoundation.xmlda.BrowseFilter.ALL;

/**
 * Open Process Control XML-DataAccess SOAP Client.
 *
 * @author Yuriy Tumakha
 */
public class OpcXmlDaClient extends WebServiceGatewaySupport {

    private static final Logger LOG = LoggerFactory.getLogger(OpcXmlDaClient.class);
    private Clock clock = Clock.systemDefaultZone();
    private Locale defaultLocale;
    private String defaultLanguageTag;

    public OpcXmlDaClient() {
        setDefaultLocale(Locale.US);
    }

    public GetStatusResponse getStatus() {
        return getStatus(defaultLocale);
    }

    public GetStatusResponse getStatus(Locale locale) {
        GetStatus statusRequest = new GetStatus();
        statusRequest.setLocaleID(getLanguageTag(locale));
        return getStatus(statusRequest);
    }

    public GetStatusResponse getStatus(GetStatus statusRequest) {
        statusRequest.setLocaleID(getOrDefaultLang(statusRequest.getLocaleID()));
        statusRequest.setClientRequestHandle(getOrDefaultHandle(statusRequest.getClientRequestHandle()));
        return invokeAction(GET_STATUS, statusRequest, GetStatusResponse.class);
    }

    public BrowseResponse browse() {
        Browse browseRequest = new Browse();
        browseRequest.setBrowseFilter(ALL);
        return browse(browseRequest);
    }

    public BrowseResponse browse(Browse browseRequest) {
        browseRequest.setLocaleID(getOrDefaultLang(browseRequest.getLocaleID()));
        browseRequest.setClientRequestHandle(getOrDefaultHandle(browseRequest.getClientRequestHandle()));
        return invokeAction(BROWSE, browseRequest, BrowseResponse.class);
    }

    public GetPropertiesResponse getProperties(GetProperties getPropertiesRequest) {
        getPropertiesRequest.setLocaleID(getOrDefaultLang(getPropertiesRequest.getLocaleID()));
        getPropertiesRequest.setClientRequestHandle(getOrDefaultHandle(getPropertiesRequest.getClientRequestHandle()));
        return invokeAction(GET_PROPERTIES, getPropertiesRequest, GetPropertiesResponse.class);
    }

    public ReadResponse read(Read readRequest) {
        if (readRequest.getOptions() == null) readRequest.setOptions(new RequestOptions());
        setDefaultOptions(readRequest.getOptions());
        return invokeAction(READ, readRequest, ReadResponse.class);
    }

    public WriteResponse write(Write writeRequest) {
        if (writeRequest.getOptions() == null) writeRequest.setOptions(new RequestOptions());
        setDefaultOptions(writeRequest.getOptions());
        return invokeAction(WRITE, writeRequest, WriteResponse.class);
    }

    public SubscribeResponse subscribe(Subscribe subscribeRequest) {
        if (subscribeRequest.getOptions() == null) subscribeRequest.setOptions(new RequestOptions());
        setDefaultOptions(subscribeRequest.getOptions());
        return invokeAction(SUBSCRIBE, subscribeRequest, SubscribeResponse.class);
    }

    public SubscriptionPolledRefreshResponse subscriptionPolledRefresh(SubscriptionPolledRefresh subscriptionRefresh) {
        if (subscriptionRefresh.getOptions() == null) subscriptionRefresh.setOptions(new RequestOptions());
        setDefaultOptions(subscriptionRefresh.getOptions());
        return invokeAction(SUBSCRIPTION_POLLED_REFRESH, subscriptionRefresh, SubscriptionPolledRefreshResponse.class);
    }

    public SubscriptionCancelResponse subscriptionCancel(SubscriptionCancel subscriptionCancelRequest) {
        subscriptionCancelRequest.setClientRequestHandle(
            getOrDefaultHandle(subscriptionCancelRequest.getClientRequestHandle()));
        return invokeAction(SUBSCRIPTION_CANCEL, subscriptionCancelRequest, SubscriptionCancelResponse.class);
    }

    public String generateClientRequestHandle() {
        return String.valueOf(clock.millis());
    }

    public void setDefaultLocale(Locale locale) {
        defaultLocale = locale;
        defaultLanguageTag = defaultLocale.toLanguageTag();
    }

    @SuppressWarnings("unchecked")
    private <T> T invokeAction(SoapAction soapAction, Object requestPayload, Class<T> responseClass) {
        return (T) getWebServiceTemplate().marshalSendAndReceive(requestPayload,
            new SoapActionCallback(soapAction.getActionPath()));
    }

    private String getLanguageTag(Locale locale) {
        return Optional.ofNullable(locale).orElse(defaultLocale).toLanguageTag();
    }

    private void setDefaultOptions(RequestOptions requestOptions) {
        requestOptions.setLocaleID(getOrDefaultLang(requestOptions.getLocaleID()));
        requestOptions.setClientRequestHandle(getOrDefaultHandle(requestOptions.getClientRequestHandle()));
        requestOptions.setReturnErrorText(true);
        requestOptions.setReturnDiagnosticInfo(true);
        requestOptions.setReturnItemName(true);
        requestOptions.setReturnItemTime(true);
    }

    private String getOrDefaultLang(String languageTag) {
        return languageTag == null || languageTag.isEmpty() ? defaultLanguageTag : languageTag;
    }

    private String getOrDefaultHandle(String requestHandle) {
        return requestHandle == null || requestHandle.isEmpty() ? generateClientRequestHandle() : requestHandle;
    }

}
