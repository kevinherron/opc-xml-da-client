package com.digitalpetri.opc.xmlda;

import com.digitalpetri.opc.xmlda.client.OpcXmlDaClient;

public class OpcXmlDaClientTest extends AbstractOpcXmlDaClientTest {

    @Override
    protected OpcXmlDaClient getClient() {
        return OpcXmlDaClient.newBuilder()
            .setServerUrl("http://info.advosol.com/XMLDADemo/XML_Sim/opcxmldaserver.asmx")
            .build();
    }

}
