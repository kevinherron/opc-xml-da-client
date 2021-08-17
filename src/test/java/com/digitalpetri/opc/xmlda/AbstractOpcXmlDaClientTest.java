package com.digitalpetri.opc.xmlda;

import java.util.List;
import javax.xml.namespace.QName;

import com.digitalpetri.opc.xmlda.client.OpcXmlDaClient;
import org.junit.Test;
import org.opcfoundation.xmlda.Browse;
import org.opcfoundation.xmlda.BrowseElement;
import org.opcfoundation.xmlda.BrowseResponse;
import org.opcfoundation.xmlda.GetProperties;
import org.opcfoundation.xmlda.GetPropertiesResponse;
import org.opcfoundation.xmlda.GetStatusResponse;
import org.opcfoundation.xmlda.ItemIdentifier;
import org.opcfoundation.xmlda.ItemProperty;
import org.opcfoundation.xmlda.ItemValue;
import org.opcfoundation.xmlda.OPCError;
import org.opcfoundation.xmlda.OPCQuality;
import org.opcfoundation.xmlda.Read;
import org.opcfoundation.xmlda.ReadRequestItem;
import org.opcfoundation.xmlda.ReadRequestItemList;
import org.opcfoundation.xmlda.ReadResponse;
import org.opcfoundation.xmlda.RequestOptions;
import org.opcfoundation.xmlda.ServerState;
import org.opcfoundation.xmlda.ServerStatus;
import org.opcfoundation.xmlda.Subscribe;
import org.opcfoundation.xmlda.SubscribeRequestItem;
import org.opcfoundation.xmlda.SubscribeRequestItemList;
import org.opcfoundation.xmlda.SubscribeResponse;
import org.opcfoundation.xmlda.SubscriptionCancel;
import org.opcfoundation.xmlda.SubscriptionCancelResponse;
import org.opcfoundation.xmlda.SubscriptionPolledRefresh;
import org.opcfoundation.xmlda.SubscriptionPolledRefreshResponse;
import org.opcfoundation.xmlda.Write;
import org.opcfoundation.xmlda.WriteRequestItemList;
import org.opcfoundation.xmlda.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opcfoundation.xmlda.BrowseFilter.ALL;
import static org.opcfoundation.xmlda.QualityBits.GOOD;

/**
 * @author Yuriy Tumakha
 */
public abstract class AbstractOpcXmlDaClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOpcXmlDaClientTest.class);
    private static final String ITEM_PATH = "";
    private static final String READ_ITEM_NAME = "Static.Limited Access.Read Only 2";
    private static final String WRITE_ITEM_NAME = "Static.Simple Types.String";
    private static final Double READ_ITEM_VALUE = 50000.0;

    protected abstract OpcXmlDaClient getClient();

    @Test
    public void testGetStatus() {
        GetStatusResponse statusResponse = getClient().getStatus();
        assertEquals("Server state isn't RUNNING",
            ServerState.RUNNING, statusResponse.getGetStatusResult().getServerState());
        ServerStatus serverStatus = statusResponse.getStatus();
        assertNotNull("response.status.vendorInfo is null", serverStatus.getVendorInfo());
        LOG.debug("Vendor: {}", serverStatus.getVendorInfo());
        LOG.debug("Product Version: {}", serverStatus.getProductVersion());
        LOG.debug("Status: {}", serverStatus.getStatusInfo());
        LOG.debug("State: {}", statusResponse.getGetStatusResult().getServerState());
        LOG.debug("Supported Locale IDs: {}", serverStatus.getSupportedLocaleIDs());
        LOG.debug("Supported Interfaces: {}", serverStatus.getSupportedInterfaceVersions());
    }

    @Test
    public void testBrowse() {
        Browse browseRequest = new Browse();
        browseRequest.setBrowseFilter(ALL);
        browseRequest.setReturnPropertyValues(true);
        browseRequest.setReturnErrorText(true);
        browseRequest.getPropertyNames().addAll(of("value", "accessRights", "description")
            .map(QName::valueOf).collect(toSet()));

        BrowseResponse browseResponse = getClient().browse(browseRequest);
        assertEquals("Server state isn't RUNNING",
            ServerState.RUNNING, browseResponse.getBrowseResult().getServerState());
        List<BrowseElement> elements = browseResponse.getElements();
        assertTrue("No items on server", elements.size() > 0);
        LOG.debug("Root items: {}", elements.stream().map(BrowseElement::getItemName).collect(toList()));
        elements.forEach(item ->
            LOG.debug("({},{} properties: {})",
                item.getItemName(), item.isHasChildren() ? " hasChildren," : "",
                item.getProperties().stream().map(this::displayProperty).collect(toList()))
        );
    }

    @Test
    public void testGetProperties() {
        ItemIdentifier itemIdentifier = new ItemIdentifier();
        itemIdentifier.setItemPath(ITEM_PATH);
        itemIdentifier.setItemName(READ_ITEM_NAME);

        GetProperties getPropertiesRequest = new GetProperties();
        getPropertiesRequest.getItemIDs().add(itemIdentifier);
        getPropertiesRequest.setReturnAllProperties(true);
        getPropertiesRequest.setReturnPropertyValues(true);
        getPropertiesRequest.setReturnErrorText(true);
        GetPropertiesResponse getPropertiesResponse = getClient().getProperties(getPropertiesRequest);
        printErrors(getPropertiesResponse.getErrors());
        assertTrue("No propertyLists found", getPropertiesResponse.getPropertyLists().size() > 0);
        getPropertiesResponse.getPropertyLists().forEach(item -> {
            LOG.info("ItemPath: \"{}\" / ItemName: \"{}\"", item.getItemPath(), item.getItemName());
            assertTrue("No Item Properties found", item.getProperties().size() > 0);
            item.getProperties().forEach(prop -> {
                    LOG.info(displayProperty(prop));
                    if (prop.getName().getLocalPart().equals("value")) {
                        assertEquals("Unexpected Item Value", READ_ITEM_VALUE, prop.getValue());
                    }
                }
            );
        });
    }

    @Test
    public void testRead() {
        ReadRequestItem readItem = new ReadRequestItem();
        readItem.setItemPath(ITEM_PATH);
        readItem.setItemName(READ_ITEM_NAME);
        readItem.setClientItemHandle("Test Read");

        ReadRequestItemList itemList = new ReadRequestItemList();
        itemList.getItems().add(readItem);

        Read readRequest = new Read();
        readRequest.setItemList(itemList);
        readRequest.setOptions(getDefaultRequestOptions());

        ReadResponse readResponse = getClient().read(readRequest);
        printErrors(readResponse.getErrors());
        assertTrue("No items to read", readResponse.getRItemList().getItems().size() > 0);
        readResponse.getRItemList().getItems().forEach(item -> {
            assertNotNull("No Item Value found", item.getValue());
            LOG.info("Value: {} (Quality: {})", item.getValue(),
                item.getQuality().getQualityField().value());
            assertEquals("Unexpected Item Value", READ_ITEM_VALUE, item.getValue());
            assertEquals("Quality isn't good", GOOD, item.getQuality().getQualityField());
        });
    }

    @Test
    public void testWrite() {
        String newValue = "Hello from Ukraine!";
        ItemValue writeItemValue = new ItemValue();
        writeItemValue.setItemPath(ITEM_PATH);
        writeItemValue.setItemName(WRITE_ITEM_NAME);
        writeItemValue.setValue(newValue);
        writeItemValue.setClientItemHandle(getClient().generateClientRequestHandle());

        WriteRequestItemList itemList = new WriteRequestItemList();
        itemList.getItems().add(writeItemValue);

        Write writeRequest = new Write();
        writeRequest.setItemList(itemList);
        writeRequest.setReturnValuesOnReply(true);
        writeRequest.setOptions(getDefaultRequestOptions());

        WriteResponse writeResponse = getClient().write(writeRequest);
        printErrors(writeResponse.getErrors());
        assertTrue("No items to write", writeResponse.getRItemList().getItems().size() > 0);
        writeResponse.getRItemList().getItems().forEach(item -> {
            assertNotNull("No Item Value found", item.getValue());
            LOG.info("Value: {} (Quality: {})", item.getValue(),
                item.getQuality() == null ? "" : item.getQuality().getQualityField().value());
            assertEquals("Unexpected Item Value", newValue, item.getValue().toString());
        });
    }

    @Test
    public void testSubscribe() {
        SubscribeResponse subscribeResponse = getClient().subscribe(createSubscribeRequest());
        printErrors(subscribeResponse.getErrors());
        assertNotNull("ServerSubHandle is null", subscribeResponse.getServerSubHandle());
        assertTrue("No items to subscribe", subscribeResponse.getRItemList().getItems().size() > 0);
        subscribeResponse.getRItemList().getItems().forEach(item -> {
            assertNotNull("No Item Value found", item.getItemValue());
            LOG.info("Value: {} (Quality: {})", item.getItemValue().getValue(),
                item.getItemValue().getQuality().getQualityField().value());
            assertEquals("Unexpected Item Value", READ_ITEM_VALUE, item.getItemValue().getValue());
            assertEquals("Quality isn't good", GOOD, item.getItemValue().getQuality().getQualityField());
        });
    }

    @Test
    public void testSubscriptionPolledRefresh() {
        SubscribeResponse subscribeResponse = getClient().subscribe(createSubscribeRequest());
        String serverSubHandle = subscribeResponse.getServerSubHandle();

        LOG.info("Make SubscriptionPolledRefresh for serverSubHandle = {}", serverSubHandle);

        SubscriptionPolledRefresh subscriptionRefresh = new SubscriptionPolledRefresh();
        subscriptionRefresh.getServerSubHandles().add(serverSubHandle);
        subscriptionRefresh.setReturnAllItems(true);

        SubscriptionPolledRefreshResponse subscriptionRefreshResponse =
            getClient().subscriptionPolledRefresh(subscriptionRefresh);
        printErrors(subscriptionRefreshResponse.getErrors());
        assertTrue("InvalidServerSubHandles isn't empty",
            subscriptionRefreshResponse.getInvalidServerSubHandles().isEmpty());
        assertTrue("No result items", subscriptionRefreshResponse.getRItemList().size() > 0);
        assertEquals("serverSubHandle in response doesn't equal with requested",
            serverSubHandle, subscriptionRefreshResponse.getRItemList().get(0).getSubscriptionHandle());
        subscriptionRefreshResponse.getRItemList().get(0).getItems().forEach(itemValue -> {
            LOG.info("Value: {} (Quality: {})", itemValue.getValue(),
                itemValue.getQuality().getQualityField().value());
            assertEquals("Unexpected Item Value", READ_ITEM_VALUE, itemValue.getValue());
            assertEquals("Quality isn't good", GOOD, itemValue.getQuality().getQualityField());
        });
    }

    @Test
    public void testSubscriptionCancel() {
        SubscribeResponse subscribeResponse = getClient().subscribe(createSubscribeRequest());
        String serverSubHandle = subscribeResponse.getServerSubHandle();

        LOG.info("Cancel Subscription for serverSubHandle = {}", serverSubHandle);

        SubscriptionCancel subscriptionCancelRequest = new SubscriptionCancel();
        subscriptionCancelRequest.setServerSubHandle(serverSubHandle);
        subscriptionCancelRequest.setClientRequestHandle("Test Cancel");

        SubscriptionCancelResponse cancelResponse = getClient().subscriptionCancel(subscriptionCancelRequest);
        assertEquals("ClientRequestHandles don't equal",
            subscriptionCancelRequest.getClientRequestHandle(), cancelResponse.getClientRequestHandle());
    }

    private Subscribe createSubscribeRequest() {
        Integer samplingRate = 3000;
        Integer pingRate = 10000;
        SubscribeRequestItem subscribeItem = new SubscribeRequestItem();
        subscribeItem.setItemPath(ITEM_PATH);
        subscribeItem.setItemName(READ_ITEM_NAME);
        subscribeItem.setRequestedSamplingRate(samplingRate);

        SubscribeRequestItemList itemList = new SubscribeRequestItemList();
        itemList.setRequestedSamplingRate(samplingRate);
        itemList.getItems().add(subscribeItem);

        Subscribe subscribeRequest = new Subscribe();
        subscribeRequest.setItemList(itemList);
        subscribeRequest.setSubscriptionPingRate(pingRate);
        subscribeRequest.setReturnValuesOnReply(true);
        return subscribeRequest;
    }

    private RequestOptions getDefaultRequestOptions() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setReturnDiagnosticInfo(true);
        requestOptions.setReturnErrorText(true);
        requestOptions.setReturnItemName(true);
        requestOptions.setReturnItemPath(true);
        requestOptions.setReturnItemTime(true);
        return requestOptions;
    }

    private boolean printErrors(List<OPCError> errors) {
        boolean hasErrors = errors.size() > 0;
        if (hasErrors) {
            errors.forEach(opcError ->
                LOG.error("{}: {}", opcError.getID().getLocalPart(), opcError.getText()));
        }
        return hasErrors;
    }

    private String displayProperty(ItemProperty prop) {
        return prop.getName().getLocalPart() + ": " + displayValue(prop.getValue());
    }

    private Object displayValue(Object value) {
        if (value instanceof OPCQuality) {
            return ((OPCQuality) value).getQualityField().value();
        } else {
            return value;
        }
    }

}
