/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxerces;

import dom.GetElementsByTagName;
import dom.ParserWrapper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xerces.xni.QName;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/**
 *
 * @author Renato
 */
public class JXerces {
      private static String c = "jXerces v1.0 - 15/04/2018 - Copyright rcamargo";
      protected static final String DEFAULT_PARSER_NAME = "dom.wrappers.Xerces";
      protected static final String filtroicon = "<img src=\"./filtro.png\" />";
      protected static String conditional_label = "";
      
      protected static   String label = "";
      protected static   String FEFiltros = "";
      protected static   String ModFiltros = "";
      protected static   String conditionallabel = "";
      protected static   String alias= "";
      protected static   String reference = "";
      protected static   String value = "";
      protected static   String declare = "";
      protected static   String sParentType = "";
      protected static   String sItemType = "";
      protected static   String objectReferenceLabel = "";
      protected static   String filePath = "";
      protected static   String fileName = "";
      protected static   String filtroconditional = "";
      protected static   String filtrocall = "";
      protected static   String filtrocommand = "";
      protected static   String when = "";
      protected static   String param = "";  
      protected static   String database_file = "";
      protected static   boolean _debug = false;
      
      private static int count_dm;
      private static int step_dm;
      private static PrintWriter writer;
      private static List<XMLElement> Elementos = new ArrayList<>();
      
      private static void ProcessCall(String XMLFile, String TargetFile, String FrontEnd, String srcModule,Element oElement)
      {
            XMLElement SingleElement = new XMLElement();
            
            label = oElement.getAttribute("label");
            Element subDataManip = getElement("subDataManip",oElement);
            if ( subDataManip != null )
            {
                 objectReferenceLabel = subDataManip.getAttribute("objectReferenceLabel");
            }

            Element dataManipXml = getElement("dataManipXml",oElement);
            if ( dataManipXml != null )
            {
                 filePath = dataManipXml.getAttribute("filePath");
                 fileName = dataManipXml.getAttribute("fileName");
            }
            SingleElement.setFilterType(sParentType);
            Element whenexpr = getElement("when",oElement);
            if ( whenexpr != null )
            {
                if ( whenexpr.getAttribute("objectReferenceLabel").toLowerCase().equals("expr"))
                {
                    Element expression = getElement("expr",whenexpr);
                    if ( expression != null ) {
                         filtrocall = expression.getAttribute("value");
                         SingleElement.setFilterType("IF");
                    }
                }
//                if(! filtroconditional.equals("")) filtrocall = "<b style=\"color:LightGray;\">" + filtroconditional + "</b> && " + filtrocall;
            }
            param = getCallParams(oElement);
            SingleElement.setDataManipNumber(count_dm);
            SingleElement.setStepInDataManipNumber(step_dm);
            SingleElement.setContainer(sParentType);
            SingleElement.setLabel(label);
            SingleElement.setObject(objectReferenceLabel);
            SingleElement.setParameters(param);
            SingleElement.setObjectType("call");
            SingleElement.setBlock((sParentType.equals("if") || sParentType.equals("else-if") || sParentType.equals("else")));
            SingleElement.setLink((filePath.equals("") ? "" : filePath + "/" + fileName));
            SingleElement.setBlockFilter(filtroconditional);
            SingleElement.setObjectFilter(filtrocall);
            SingleElement.setXmlSourceFile(XMLFile);
            SingleElement.setXmlModule(srcModule);
            SingleElement.setXmlFrontEnd(FrontEnd);
            SingleElement.setTargetFile(TargetFile);
            Elementos.add(SingleElement);
            filtrocall="";
      }
      
