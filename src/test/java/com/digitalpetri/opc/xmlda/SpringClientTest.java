package com.digitalpetri.opc.xmlda;

import com.digitalpetri.opc.xmlda.client.OpcXmlDaClient;
import com.digitalpetri.opc.xmlda.client.OpcXmlDaClientConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OpcXmlDaClientConfiguration.class)
public class SpringClientTest extends AbstractOpcXmlDaClientTest {

    @Autowired
    private OpcXmlDaClient client;

    @Override
    protected OpcXmlDaClient getClient() {
        return client;
    }

}
