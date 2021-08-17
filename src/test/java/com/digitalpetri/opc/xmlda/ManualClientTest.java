package com.digitalpetri.opc.xmlda;

import java.util.Locale;

import com.digitalpetri.opc.xmlda.client.OpcXmlDaClient;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

public class ManualClientTest extends AbstractOpcXmlDaClientTest {

    @Override
    protected OpcXmlDaClient getClient() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("org.opcfoundation.xmlda");

        OpcXmlDaClient client = new OpcXmlDaClient();
        client.setDefaultUri("http://info.advosol.com/XMLDADemo/XML_Sim/opcxmldaserver.asmx");
        client.setDefaultLocale(Locale.US);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);

        return client;
    }

}