      private static void ProcessCommand(String XMLFile, String TargetFile, String FrontEnd, String srcModule,Element oElement)
      {
            XMLElement SingleElement = new XMLElement();
            
            label = oElement.getAttribute("label");
            objectReferenceLabel = oElement.getAttribute("objectReferenceLabel");
            SingleElement.setFilterType(sParentType);
            Element whenexpr = getElement("when",oElement);
            if ( whenexpr != null )
            {
                if ( whenexpr.getAttribute("objectReferenceLabel").toLowerCase().equals("expr"))
                {
                    Element expression = getElement("expr",whenexpr);
                    if ( expression != null ) {
                        filtrocommand = expression.getAttribute("value");
                        SingleElement.setFilterType("IF");
                    }
                }
            }
            param = getCommandParams(oElement);
            SingleElement.setDataManipNumber(count_dm);
            SingleElement.setStepInDataManipNumber(step_dm);
            SingleElement.setContainer(sParentType);
            SingleElement.setLabel(label);
            SingleElement.setObject(objectReferenceLabel);
            SingleElement.setParameters(param);
            SingleElement.setObjectType("command");
            SingleElement.setBlock((sParentType.equals("if") || sParentType.equals("else-if") || sParentType.equals("else")));
            SingleElement.setBlockFilter(filtroconditional);
            SingleElement.setObjectFilter(filtrocommand);
            SingleElement.setXmlSourceFile(XMLFile);
            SingleElement.setXmlModule(srcModule);
            SingleElement.setXmlFrontEnd(FrontEnd);
            SingleElement.setTargetFile(TargetFile);
            Elementos.add(SingleElement);            
            filtrocommand="";
      }
      
      private static Element getElement( String sElementName, Element oParentElement) {
          
          NodeList oNodeList  = oParentElement.getChildNodes();
          
          for ( int iIndex = 0; iIndex < oNodeList.getLength(); iIndex++) {
              if ( oNodeList.item(iIndex).getNodeName().toLowerCase().equals( sElementName.toLowerCase() ))
                  return (Element) oNodeList.item(iIndex);
          }
          return null;
      }
      
      private static String getCommandParams( Element oParentElement) {
          
          NodeList oNodeList  = oParentElement.getChildNodes();
          String sParameters = ".:";
          for ( int iIndex = 0; iIndex < oNodeList.getLength(); iIndex++) {
              if ( oNodeList.item(iIndex).getNodeType() == Node.ELEMENT_NODE)  {
                    Element oCommand = (Element) oNodeList.item(iIndex);
                    if(!(oCommand.getNodeName().toLowerCase().equals("datamanipxml") || oCommand.getNodeName().toLowerCase().equals("subdatamanip") || oCommand.getNodeName().toLowerCase().equals("when")))
                        sParameters = sParameters + oCommand.getNodeName() + "=[" + oCommand.getAttribute("value") +  "]" + " :";
              }
          }
          sParameters = sParameters + ".";
          return sParameters;
      }
      
      private static String getCallParams( Element oParentElement) {
          
          NodeList oNodeList  = oParentElement.getChildNodes();
          String sParameters = ".:";
          for ( int iIndex = 0; iIndex < oNodeList.getLength(); iIndex++) {
              if ( oNodeList.item(iIndex).getNodeType() == Node.ELEMENT_NODE)  {
                    Element oCommand = (Element) oNodeList.item(iIndex);
                    
                    if(!(oCommand.getNodeName().toLowerCase().equals("datamanipxml") || oCommand.getNodeName().toLowerCase().equals("subdatamanip") || oCommand.getNodeName().toLowerCase().equals("when")))
                        sParameters = sParameters + oCommand.getNodeName() + "=[" + oCommand.getAttribute("value") +  "]" + " :";
              }
          }
          sParameters = sParameters + ".";
          return sParameters;
      }
   
