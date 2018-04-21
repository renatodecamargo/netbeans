/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jxerces;

/**
 *
 * @author renat
 */
public class XMLElement {
    protected  String XmlSourceFile;   // Nome do Arquivo XML origem
    protected  String XmlFrontEnd;     // Nome do Front-End
    protected  String XmlModule;       // Nome do Modulo
    protected  String TargetFile;   // Nome do Arquivo HTML destino
    protected  int DataManipNumber;    // Numero do DataManip
    protected  int StepInDataManipNumber;  // Numero do Step
    protected  String Label;            // Label do Objeto 
    protected  String Object;           // Nome do Objeto
    protected  String ObjectType;       // Tipo do Objeto
    protected  boolean Block;           // Objeto faz parte de Bloco
    protected  String FilterType;       // Tipo do Filtro
    protected  String ObjectFilter;    // Filtro do Objeto
    protected  String BlockFilter;     // Filtro do Bloco
    protected  String Parameters;      // Parameters
    protected  String Container;       // Nome do Bloco 
    protected  String Link;            // Link do Objeto

    public String getLink() {
        return Link;
    }

    public void setLink(String Link) {
        this.Link = Link;
    }
    
    public XMLElement XMLElement()
    {
        XmlSourceFile = "";   // Nome do Arquivo XML origem
        XmlFrontEnd = "";     // Nome do Front-End
        XmlModule = "";       // Nome do Modulo
        TargetFile = "";   // Nome do Arquivo HTML destino
        DataManipNumber = 0;    // Numero do DataManip
        StepInDataManipNumber = 0;  // Numero do Step
        Label= "";            // Label do Objeto 
        Object = "";           // Nome do Objeto
        ObjectType = "";       // Tipo do Objeto
        Block = false;           // Objeto faz parte de Bloco
        ObjectFilter = "";    // Filtro do Objeto
        BlockFilter = "";     // Filtro do Bloco
        Parameters = "";      // Parameters
        Container = "";       // Nome do Bloco
        Link = "";
        FilterType = "";
        return this;
    }
    
    public String getXmlSourceFile() {
        return XmlSourceFile;
    }

    public  void setXmlSourceFile(String XmlSourceFile) {
        this.XmlSourceFile = XmlSourceFile;
    }

    public  String getXmlFrontEnd() {
        return XmlFrontEnd;
    }

    public  void setXmlFrontEnd(String XmlFrontEnd) {
        this.XmlFrontEnd = XmlFrontEnd;
    }

    public  String getXmlModule() {
        return XmlModule;
    }

    public  void setXmlModule(String XmlModule) {
        this.XmlModule = XmlModule;
    }

    public  String getTargetFile() {
        return TargetFile;
    }

    public  void setTargetFile(String TargetFile) {
        this.TargetFile = TargetFile;
    }

    public  int getDataManipNumber() {
        return DataManipNumber;
    }

    public  void setDataManipNumber(int DataManipNumber) {
        this.DataManipNumber = DataManipNumber;
    }

    public  int getStepInDataManipNumber() {
        return StepInDataManipNumber;
    }

    public void setStepInDataManipNumber(int StepInDataManipNumber) {
        this.StepInDataManipNumber = StepInDataManipNumber;
    }

    public  String getLabel() {
        return Label;
    }

    public void setLabel(String Label) {
        this.Label = Label;
    }

    public String getObject() {
        return Object;
    }

    public void setObject(String Object) {
        this.Object = Object;
    }

    public  String getObjectType() {
        return ObjectType;
    }

    public  void setObjectType(String ObjectType) {
        this.ObjectType = ObjectType;
    }

    public  boolean isBlock() {
        return Block;
    }

    public  void setBlock(boolean Block) {
        this.Block = Block;
    }

    public  String getObjectFilter() {
        return ObjectFilter;
    }

    public  void setObjectFilter(String ObjectFilter) {
        this.ObjectFilter = ObjectFilter;
    }

    public  String getBlockFilter() {
        return BlockFilter;
    }

    public  void setBlockFilter(String BlockFilter) {
        this.BlockFilter = BlockFilter;
    }

    public  String getParameters() {
        return Parameters;
    }

    public  void setParameters(String Parameters) {
        this.Parameters = Parameters;
    }

    public  String getContainer() {
        return Container;
    }

    public  void setContainer(String Container) {
        this.Container = Container;
    }
    

    public String getFilterType() {
        return FilterType;
    }

    public void setFilterType(String FilterType) {
        this.FilterType = FilterType;
    }    

    public String toString()
    {
        return "XmlSourceFile" + "[" + XmlSourceFile + "]\n" +
            "XmlFrontEnd" + "[" + XmlFrontEnd + "]\n" +
            "XmlModule" + "[" + XmlModule + "]\n" +
            "TargetFile" + "[" + TargetFile + "]\n" +
            "DataManipNumber" + "[" + DataManipNumber + "]\n" +
            "StepInDataManipNumber" + "[" + StepInDataManipNumber + "]\n" +
            "Label"  + "[" + Label  + "]\n" +
            "Object" + "[" + Object + "]\n" +
            "ObjectType" + "[" + ObjectType + "]\n" +
            "Block" + "[" + Block + "]\n" +
            "FilterType" + "[" + FilterType + "]\n" +
            "ObjectFilter" + "[" + ObjectFilter + "]\n" +
            "BlockFilter" + "[" + BlockFilter + "]\n" +
            "Parameters" + "[" + Parameters + "]\n" +
            "Container" + "[" + Container + "]\n" +
            "Link" + "[" + Link + "]\n";
    }
}
