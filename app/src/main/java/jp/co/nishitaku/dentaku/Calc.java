package jp.co.nishitaku.dentaku;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by takuro on 2017/09/11.
 */
public class Calc{

    /**
     * 逆ポーランド記法用の演算子優先度
     */
    @SuppressWarnings("serial")
    private static final Map<Character, Integer> rpnRank = new HashMap<Character, Integer>() {
        {
            // 数値が大きいほど、優先順位が高い
            put('(', 4);
            put('#', 3);
            put('×', 2);
            put('÷', 2);
            put('+', 1);
            put('-', 1);
            put(')', 0);
        }
    };

    /**
     * 数式をパースして計算する。
     * 逆ポーランド記法で計算する。使える演算子は四則演算と括弧。
     * スペースを含まない "(10+20)*30-(40+50)/2" のような式。空白文字は全て除去される。
     *
     * @param expression 数式の文字列
     * @return 計算値
     */
    public static final BigDecimal parseExpression(final String expression) {
        final Deque<Character> stack = new ArrayDeque<Character>(); // 演算子スタック
        final Deque<BigDecimal> val = new ArrayDeque<BigDecimal>();  // 数値スタック

        // 空白文字を除去
        String s = expression.replaceAll("¥¥s+", "");
        // 末尾に")"をつけることで、最後にスタックを吐き出させる
        s = "(" + s + ")";
        final int len = s.length();

        String tmp = "";    // 数字用バッファ
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if ('0' <= c && c <= '9') {
                // 数字だった場合、数字用バッファへ格納する
                tmp += c;
            } else {
                // 演算子だった場合、数字用バッファを数値スタックへ積む
                if (!tmp.equals("")) {
                    val.push(new BigDecimal(tmp));
                    tmp = "";
                }

                // 演算子スタックの先頭の優先度が低くなるまで演算子スタックの内容を取り出す
                // 数値スタックの先頭と次の要素を取り出した演算子で計算し、数値スタックに戻す
                while (!stack.isEmpty()
                        && rpnRank.get(stack.peek()) >= rpnRank.get(c)
                        && stack.peek() != '(') {
                    String opeStr = String.valueOf(stack.pop());
                    BigDecimal value2 = val.pop();
                    BigDecimal value1 = val.pop();
                    try {
                        if ("+".equals(opeStr)) {
                            val.push(value1.add(value2).stripTrailingZeros());
                        } else if ("-".equals(opeStr)) {
                            val.push(value1.subtract(value2).stripTrailingZeros());
                        } else if ("×".equals(opeStr)) {
                            val.push(value1.multiply(value2).stripTrailingZeros());
                        } else if ("÷".equals(opeStr)) {
                            val.push(value1.divide(value2, 12, BigDecimal.ROUND_HALF_UP).stripTrailingZeros());
                        }
                    } catch (ArithmeticException exception) {
                        return null;
                    }
                }

                if (c == ')') {
                    stack.pop();
                } else {
                    stack.push(c);
                }
            }
        }

        return val.pop();
    }
}
