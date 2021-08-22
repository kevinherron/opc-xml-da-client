package com.digitalpetri.opc.xmlda.client;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.opcfoundation.xmlda.Browse;
import org.opcfoundation.xmlda.BrowseFilter;
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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

/**
 * OPC XML-DA SOAP Client.
 *
 * @author Kevin Herron
 * @author Yuriy Tumakha
 */
public class OpcXmlDaClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpcXmlDaClient.class);

    private final AtomicLong clientRequestHandles = new AtomicLong(0L);

    private final Locale defaultLocale;
    private final WebServiceTemplate webServiceTemplate;

    public OpcXmlDaClient(WebServiceTemplate webServiceTemplate) {
        this(webServiceTemplate, Locale.US);
    }

    public OpcXmlDaClient(WebServiceTemplate webServiceTemplate, Locale defaultLocale) {
        this.webServiceTemplate = webServiceTemplate;
        this.defaultLocale = defaultLocale;
    }

    //region GetStatus

    public GetStatusResponse getStatus() {
        return getStatus(new GetStatus());
    }

    public GetStatusResponse getStatus(Consumer<GetStatus> requestCustomizer) {
        var request = new GetStatus();

        requestCustomizer.accept(request);

        return getStatus(request);
    }

    public GetStatusResponse getStatus(GetStatus request) {
        request.setLocaleID(getOrDefaultLang(request.getLocaleID()));
        request.setClientRequestHandle(getOrDefaultHandle(request.getClientRequestHandle()));

        return invokeAction(SoapAction.GET_STATUS, request, GetStatusResponse.class);
    }

    //endregion

    //region Browse

    public BrowseResponse browse() {
        var browseRequest = new Browse();
        browseRequest.setBrowseFilter(BrowseFilter.ALL);

        return browse(browseRequest);
    }

    public BrowseResponse browse(Consumer<Browse> requestCustomizer) {
        var browseRequest = new Browse();
        browseRequest.setBrowseFilter(BrowseFilter.ALL);

        requestCustomizer.accept(browseRequest);

        return browse(browseRequest);
    }

    public BrowseResponse browse(Browse browseRequest) {
        browseRequest.setLocaleID(getOrDefaultLang(browseRequest.getLocaleID()));
        browseRequest.setClientRequestHandle(getOrDefaultHandle(browseRequest.getClientRequestHandle()));

        return invokeAction(SoapAction.BROWSE, browseRequest, BrowseResponse.class);
    }

    //endregion

    //region GetProperties

    public GetPropertiesResponse getProperties(GetProperties getPropertiesRequest) {
        getPropertiesRequest.setLocaleID(getOrDefaultLang(getPropertiesRequest.getLocaleID()));
        getPropertiesRequest.setClientRequestHandle(getOrDefaultHandle(getPropertiesRequest.getClientRequestHandle()));

        return invokeAction(SoapAction.GET_PROPERTIES, getPropertiesRequest, GetPropertiesResponse.class);
    }

    //endregion

    //region Read

    public ReadResponse read(Read readRequest) {
        if (readRequest.getOptions() == null) {
            readRequest.setOptions(new RequestOptions());
        }
        setDefaultOptions(readRequest.getOptions());

        return invokeAction(SoapAction.READ, readRequest, ReadResponse.class);
    }

    //endregion

    //region Write

    public WriteResponse write(Write writeRequest) {
        if (writeRequest.getOptions() == null) {
            writeRequest.setOptions(new RequestOptions());
        }
        setDefaultOptions(writeRequest.getOptions());

        return invokeAction(SoapAction.WRITE, writeRequest, WriteResponse.class);
    }

    //endregion

    //region Subscribe

    public SubscribeResponse subscribe(Subscribe subscribeRequest) {
        if (subscribeRequest.getOptions() == null) {
            subscribeRequest.setOptions(new RequestOptions());
        }
        setDefaultOptions(subscribeRequest.getOptions());

        return invokeAction(SoapAction.SUBSCRIBE, subscribeRequest, SubscribeResponse.class);
    }

    //endregion

    //region SubscriptionPolledRefresh

    public SubscriptionPolledRefreshResponse subscriptionPolledRefresh(SubscriptionPolledRefresh subscriptionRefresh) {
        if (subscriptionRefresh.getOptions() == null) {
            subscriptionRefresh.setOptions(new RequestOptions());
        }

        setDefaultOptions(subscriptionRefresh.getOptions());

        return invokeAction(
            SoapAction.SUBSCRIPTION_POLLED_REFRESH,
            subscriptionRefresh,
            SubscriptionPolledRefreshResponse.class
        );
    }

    //endregion

    //region SubscriptionCancel

    public SubscriptionCancelResponse subscriptionCancel(SubscriptionCancel subscriptionCancelRequest) {
        subscriptionCancelRequest.setClientRequestHandle(
            getOrDefaultHandle(subscriptionCancelRequest.getClientRequestHandle())
        );

        return invokeAction(
            SoapAction.SUBSCRIPTION_CANCEL,
            subscriptionCancelRequest,
            SubscriptionCancelResponse.class
        );
    }

    //endregion

    public String nextClientRequestHandle() {
        return String.valueOf(clientRequestHandles.getAndIncrement());
    }

    private <T> T invokeAction(SoapAction soapAction, Object requestPayload, Class<T> responseClass) {
        Object response = webServiceTemplate.marshalSendAndReceive(
            requestPayload,
            new SoapActionCallback(soapAction.getActionPath())
        );

        return responseClass.cast(response);
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
        return languageTag == null || languageTag.isEmpty() ? defaultLocale.toLanguageTag() : languageTag;
    }

    private String getOrDefaultHandle(String requestHandle) {
        return requestHandle == null || requestHandle.isEmpty() ? nextClientRequestHandle() : requestHandle;
    }

    public static OpcXmlDaClient.Builder newBuilder() {
        return new Builder();
    }

    private enum SoapAction {

        GET_STATUS("GetStatus"),
        BROWSE("Browse"),
        GET_PROPERTIES("GetProperties"),
        READ("Read"),
        WRITE("Write"),
        SUBSCRIBE("Subscribe"),
        SUBSCRIPTION_POLLED_REFRESH("SubscriptionPolledRefresh"),
        SUBSCRIPTION_CANCEL("SubscriptionCancel");

        private static final String ACTION_BASE = "http://opcfoundation.org/webservices/XMLDA/1.0/";

        private final String actionPath;

        SoapAction(String action) {
            this.actionPath = ACTION_BASE + action;
        }

        String getActionPath() {
            return actionPath;
        }

    }


    public static class Builder {

        private String serverUrl;
        private int connectTimeout = 5000;
        private int requestTimeout = 5000;
        private Locale defaultLocale = Locale.US;
        private WebServiceTemplate webServiceTemplate;

        public Builder setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setRequestTimeout(int requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder setDefaultLocale(Locale defaultLocale) {
            this.defaultLocale = defaultLocale;
            return this;
        }

        public Builder setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
            this.webServiceTemplate = webServiceTemplate;
            return this;
        }

        public OpcXmlDaClient build() {
            if (webServiceTemplate == null) {
                if (serverUrl == null) {
                    throw new IllegalArgumentException("serverUrl must be specified");
                }

                var marshaller = new Jaxb2Marshaller();
                marshaller.setContextPath("org.opcfoundation.xmlda");

                var messageSender = new HttpComponentsMessageSender();
                messageSender.setConnectionTimeout(connectTimeout);
                messageSender.setReadTimeout(requestTimeout);

                webServiceTemplate = new WebServiceTemplate();
                webServiceTemplate.setDefaultUri(serverUrl);
                webServiceTemplate.setMarshaller(marshaller);
                webServiceTemplate.setUnmarshaller(marshaller);
                webServiceTemplate.setMessageSender(messageSender);
            }

            return new OpcXmlDaClient(webServiceTemplate, defaultLocale);
        }

    }

}