      private static void ProcessElement(String XMLFile, String TargetFile, String FrontEnd, String srcModule, Element e, int depth) {
        Element firstElementChild;
        
        conditionallabel = "";
        label = "";
        alias= "";
        reference = "";
        value = "";
        declare = "";
        sParentType = "";
        sItemType = "";
        objectReferenceLabel = "";
        filePath = "";
        fileName = "";
        filtroconditional = "";
        when = "";
        param = "";          
        do {
                ElementTraversal et = (ElementTraversal) e;
                Element nodeparent = null;
                
                if ( e.getParentNode().getNodeType() == Node.ELEMENT_NODE) 
                    nodeparent = (Element) e.getParentNode();
                
                sParentType = e.getParentNode().getNodeName().toLowerCase();
                sItemType = e.getNodeName().toLowerCase();
                alias = e.getAttribute("alias");
                reference = e.getAttribute("reference");
                label = e.getAttribute("label");
                value = e.getAttribute("value");
                objectReferenceLabel = e.getAttribute("objectReferenceLabel");
                filePath = e.getAttribute("filePath");
                fileName = e.getAttribute("fileName");
               
                if (_debug)
                    System.out.println(depth + ". => " + sParentType + " <= " + sItemType);
                
                if(sItemType.equals("datamanip"))
                {
                    count_dm++;
                    step_dm=0;
                    XMLElement SingleElement = new XMLElement();                    
                    SingleElement.setDataManipNumber(count_dm);
                    SingleElement.setStepInDataManipNumber(step_dm);
                    SingleElement.setContainer("XML");
                    SingleElement.setLabel(label);
                    SingleElement.setObject(label);
                    SingleElement.setObjectType("datamanip");
                    SingleElement.setXmlSourceFile(XMLFile);
                    SingleElement.setXmlModule(srcModule);
                    SingleElement.setXmlFrontEnd(FrontEnd);
                    SingleElement.setTargetFile(TargetFile);
                    Elementos.add(SingleElement);
                }
                
                if(sItemType.equals("fieldsetreference")) 
                {
                    //step_dm++;
                    XMLElement SingleElement = new XMLElement(); 
                    SingleElement.setDataManipNumber(count_dm);
                    SingleElement.setStepInDataManipNumber(step_dm);
                    SingleElement.setContainer(sParentType);
                    SingleElement.setLabel(alias+"="+reference);
                    SingleElement.setObject(alias+"="+reference);
                    SingleElement.setObjectType("fieldsetreference");
                    SingleElement.setXmlSourceFile(XMLFile);
                    SingleElement.setXmlModule(srcModule);
                    SingleElement.setXmlFrontEnd(FrontEnd);
                    SingleElement.setTargetFile(TargetFile);
                    Elementos.add(SingleElement);                    
		}
                
                if(sItemType.equals("field"))
                {
                    step_dm++;
                    declare = nodeparent.getAttribute("label");
                    XMLElement SingleElement = new XMLElement(); 
                    SingleElement.setDataManipNumber(count_dm);
                    SingleElement.setStepInDataManipNumber(step_dm);
                    SingleElement.setContainer(sParentType);
                    SingleElement.setLabel(label);
                    SingleElement.setObject(declare + "." + label + (value.equals("")? "" : "="+value));
                    SingleElement.setObjectType("field");
                    SingleElement.setXmlSourceFile(XMLFile);
                    SingleElement.setXmlModule(srcModule);
                    SingleElement.setXmlFrontEnd(FrontEnd);
                    SingleElement.setTargetFile(TargetFile);
                    Elementos.add(SingleElement);
		}
                
                if(sItemType.equals("call"))
                {
                    if(!(sParentType.equals("if") || sParentType.equals("else-if") || sParentType.equals("else")))
                    {
                        step_dm++;
                        ProcessCall(XMLFile, TargetFile, FrontEnd, srcModule,e);
                    }
		}
                
                if(sItemType.equals("command"))
                {
                    if(!(sParentType.equals("if") || sParentType.equals("else-if") || sParentType.equals("else")))
                    {                    
                        step_dm++;
                        ProcessCommand(XMLFile, TargetFile, FrontEnd, srcModule,e);
                    }
		}
                
                if(sItemType.equals("conditional-block"))
                {
                    step_dm++;
                    conditionallabel = e.getAttribute("label");
                    NodeList oNodeList  = e.getChildNodes();
                    for ( int iIndex = 0; iIndex < oNodeList.getLength(); iIndex++) {
                        if(_debug) System.out.println(oNodeList.item(iIndex).getNodeName());
                        if ( oNodeList.item(iIndex).getNodeName().toLowerCase().equals("if"))
                        {
                                NodeList oNodeListif  = oNodeList.item(iIndex).getChildNodes();
                                sParentType = "if";
                                filtroconditional = "";
                                filtrocommand="";
                                filtrocall="";
                                for ( int iIndexif = 0; iIndexif < oNodeListif.getLength(); iIndexif++) {
                                    if(_debug) System.out.println(oNodeListif.item(iIndexif).getNodeName());
                                    if ( oNodeListif.item(iIndexif).getNodeName().toLowerCase().equals("expr"))
                                    {
                                        Element filtroif = (Element) oNodeListif.item(iIndexif);
                                        filtroconditional = filtroif.getAttribute("value");                            
                                    }                                    
                                    if ( oNodeListif.item(iIndexif).getNodeName().toLowerCase().equals("command"))
                                    {
                                        ProcessCommand(XMLFile, TargetFile, FrontEnd, srcModule,(Element)oNodeListif.item(iIndexif));
                                        filtrocommand="";
                                    }
                                    if ( oNodeListif.item(iIndexif).getNodeName().toLowerCase().equals("call"))
                                    {
                                        ProcessCall(XMLFile, TargetFile, FrontEnd, srcModule,(Element)oNodeListif.item(iIndexif));
                                        filtrocall="";
                                    }
                                }
                        }
                        if ( oNodeList.item(iIndex).getNodeName().toLowerCase().equals("else-if"))
                        {
                                NodeList oNodeListelseif  = oNodeList.item(iIndex).getChildNodes();
                                sParentType = "else-if";
                                filtroconditional = "";
                                filtrocommand="";
                                filtrocall="";                                
                                for ( int iIndexelseif = 0; iIndexelseif < oNodeListelseif.getLength(); iIndexelseif++) {
                                    if(_debug) System.out.println(oNodeListelseif.item(iIndexelseif).getNodeName());
                                    if ( oNodeListelseif.item(iIndexelseif).getNodeName().toLowerCase().equals("expr"))
                                    {
                                        Element filtroelseif = (Element) oNodeListelseif.item(iIndexelseif);
                                        filtroconditional = filtroelseif.getAttribute("value");                            
                                    }
                                    if ( oNodeListelseif.item(iIndexelseif).getNodeName().toLowerCase().equals("command"))
                                    {
                                        ProcessCommand(XMLFile, TargetFile, FrontEnd, srcModule,(Element)oNodeListelseif.item(iIndexelseif));
                                        filtrocommand="";
                                    }
                                    if ( oNodeListelseif.item(iIndexelseif).getNodeName().toLowerCase().equals("call"))
                                    {
                                        ProcessCall(XMLFile, TargetFile, FrontEnd, srcModule,(Element)oNodeListelseif.item(iIndexelseif));
                                        filtrocall="";
                                    }
                                }
                        }
                        if ( oNodeList.item(iIndex).getNodeName().toLowerCase().equals("else"))
                        {
                                NodeList oNodeListelse  = oNodeList.item(iIndex).getChildNodes();
                                sParentType = "else";
                                filtroconditional = "";
                                filtrocommand="";
                                filtrocall="";                                
                                for ( int iIndexelse = 0; iIndexelse < oNodeListelse.getLength(); iIndexelse++) {
                                    if(_debug) System.out.println(oNodeListelse.item(iIndexelse).getNodeName());
                                    if ( oNodeListelse.item(iIndexelse).getNodeName().toLowerCase().equals("command"))
                                    {
                                        filtroconditional = "ELSE";
                                        ProcessCommand(XMLFile, TargetFile, FrontEnd, srcModule,(Element)oNodeListelse.item(iIndexelse));
                                        filtrocommand="";
                                    }
                                    if ( oNodeListelse.item(iIndexelse).getNodeName().toLowerCase().equals("call"))
                                    {
                                        filtroconditional = "ELSE";
                                        ProcessCall(XMLFile, TargetFile, FrontEnd, srcModule,(Element)oNodeListelse.item(iIndexelse));
                                        filtrocall="";
                                    }                                    
                                }
                        }                                
                    }
                    filtroconditional = "";
		}                
               
                firstElementChild = et.getFirstElementChild();
                if (firstElementChild != null) {
                    ProcessElement(XMLFile, TargetFile, FrontEnd, srcModule, firstElementChild, depth + 1);
                }
                e = et.getNextElementSibling();
        } while (e != null);
    }
    
