package edu.citchennai.hostel.complaint;

public enum ComplaintCategory {
    LIGHT("Light"),
    FAN("Fan"),
    DOOR("Door"),
    CUPBOARD("Cupboard"),
    FLUSH_WATER("Flush / water"),
    OTHER("Other");

    private final String label;

    ComplaintCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
