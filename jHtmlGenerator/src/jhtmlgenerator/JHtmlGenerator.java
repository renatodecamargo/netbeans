/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jhtmlgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rcamargo
 */
public class JHtmlGenerator {
    private static String copyright = "JHtmlGenerator v1.0 - 20/04/2018 - Copyright rcamargo";
    protected static String database_file = "";
    protected static String target_dir = "";
    protected static   boolean _debug = false;
    private static List<XMLElement> Elementos = new ArrayList<>();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, UnsupportedEncodingException {
        // TODO code application logic here
        System.out.println(copyright);
      
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i].charAt(0)) {
                    case '-': {
                        switch (Character.toUpperCase(args[i].charAt(1))) {
                            case 'D': {
                                database_file = args[i].substring(2, args[i].length());
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
                            default: {
                                System.err.print("Unknown Parameter !!!\n");
                                Sintaxe();
                                System.exit(1);
                                break;
                            }
                        }
                    }
                    if (_debug) {
                        System.err.println("Parameters [" + i + "] " + args[i]);
                    }
                }
            }
        } else {
            Sintaxe();
            System.exit(1);
        }

        if (args.length == 0 || database_file.equals("") || target_dir.equals("")) 
        {
            Sintaxe();            
            System.exit(1);
        }
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        
        RunHtmlGenerator();
    }
    
    public static void Sintaxe() {
        System.err.print("Sintaxe : jHtmlGenerator -T<TargetDir> -D<database> -v\n");
        System.err.print("  -T<TargetDir>         - Where HTML will be generated\n");
        System.err.print("  -D<Database>          - Database where XML Elements are stored\n");
        System.err.print("  -V                    - Verbose (debug info)\n");
    }        
        
    public static void RunHtmlGenerator() throws FileNotFoundException, UnsupportedEncodingException 
    {
        PrintWriter writer = null;
        Connection connection = null;
        
        try
        {
                // create a database connection
                connection = DriverManager.getConnection("jdbc:sqlite:" + database_file );
                Statement statement = connection.createStatement();
                statement.setQueryTimeout(30);  // set timeout to 30 sec.
                ResultSet source = statement.executeQuery("select distinct xmlsourcefile from xmlelements order by xmlsourcefile");
                while(source.next())
                {
                    Statement statementElement = connection.createStatement();
                    ResultSet elements = statementElement.executeQuery("select * from xmlelements where xmlsourcefile = '" + source.getString("xmlsourcefile") + "' order by xmlsourcefile,xmlfrontend,xmlmodule,targetfile,datamanipnumber,stepindatamanipnumber");
                    System.out.println("...Processing " + source.getString("xmlsourcefile"));
                    Elementos.clear();
                    while(elements.next())
                    {
                            XMLElement SingleElement = new XMLElement();
                            SingleElement.setDataManipNumber(elements.getInt("datamanipnumber"));
                            SingleElement.setStepInDataManipNumber(elements.getInt("stepindatamanipnumber"));
                            SingleElement.setContainer(elements.getString("container"));
                            SingleElement.setLabel(elements.getString("label"));
                            SingleElement.setObject(elements.getString("object"));
                            SingleElement.setParameters(elements.getString("parameters"));
                            SingleElement.setObjectType(elements.getString("objecttype"));
                            SingleElement.setBlock((elements.getInt("block") == 1));
                            SingleElement.setLink(elements.getString("link"));
                            SingleElement.setBlockFilter(elements.getString("blockfilter"));
                            SingleElement.setObjectFilter(elements.getString("objectfilter"));
                            SingleElement.setXmlSourceFile(elements.getString("xmlsourcefile"));
                            SingleElement.setXmlModule(elements.getString("xmlmodule"));
                            SingleElement.setXmlFrontEnd(elements.getString("xmlfrontend"));
                            SingleElement.setTargetFile(elements.getString("targetfile"));
                            SingleElement.setFilterType(elements.getString("filtertype"));
                            Elementos.add(SingleElement);                            
                    }
                    
                    System.out.println("... Writing in "  + target_dir + Elementos.get(0).getTargetFile());
                    writer = new PrintWriter(target_dir +  Elementos.get(0).getTargetFile(), "UTF-8");

                    writer.println("<!DOCTYPE html>");
                    writer.println("<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
                    writer.println("<h2>:. " + Elementos.get(0).getXmlSourceFile()+ "</h2><hr>");
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
                }                
        }
        catch(SQLException e)
        {
                // if the error message is "out of memory", 
                // it probably means no database file is found
                System.err.println(e.getMessage());
        }
    }
}