package org.dreamwork.db;

/**
 * Created by seth.yang on 2019/3/6
 */
public class Parameter {
    public enum Operator {
        EQ ("="),                           // =
        LT ("<"),                           // <
        LE ("<="),                          // <=
        GT (">"),                           // >
        GE (">="),                          // >=
        IS_NULL (null),                     // is null
        IS_NOT_NULL (null),                 // is not null
        LIKE ("LIKE"),                      // like
        IN ("IN")                           // in
        ;

        public final String text;
        Operator (String text) {
            this.text = text;
        }
    }

    public final String   name;
    public final Operator operator;
    public final Object   value;

    public Parameter (String name, Operator operator, Object value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

/*
    @Override
    public String toString () {
        switch (operator) {
            case EQ:
                return name + " = ?";
            case LT:
                return name + " < ?";
            case LE:
                return name + " <= ?";
            case GT:
                return name + " > ?";
            case GE:
                return name + " >= ?";
            case IN:
        }
    }
*/
}
