package com.digitalpetri.opc.xmlda.spring;

import com.digitalpetri.opc.xmlda.AbstractOpcXmlDaClientTest;
import com.digitalpetri.opc.xmlda.client.OpcXmlDaClient;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringOpcXmlDaClientConfiguration.class)
public class SpringOpcXmlDaClientTest extends AbstractOpcXmlDaClientTest {

    @Autowired
    private OpcXmlDaClient client;

    @Override
    protected OpcXmlDaClient getClient() {
        return client;
    }

}