    public static void ProcessXML(String XMLFile, String TargetFile, String FrontEnd, String srcModule) throws FileNotFoundException, UnsupportedEncodingException
    {
          Elementos.clear();
                    
          try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setExpandEntityReferences(false);
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(XMLFile);
            
            count_dm = 0;
            DOMImplementation domImpl = doc.getImplementation();
            
            if (domImpl.hasFeature("ElementTraversal", "1.0")) {
                ProcessElement(XMLFile,TargetFile,FrontEnd,srcModule, doc.getDocumentElement(), 0);
            }
            else {
                System.err.println("The DOM implementation does not claim support for ElementTraversal.");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
        if( Elementos.size() == 0) {
            System.out.println("...... Discarded - XML has no expected tokens" );
            return;
        }
        
        System.out.println("... Writing in "  + TargetFile);
        writer = new PrintWriter(TargetFile, "UTF-8");

        writer.println("<!DOCTYPE html>");
        writer.println("<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
        writer.println("<h2>:. " + XMLFile + "</h2><hr>");
        writer.println("<style> .Cabecalho { background-color: #4CAF50; color: white; }");
        writer.println(".collapsible { background-color: #777; color: white; cursor: pointer; padding: 18px; width: 100%; border: none; text-align: left; outline: none; font-size: 15px; }");
        writer.println(".active, .collapsible:hover { background-color: #555; }");
        writer.println(".content {  padding: 0 18px; display: none; overflow: hidden; }");
        writer.println("table { font-family: Courier; font-size:6; border-collapse: collapse; border-spacing: 0; width: 100%; border: 1px solid #ddd; font-size: 15px; }");
        writer.println("th, td { font-family: Courier ;font-size:6 ; text-align: left; padding: 4px; }");
        writer.println("tr:nth-child(even) { font-family: Courier ; font-size=6; background-color: #f2f2f2 }");
        writer.println("</style></head><body>");
        
        for ( int iIndex = 0; iIndex < Elementos.size(); iIndex++)
        {
            if(_debug) System.out.println(Elementos.get(iIndex).toString());
            if( Elementos.get(iIndex).getObjectType().equals("datamanip")) {
                if (iIndex > 0) writer.println("</table>\n</div>");
                String DataManipParameters = ".:";
                for ( int iIndex2 = iIndex+1; iIndex2 < Elementos.size(); iIndex2++ ) {
                    if(Elementos.get(iIndex2).getObjectType().equals("fieldsetreference") && Elementos.get(iIndex2).getDataManipNumber() == Elementos.get(iIndex).getDataManipNumber())
                        DataManipParameters = DataManipParameters + Elementos.get(iIndex2).getObject() + " :";
                    else
                        break;
                }
                DataManipParameters =  DataManipParameters + ".";
                writer.printf("<button class=\"collapsible\"> %03d.%s <b style=\"color:Yellow;\">(%s)</b></button>\n<div class=\"content\">\n<table>",Elementos.get(iIndex).getDataManipNumber(),Elementos.get(iIndex).getObject(),DataManipParameters);
            }
            if( Elementos.get(iIndex).getObjectType().equals("field"))
            {
                writer.printf("<tr><td><b style=\"color:MediumSeaGreen;\">%03d.%03d </b>%s</td></tr>\n",Elementos.get(iIndex).getDataManipNumber(),Elementos.get(iIndex).getStepInDataManipNumber(),Elementos.get(iIndex).getObject());
            }              
            if( Elementos.get(iIndex).getObjectType().equals("call"))
            {
                boolean bForwardFlag = false;
                if (!Elementos.get(iIndex).getObjectFilter().equals("") || !Elementos.get(iIndex).getBlockFilter().equals("")) {
                    String sMasterFilter;
                    if ( !Elementos.get(iIndex).getBlockFilter().equals("") && !Elementos.get(iIndex).getBlockFilter().equals("ELSE")) {
                        sMasterFilter = Elementos.get(iIndex).getBlockFilter() + (Elementos.get(iIndex).getObjectFilter().equals("") ? "" : " && ") + Elementos.get(iIndex).getObjectFilter();
                    } else sMasterFilter = Elementos.get(iIndex).getObjectFilter();
                    writer.printf("<tr><td><table>\n");
                    writer.printf("<tr><td class=Cabecalho> %s %s</td></tr>\n", Elementos.get(iIndex).getFilterType().toUpperCase(), sMasterFilter);
                    bForwardFlag = true;
                }
                writer.printf("<tr><td>%s<b style=\"color:MediumSeaGreen;\">%03d.%03d </b>%s===> <b style=\"color:DodgerBlue;\">%s</b>(<b style=\"color:Violet;\">%s</b>)<b style=\"color:DodgerBlue;\">%s</b></td></tr>\n",( bForwardFlag ? "&nbsp;&nbsp;&nbsp;&nbsp;" : "" ), Elementos.get(iIndex).getDataManipNumber(),Elementos.get(iIndex).getStepInDataManipNumber(),Elementos.get(iIndex).getLabel(),Elementos.get(iIndex).getObject(),Elementos.get(iIndex).getParameters(),(Elementos.get(iIndex).getLink().equals("")?"":"==> " + Elementos.get(iIndex).getLink()));
                if(bForwardFlag)
                    writer.printf("</table></td></tr>\n");
            }
            if( Elementos.get(iIndex).getObjectType().equals("command"))
            {
                boolean bForwardFlag = false;
                if (!Elementos.get(iIndex).getObjectFilter().equals("") || !Elementos.get(iIndex).getBlockFilter().equals("")) {
                    String sMasterFilter;
                    if ( !Elementos.get(iIndex).getBlockFilter().equals("") && !Elementos.get(iIndex).getBlockFilter().equals("ELSE")) {
                        sMasterFilter = Elementos.get(iIndex).getBlockFilter() + (Elementos.get(iIndex).getObjectFilter().equals("") ? "" : " && ") + Elementos.get(iIndex).getObjectFilter();
                    } else sMasterFilter = Elementos.get(iIndex).getObjectFilter();
                    writer.printf("<tr><td><table>\n");
                    writer.printf("<tr><td class=Cabecalho> %s %s</td></tr>\n", Elementos.get(iIndex).getFilterType().toUpperCase(), sMasterFilter);
                    bForwardFlag = true;
                }
                writer.printf("<tr><td>%s<b style=\"color:MediumSeaGreen;\">%03d.%03d </b>%s===> <b style=\"background-color:DodgerBlue;color:white;\">%s</b>(<b style=\"color:Violet;\">%s</b>)</td></tr>\n",( bForwardFlag ? "&nbsp;&nbsp;&nbsp;&nbsp;" : "" ),Elementos.get(iIndex).getDataManipNumber(),Elementos.get(iIndex).getStepInDataManipNumber(),Elementos.get(iIndex).getLabel(),Elementos.get(iIndex).getObject(),Elementos.get(iIndex).getParameters());
               
                if(bForwardFlag)
                    writer.printf("</table></td></tr>\n");                
                
            }            
            if(_debug) System.out.println("================================================================================================");
            if(_debug) System.out.println(Elementos.get(iIndex).toString());
        }
        
        writer.println("</table></div>");
        writer.println("<script>");
        writer.println("var coll = document.getElementsByClassName(\"collapsible\"); var i;");
        writer.println("for (i = 0; i < coll.length; i++) {");
        writer.println("coll[i].addEventListener(\"click\", function() {");
        writer.println("this.classList.toggle(\"active\");");
        writer.println("var content = this.nextElementSibling;");
        writer.println("if (content.style.display === \"block\") {");
        writer.println("content.style.display = \"none\";");
        writer.println("} else {");
        writer.println("content.style.display = \"block\";}");
        writer.println("});}");
        writer.println("</script>");
        writer.println("</body>");
        writer.println("</html>");
        writer.close();    

        return;
    }
    
    private static String DiscoveryFEByDirectory( String Directory ) {
        String FE = "XX";
               
        String[] conj = FEFiltros.split(",");
        
        for (String conj1 : conj) {
            if (Directory.contains(conj1)) {
                return conj1;
            }
        }
        
        return FE;
    }
    
    private static String DiscoveryModuleByDirectory( String Directory ) {
        String Module = "XXX";
        
        String[] conj = ModFiltros.split(",");
        
        for (String conj1 : conj) {
            if (Directory.contains(conj1)) {
                return conj1;
            }
        }

        return Module;
    }    

    public static void ProcessDirectory(File directory, String sTargetDir) throws FileNotFoundException, UnsupportedEncodingException {
        if(directory.isDirectory()) {
            System.out.println("Examining directory " + directory.getPath());
            String[] subDirectory = directory.list();
            if(subDirectory != null) {
                for(String dir : subDirectory){
                    ProcessDirectory(new File(directory + File.separator  + dir), sTargetDir);
                }
            }
        }
        if(directory.isFile() && directory.getAbsoluteFile().toString().toUpperCase().contains(".XML")) {
             System.out.println("... Processing " + directory.getAbsoluteFile() + " ..." );
             
             String FrontEnd = DiscoveryFEByDirectory(directory.getAbsoluteFile().toString());
             String srcModule = DiscoveryModuleByDirectory(directory.getAbsoluteFile().toString());
             String TargetFile = sTargetDir + FrontEnd + "_" + srcModule + "_" + directory.getName().substring(0,directory.getName().length()-4) + ".html";

            ProcessXML(directory.getAbsoluteFile().toString(),TargetFile,FrontEnd,srcModule);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        String source_dir = ".";
        String target_dir = ".";
        
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i].charAt(0)) {
                    case '-': {
                        switch (Character.toUpperCase(args[i].charAt(1))) {
                            case 'D': {
                                database_file = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'S': {
                                source_dir = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'T': {
                                target_dir = args[i].substring(2, args[i].length());
                                break;
                            }
                            case 'V': {
                                _debug = true;
                                break;
                            }
                            case 'M': {
                                ModFiltros = args[i].substring(2, args[i].length());
                                break;
                            }   
                            case 'F': {
                                FEFiltros = args[i].substring(2, args[i].length());
                                break;
                            }
                            default: {
                                System.err.print("Unknown Parameter !!!\n");
                                Sintaxe();
                                System.exit(1);
                                break;
                            }
                        }
                    }
                    if (_debug) {
                        System.out.println("Parameters [" + i + "] " + args[i]);
                    }
                }
            }
        } else {
            Sintaxe();
            System.exit(1);
        }
                
        if (args.length == 0) {
            Sintaxe();            
            System.exit(1);
        }

        ProcessDirectory(new File(source_dir), target_dir);        
    }
    
    public static void Sintaxe() {
        System.err.print("Sintaxe : jXerces -S<Source> -T<Target> -F<Front Filter> -M<Module Filter> -D -v\n");
        System.err.print("  -S<Source>    - Root Directory of XML Files\n");
        System.err.print("  -T<Target>    - Destination of HTML Files\n");
        System.err.print("  -F<Front-End Filter>  - Front-Ends Filters => separate by ,\n");
        System.err.print("  -M<Modules Filter>  - Module Filters => separate by , \n");
        System.err.print("  -D<Database>        - Database to Store XML\n");
        System.err.print("  -V            - Verbose (debug info)\n");
    }
}
