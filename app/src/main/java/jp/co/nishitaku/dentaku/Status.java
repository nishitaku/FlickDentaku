package jp.co.nishitaku.dentaku;

/**
 * Created by takuro on 2017/06/12.
 */

public class Status {

    public enum PushedStatus {
        NUMBER(1),      // 数値ボタン押下
        OPERATOR(2),    // 演算子ボタン押下
        EQUAL(3),       // ＝ボタン押下
        INIT(4);        // 初期状態

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
            return INIT;
        }

        public int getId() {
            return id;
        }
    }
}
