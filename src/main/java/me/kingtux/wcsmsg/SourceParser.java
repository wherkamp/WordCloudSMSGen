package me.kingtux.wcsmsg;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SourceParser {

    public static void parse(File file, File export) throws Exception {
        if (!export.exists()) export.mkdirs();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        Node allMessages = doc.getElementsByTagName("smses").item(0);
        if (allMessages == null) {
            throw new IllegalArgumentException("No Messages Found");
        }

        NodeList childNodes = allMessages.getChildNodes();
        if (childNodes.getLength() == 0) {
            throw new IllegalArgumentException("No Messages Found");
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getAttributes() == null) continue;
            if (item.getAttributes().getNamedItem("contact_name") == null) continue;
            String who = item.getAttributes().getNamedItem("contact_name").getTextContent();
            File workingFolder = new File(export, who);
            if (!workingFolder.exists()) workingFolder.mkdir();
            if (item.getNodeName().equals("sms")) {
                writeMessage(who, item.getAttributes().getNamedItem("body").getTextContent(), workingFolder, Type.fromID(item.getAttributes().getNamedItem("type").getTextContent()));
            } else if (item.getNodeName().equals("mms")) {

                NodeList mmsNodes = null;
                for (int j = 0; j < item.getChildNodes().getLength(); j++) {
                    if (item.getChildNodes().item(j).getNodeName().equals("parts")) {
                        mmsNodes = item.getChildNodes().item(j).getChildNodes();
                    }
                }
                if (mmsNodes == null) continue;
                for (int j = 0; j < mmsNodes.getLength(); j++) {
                    Node item1 = mmsNodes.item(j);
                    if (item1.getAttributes() == null) continue;
                    if (item1.getAttributes().getNamedItem("ct") == null) continue;
                    if (item1.getAttributes().getNamedItem("ct").getTextContent().startsWith("image")) {
                        String base64 = item1.getAttributes().getNamedItem("data").getTextContent();
                        byte[] decodedBytes = Base64.getDecoder().decode(base64);
                        String imageFile;
                        String ct = item1.getAttributes().getNamedItem("ct").getTextContent();
                        if (ct.equalsIgnoreCase("image/jpeg")) {
                            imageFile = base64.hashCode() + ".jpg";
                        } else if (ct.equalsIgnoreCase("image/png")) {
                            imageFile = base64.hashCode() + ".png";
                        } else {
                            Main.LOGGER.warn(String.format("No handler is available for the type: %s", ct));
                            continue;
                        }
                        File imageFolder = new File(workingFolder, "images");
                        if (!imageFolder.exists()) imageFolder.mkdir();
                        File imageFileFile = new File(imageFolder, imageFile);
                        FileUtils.writeByteArrayToFile(imageFileFile, decodedBytes);
                    } else if (item1.getAttributes().getNamedItem("ct").getTextContent().equalsIgnoreCase("text/plain")) {
                        Node msg_box = item.getAttributes().getNamedItem("msg_box");
                        writeMessage(who, item1.getAttributes().getNamedItem("text").getTextContent(), workingFolder, Type.fromID(msg_box.getTextContent()));

                    }
                }
            }
        }
    }

    private static void writeMessage(String who, String message, File workingFolder, Type type) {
        String[] content = cleanupText(message.split(" "));
        try {
            if (type == Type.SENT) {
                File texts = new File(workingFolder, "texts-sent.txt");
                File textsSplit = new File(workingFolder, "texts-sent.split.txt");
                appendText(texts, message);
                appendText(textsSplit, content);
            } else if (type == Type.RECEIVED) {
                File texts = new File(workingFolder, "texts-received.txt");
                File textsSplit = new File(workingFolder, "texts-sent.received.txt");
                appendText(texts, message);
                appendText(textsSplit, content);
            }
            File texts = new File(workingFolder, "texts.txt");
            File textsSplit = new File(workingFolder, "texts.split.txt");

            appendText(texts, message);
            appendText(textsSplit, content);

        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    private static String[] cleanupText(String[] s) {
        List<String> stringList = new ArrayList<>();
        for (String s1 : s) {
            String value = s1.toLowerCase();
            //TODO check for banned words

            stringList.add(value);
        }
        return stringList.toArray(String[]::new);
    }

    public static void appendText(File file, String[] content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            for (String s : content) {
                bw.newLine();
                bw.write(s);
            }
            bw.newLine();
        }
    }

    public static void appendText(File file, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(content);
            bw.newLine();
        }
    }
}
