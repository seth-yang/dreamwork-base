package org.dreamwork.db;

/**
 * Created by seth.yang on 2019/3/6
 */
@SuppressWarnings ("unused")
public record Parameter(String name, org.dreamwork.db.Parameter.Operator operator, Object value) {
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
}
