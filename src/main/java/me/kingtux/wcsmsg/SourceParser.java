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
import java.util.Base64;

public class SourceParser {

    public static void parse(File file, File export) throws Exception {
        if (!export.exists()) export.mkdirs();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//an instance of builder to parse the specified xml file
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        Node smses = doc.getElementsByTagName("smses").item(0);
        NodeList childNodes = smses.getChildNodes();
        if (childNodes == null) System.out.println("OUCH");
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
                        if (item1.getAttributes().getNamedItem("ct").getTextContent().equalsIgnoreCase("image/jpeg")) {
                            imageFile = base64.hashCode() + ".jpg";
                        } else if (item1.getAttributes().getNamedItem("ct").getTextContent().equalsIgnoreCase("image/png")) {
                            imageFile = base64.hashCode() + ".png";
                        } else {
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
        File output = new File(workingFolder, "texts.txt");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(output, true));
            bw.write(message);
            bw.newLine();
            bw.close();
            if (type == Type.SENT) {
                File outputTwo = new File(workingFolder, "texts-sent.txt");
                BufferedWriter bwTwo = new BufferedWriter(new FileWriter(outputTwo, true));
                bwTwo.write(message);
                bwTwo.newLine();
                bwTwo.close();
            } else if (type == Type.RECEIVED) {
                File outputTwo = new File(workingFolder, "texts-received.txt");
                BufferedWriter bwTwo = new BufferedWriter(new FileWriter(outputTwo, true));
                bwTwo.write(message);
                bwTwo.newLine();
                bwTwo.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }
}
