
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class XMLstaxParser {
    public static void main(String[] args) throws TransformerConfigurationException, IOException, SAXException, XMLStreamException {
        boolean ok = schemaValidator();
        if (ok) {
            File file = new File(Objects.requireNonNull(XMLstaxParser.class.getClassLoader().getResource("state.xml")).getFile());
            try {
                Root root = parseXMLFile(file);
                htmlGen(root);

            } catch (ParseException e) {
                System.out.println(e);
            }

            //htmlGen(issueList);
        }
    }

    public static boolean schemaValidator() throws SAXException, IOException, XMLStreamException {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(".\\src\\main\\resources\\state.xsd"));
            Validator validator = schema.newValidator();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(Files.newInputStream(Paths.get(".\\src\\main\\resources\\state.xml")));
            validator.validate(new StAXSource(reader));

            System.out.println("XML is valid");
            return true;
        } catch (Exception e) {
            System.out.println("XML is not valid");
            return false;
        }
    }

    public static Root parseXMLFile(File file) throws ParseException {
        Root root = new Root();
        State state = null;
        List<State> states = new ArrayList<>();
        Location location = new Location();
        List<Location> locations = new ArrayList<>();
        List<SensorValue> sensorValues = new ArrayList<>();
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        String input = "2022-06-05T20:32:12";
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(file));
            while (reader.hasNext()) {
                XMLEvent xmlEvent = reader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();
                    if (startElement.getName().getLocalPart().equals("state")) {
                        state = new State();
                        Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                        if (idAttr != null) {
                            state.id = (Integer.parseInt(idAttr.getValue()));
                        }
                    } else if (startElement.getName().getLocalPart().equals("time")) {
                        xmlEvent = reader.nextEvent();
                        state.time = parser.parse(xmlEvent.asCharacters().getData());
                    } else if (startElement.getName().getLocalPart().equals("sensor_value")) {
                        state.sensorValues = sensorValues;
                        state.sensorValues.add(parseSensorValue(reader, xmlEvent));
                    } else if (startElement.getName().getLocalPart().equals("location")) {
                        locations.add(parseLocation(reader, xmlEvent));
                    }
                }

                if (xmlEvent.isEndElement()) {
                    EndElement endElement = xmlEvent.asEndElement();
                    if (endElement.getName().getLocalPart().equals("state")) {
                        states.add(state);
                        sensorValues = new ArrayList<>();
                        ;
                    }
                }
            }

        } catch (FileNotFoundException | XMLStreamException exc) {
            exc.printStackTrace();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        root.states = states;
        root.location = locations;
        return root;
    }

    public static SensorValue parseSensorValue(XMLEventReader reader, XMLEvent xmlEvent) throws XMLStreamException {
        SensorValue sensorValue = new SensorValue();
        while (reader.hasNext()) {
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if (startElement.getName().getLocalPart().equals("sensor_value")) {
                    Attribute idAttr = startElement.getAttributeByName(new QName("data_type"));
                    if (idAttr != null) {
                        sensorValue.data_type = idAttr.getValue();
                    }
                } else if (startElement.getName().getLocalPart().equals("sid")) {
                    xmlEvent = reader.nextEvent();
                    sensorValue.id = Integer.parseInt(xmlEvent.asCharacters().getData());
                } else if (startElement.getName().getLocalPart().equals("value")) {
                    xmlEvent = reader.nextEvent();
                    sensorValue.value = Integer.parseInt(xmlEvent.asCharacters().getData());
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("sensor_value")) {
                    break;
                }
            }
            xmlEvent = reader.nextEvent();
        }
        return sensorValue;
    }

    public static Location parseLocation(XMLEventReader reader, XMLEvent xmlEvent) throws XMLStreamException {
        Location location = new Location();
        List<Mechamism> sensors = new ArrayList<>();
        List<Mechamism> devices = new ArrayList<>();
        while (reader.hasNext()) {
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if (startElement.getName().getLocalPart().equals("location")) {
                    Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                    if (idAttr != null) {
                        location.id = Integer.parseInt(idAttr.getValue());
                    }
                    idAttr = startElement.getAttributeByName(new QName("location_type"));
                    if (idAttr != null) {
                        location.locationType = idAttr.getValue();
                    }
                    idAttr = startElement.getAttributeByName(new QName("address"));
                    if (idAttr != null) {
                        location.address = idAttr.getValue();
                    }
                    idAttr = startElement.getAttributeByName(new QName("square"));
                    if (idAttr != null) {
                        location.square = Double.parseDouble(idAttr.getValue());
                    }
                } else if (startElement.getName().getLocalPart().equals("description")) {
                    xmlEvent = reader.nextEvent();
                    location.description = xmlEvent.asCharacters().getData();
                } else if (startElement.getName().getLocalPart().equals("sensor")) {
                    location.sensors = sensors;
                    location.sensors.add(parseSensor(reader, xmlEvent));
                } else if (startElement.getName().getLocalPart().equals("device")) {
                    location.devices = devices;
                    location.devices.add(parseDevice(reader, xmlEvent));
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("location")) {
                    break;
                }
            }
            xmlEvent = reader.nextEvent();
        }
        return location;
    }

    public static Mechamism parseSensor(XMLEventReader reader, XMLEvent xmlEvent) throws XMLStreamException {
        Mechamism mechamism = new Mechamism();
        while (reader.hasNext()) {
            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();
                if (startElement.getName().getLocalPart().equals("sensor")) {
                    Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                    if (idAttr != null) {
                        mechamism.id = Integer.parseInt(idAttr.getValue());
                    }
                } else if (startElement.getName().getLocalPart().equals("name")) {
                    xmlEvent = reader.nextEvent();
                    mechamism.name = xmlEvent.asCharacters().getData();
                }
            }
            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();
                if (endElement.getName().getLocalPart().equals("sensor")) {
                    break;
                }
            }
            xmlEvent = reader.nextEvent();
        }
        return mechamism;
    }

    public static Mechamism parseDevice(XMLEventReader reader, XMLEvent xmlEvent) throws XMLStreamException {
        Mechamism mechamism = new Mechamism();
        if (xmlEvent.isStartElement()) {
            StartElement startElement = xmlEvent.asStartElement();
            if (startElement.getName().getLocalPart().equals("device")) {
                Attribute idAttr = startElement.getAttributeByName(new QName("id"));
                if (idAttr != null) {
                    mechamism.id = Integer.parseInt(idAttr.getValue());
                }
                idAttr = startElement.getAttributeByName(new QName("device_name"));
                if (idAttr != null) {
                    mechamism.name = idAttr.getValue();
                }
            }
        }
        xmlEvent = reader.nextEvent();

        return mechamism;
    }


    public static void htmlGen(Root root) throws IOException, SAXException, TransformerConfigurationException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.ENGLISH);
        String encoding = "UTF-8";
        File file = new File(Objects.requireNonNull(XMLstaxParser.class.getClassLoader().getResource("example.xml")).getFile());
        FileOutputStream fos = new FileOutputStream(".\\myfile.html");
        OutputStreamWriter writer = new OutputStreamWriter(fos, encoding);
        StreamResult streamResult = new StreamResult(writer);

        SAXTransformerFactory saxFactory =
                (SAXTransformerFactory) TransformerFactory.newInstance();
        TransformerHandler tHandler = saxFactory.newTransformerHandler();
        tHandler.setResult(streamResult);

        Transformer transformer = tHandler.getTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");


        writer.write("<!DOCTYPE html>\n");
        writer.flush();
        tHandler.startDocument();
        tHandler.startElement("", "", "html", new AttributesImpl());
        tHandler.startElement("", "", "head", new AttributesImpl());
        tHandler.startElement("", "", "link rel=\"stylesheet\" href=\"mysite.css\"", new AttributesImpl());
        tHandler.startElement("", "", "title", new AttributesImpl());
        tHandler.characters("Issue".toCharArray(), 0, 5);
        tHandler.endElement("", "", "title");
        tHandler.startElement("", "", "title", new AttributesImpl());
        tHandler.characters("Issue".toCharArray(), 0, 5);
        tHandler.endElement("", "", "title");
        tHandler.endElement("", "", "head");
        tHandler.startElement("", "", "body", new AttributesImpl());

        tHandler.startElement("", "", "h1", new AttributesImpl());
        tHandler.characters("Помещения".toCharArray(), 0, "Помещения".length());
        tHandler.endElement("", "", "h1");
        //Таблица
        tHandler.startElement("", "", "table", new AttributesImpl());
        //Название задачи
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("ID помещения".toCharArray(), 0, "ID помещения".length());
        tHandler.endElement("", "", "td");
        //Статус
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("Тип помещения".toCharArray(), 0, "Тип помещения".length());
        tHandler.endElement("", "", "td");
        //Исполнитель
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("Адрес".toCharArray(), 0, "Адрес".length());
        tHandler.endElement("", "", "td");
        //Дата начала
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("Площадь".toCharArray(), 0, "Площадь".length());
        tHandler.endElement("", "", "td");


        Double allSqueare = 0.0;
        for (Location i : root.location) {
            tHandler.startElement("", "", "tr", new AttributesImpl());
            //Название задачи
            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(Integer.toString(i.id).toCharArray(), 0, Integer.toString(i.id).length());
            tHandler.endElement("", "", "td");
            //Статус
            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(i.locationType.toCharArray(), 0, i.locationType.length());
            tHandler.endElement("", "", "td");
            //Исполнитель
            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(i.address.toCharArray(), 0, i.address.length());
            tHandler.endElement("", "", "td");


            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(Double.toString(i.square).toCharArray(), 0, Double.toString(i.square).length());
            allSqueare += i.square;
            tHandler.endElement("", "", "td");

            tHandler.endElement("", "", "tr");
        }
        tHandler.startElement("", "", "tr", new AttributesImpl());
        tHandler.startElement("", "", "th", new AttributesImpl());
        tHandler.endElement("", "", "th");
        tHandler.startElement("", "", "th", new AttributesImpl());
        tHandler.endElement("", "", "th");
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("Всего задач".toCharArray(), 0, 11);
        tHandler.endElement("", "", "td");
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters(Double.toString(allSqueare).toCharArray(), 0, Double.toString(allSqueare).length());
        tHandler.endElement("", "", "td");
        tHandler.endElement("", "", "tr");

        tHandler.endElement("", "", "table");
        //Конец таблицы 1

        tHandler.startElement("", "", "h1", new AttributesImpl());
        tHandler.characters("Записи состояний".toCharArray(), 0, "Записи состояний".length());
        tHandler.endElement("", "", "h1");
        //Таблица 2
        tHandler.startElement("", "", "table", new AttributesImpl());
        //ID статуса
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("ID статуса".toCharArray(), 0, "ID статуса".length());
        tHandler.endElement("", "", "td");
        //Время записи
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("Время записи".toCharArray(), 0, "Время записи".length());
        tHandler.endElement("", "", "td");
        //Тип
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("Тип".toCharArray(), 0, "Тип".length());
        tHandler.endElement("", "", "td");
        //Показатель
        tHandler.startElement("", "", "td", new AttributesImpl());
        tHandler.characters("Показатель".toCharArray(), 0, "Показатель".length());
        tHandler.endElement("", "", "td");

        for (State i : root.states) {
            tHandler.startElement("", "", "tr", new AttributesImpl());
            //ID статуса
            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(Integer.toString(i.id).toCharArray(), 0, Integer.toString(i.id).length());
            tHandler.endElement("", "", "td");
            //Время записи
            tHandler.startElement("", "", "td", new AttributesImpl());
            tHandler.characters(formatter.format(i.time).toCharArray(), 0, formatter.format(i.time).length());
            tHandler.endElement("", "", "td");
            tHandler.endElement("", "", "tr");

            for (SensorValue ii : i.sensorValues) {
                tHandler.startElement("", "", "tr", new AttributesImpl());
                tHandler.startElement("", "", "th", new AttributesImpl());
                tHandler.endElement("", "", "th");
                tHandler.startElement("", "", "th", new AttributesImpl());
                tHandler.endElement("", "", "th");
                //Тип
                tHandler.startElement("", "", "td", new AttributesImpl());
                tHandler.characters(ii.data_type.toCharArray(), 0, ii.data_type.length());
                tHandler.endElement("", "", "td");
                //Показатель
                tHandler.startElement("", "", "td", new AttributesImpl());
                tHandler.characters(Integer.toString(ii.value).toCharArray(), 0, Integer.toString(ii.value).length());
                tHandler.endElement("", "", "td");
                tHandler.endElement("", "", "tr");
            }
        }

        tHandler.endElement("", "", "table");
        //Конец таблицы 2

        tHandler.endElement("", "", "body");
        tHandler.endElement("", "", "html");
        tHandler.endDocument();
        writer.close();

        fos.close();
    }

}
