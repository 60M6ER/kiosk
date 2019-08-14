package Interface;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import properties.Props;

import javax.xml.soap.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Soap {
    public static String getMenuJson()  {

        SOAPMessage soapMessage = createSoapMessageMenu();
        String json = callSoapWebService(soapMessage);

        return json;
    }

    public static String RegistrationKiosk()  {

        SOAPMessage soapMessage = createSoapMessageRegistrationKiosk();

        if (soapMessage == null)
            return null;

        String json = callSoapWebService(soapMessage);

        if (json == null) {
            Props.setRezervAddress();
            json = callSoapWebService(soapMessage);
        }

        if (json == null)
            return "update";

        return json;
    }

    public static String newTalon (String usluga) {


        SOAPMessage soapMessage = createSoapMessageTalon(usluga);
        String json = callSoapWebService(soapMessage);

        return json;
    }

    private static void getSOAPBody(SOAPEnvelope envelope, String parameter, ArrayList<String> parameterList) throws SOAPException {
        if (parameter.equals("menu"))
            getSOAPBodyMenu(envelope);
        if (parameter.equals("talon"))
            getSOAPBodyTalon(envelope, parameterList);
        if (parameter.equals("RegistrationKiosk"))
            getSOAPBodyRegistrationKiosk(envelope);
    }

    private static void getSOAPBodyTalon(SOAPEnvelope envelope, ArrayList<String> parameterList) throws SOAPException {
        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("NewTalon", "que");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("QueueId", "que");
        soapBodyElem1.addTextNode(Props.getMACAddress());
        SOAPElement soapBodyElem12 = soapBodyElem.addChildElement("ServId", "que");
        soapBodyElem12.addTextNode(parameterList.get(0));
    }

    private static void getSOAPBodyMenu(SOAPEnvelope envelope) throws SOAPException {
        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("GetMenu", "que");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("QueueId", "que");
        soapBodyElem1.addTextNode(Props.getMACAddress());
    }

    private static void getSOAPBodyRegistrationKiosk(SOAPEnvelope envelope) throws SOAPException {
        // SOAP Body

        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("RegistrationKiosk", "que");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("IP", "que");
        soapBodyElem1.addTextNode(Props.getIPAddress());
        SOAPElement soapBodyElem12 = soapBodyElem.addChildElement("MAC", "que");
        soapBodyElem12.addTextNode(Props.getMACAddress());
    }

    private static SOAPMessage createSoapMessage(String parameter, ArrayList<String> parameterList) {
        SOAPMessage soapMessage = null;
        try {

            String soapAction = Props.getSoapAction();

            MessageFactory messageFactory = MessageFactory.newInstance();
            soapMessage = messageFactory.createMessage();
            //----------------- createSoapEnvelope ----------------------//
            SOAPPart soapPart = soapMessage.getSOAPPart();

            // SOAP Envelope
            SOAPEnvelope envelope = soapPart.getEnvelope();
            envelope.addNamespaceDeclaration("que", "Queue");

            // SOAP Body
            getSOAPBody(envelope, parameter, parameterList);

            //-------------------- createSoapEnvelope --------------------//
            MimeHeaders headers = soapMessage.getMimeHeaders();
            headers.addHeader("SOAPAction", soapAction);

            // Определение авторизации сервиса

            String loginPassword = Props.getWsLogin() + ":" + Props.getWsPassword();
            byte[] bytes = loginPassword.getBytes();
            String auth = new String(Base64.getMimeEncoder().encode(bytes));
            headers.addHeader("Authorization", "Basic " + auth);
            soapMessage.saveChanges();
        }
        catch (Exception e ) {
            Props.getLogger().log(Level.WARNING, "Ошибка при создании сообщения веб-сервису", e);
        }
        return soapMessage;
    }

    public static SOAPMessage createSoapMessageMenu ()  {
        return createSoapMessage("menu", null);
    }

    public static SOAPMessage createSoapMessageRegistrationKiosk ()  {

        return createSoapMessage("RegistrationKiosk", null);
    }

    public static SOAPMessage createSoapMessageTalon (String usluga)  {
        ArrayList<String> parameterList = new ArrayList<>();
        parameterList.add(usluga);

        return createSoapMessage("talon", parameterList);
    }

    private static String callSoapWebService(SOAPMessage soapMessage) {

        String soapEndpointUrl = Props.getSoapEndpointUrl();

        for (int i = 0; i < 10; i++){
            try {
                // Create SOAP Connection
                SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
                SOAPConnection soapConnection = soapConnectionFactory.createConnection();
                SOAPMessage soapResponse = soapConnection.call(soapMessage, soapEndpointUrl);

                //получение значения.
                NodeList nodes = soapResponse.getSOAPBody().getElementsByTagName("m:return");
                String someMsgContent = null;
                Node node = nodes.item(0);
                someMsgContent = node != null ? node.getTextContent() : "";

                //return soapResponse;
                soapConnection.close();
                return someMsgContent;

            } catch (Exception e) {
                Props.getLogger().log(Level.WARNING,
                        "Ошибка вызова веб сервиса.",
                        e);
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException e) {
                Props.getLogger().log(Level.WARNING, "Ошибка обработчика ожидания.", e);
            }
        }
        return null;
    }
}
