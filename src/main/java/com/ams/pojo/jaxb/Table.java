package com.ams.pojo.jaxb;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "table")
@XmlType(name = "table", propOrder = {"name", "fname", "chname", "fields"})
public class Table {
    @XmlElement
    private String name;
    @XmlElement
    private String fname;
    @XmlElement
    private String chname;
    @XmlElementWrapper(name = "fields")
    @XmlElements(value = @XmlElement(name = "field"))
    private List<Field> fields;

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getFname() {
        return fname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getChname() {
        return chname;
    }

    public void setChname(String chname) {
        this.chname = chname;
    }
}
