package jp.co.nishitaku.dentaku;

/**
 * Created by takuro on 2017/06/12.
 */

public class Status {

    public enum PushedStatus {
        NUMBER(1),
        OPERATOR(2),
        EQUAL(3),
        UNKNOWN(4);

        private final int id;

        private PushedStatus(final int id) {
            this.id = id;
        }

        public static PushedStatus valueOf(int id) {
            for(PushedStatus num : values()) {
                if(num.getId() == id) {
                    return num;
                }
            }
            return UNKNOWN;
        }

        public int getId() {
            return id;
        }
    }
}
