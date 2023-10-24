package ch.hearc.ig.guideresto.business;

public class EvaluationCriteria {

    private Integer id;
    private String name;
    private String description;

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}