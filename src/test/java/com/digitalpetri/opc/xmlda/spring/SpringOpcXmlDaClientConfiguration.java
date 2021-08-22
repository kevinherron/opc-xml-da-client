package com.digitalpetri.opc.xmlda.spring;

import com.digitalpetri.opc.xmlda.client.OpcXmlDaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

/**
 * OPC XML-DA SOAP Client Configuration.
 *
 * @author Yuriy Tumakha
 */
@Configuration
public class SpringOpcXmlDaClientConfiguration {

    private static final String SERVER_URL_PROPERTY = "opc.xmlda.server.url";

    private final Environment env;

    @Autowired
    public SpringOpcXmlDaClientConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("org.opcfoundation.xmlda");
        return marshaller;
    }

    @Bean
    public WebServiceMessageSender messageSender() {
        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();
        messageSender.setConnectionTimeout(5000);
        messageSender.setReadTimeout(5000);
        return messageSender;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        String serverUrl = env.getRequiredProperty(SERVER_URL_PROPERTY);

        var template = new WebServiceTemplate();
        template.setDefaultUri(serverUrl);
        template.setMarshaller(marshaller());
        template.setUnmarshaller(marshaller());
        template.setMessageSender(messageSender());

        return template;
    }

    @Bean
    public OpcXmlDaClient opcXmlDaClient() {
        return new OpcXmlDaClient(webServiceTemplate());
    }

}
