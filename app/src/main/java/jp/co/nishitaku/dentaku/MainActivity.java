package jp.co.nishitaku.dentaku;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import jp.co.nishitaku.dentaku.Status.PushedStatus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView textViewCalc = null;
    private TextView textViewResult = null;

    PushedStatus pushedStatus = PushedStatus.UNKNOWN;

    StringBuilder inputStr = new StringBuilder();    // 入力中文字列
    double result = 0;                                  // 計算結果
    int recentOperator = R.id.btn_equal;

    /**
     * 数値キーを押したときの動作
     */
    OnClickListener numberKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Button button = (Button) view;
            // 式に追加
            textViewCalc.append(button.getText());
            // 入力中文字列に追加
            inputStr.append(button.getText());
            // 状態更新
            pushedStatus = PushedStatus.NUMBER;
            Log.d(TAG, "numberKeyClick: inputStr=" + inputStr);
        }
    };

    /**
     * 演算キー(+, -, *, /)を押したときの動作
     * 2回目以降押された場合は、前回押された演算を行い、結果を表示する
     */
    OnClickListener operatorKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Button operatorButton = (Button) view;
            Log.d(TAG, "operatorKeyClick: button=" + operatorButton.getText());

            switch (pushedStatus) {
                case NUMBER:
                    // 式に追加
                    textViewCalc.append(" " + operatorButton.getText() + " ");
                    // 入力中文字列を数値に変換
                    double value = Double.parseDouble(inputStr.toString());

                    if (recentOperator == R.id.btn_equal) {
                        // 計算結果がない場合(初回)は、そのまま格納
                        result = value;
                    } else {
                        // ある場合は計算して、結果を表示
                        result = calc(recentOperator, result, value);
                        textViewResult.setText(String.valueOf(result));

                    }
                    Log.d(TAG, "operatorKeyClick: result=" + result);
                    break;

                case OPERATOR:
                    // 直前の演算子を書き換える
                    SpannableStringBuilder sb = new SpannableStringBuilder(textViewCalc.getText());
                    if(sb.length() >= 3) {
                        sb.delete(sb.length() - 3, sb.length());
                        sb.append(" " + operatorButton.getText() + " ");
                        textViewCalc.setText(sb.toString());
                        sb.clear();
                        sb = null;
                    }
                    break;

                case EQUAL:
                    // 何もしない
                    break;

                default:
                    break;
            }
            // 押下した演算キーを格納
            recentOperator = operatorButton.getId();
            // 入力中文字列をリセット
            inputStr.setLength(0);
            // 状態更新
            pushedStatus = PushedStatus.OPERATOR;
        }
    };

    /**
     * イコールを押したときの動作
     * 計算結果を表示する
     * →式を解析して再計算した結果を表示させたい
     * 例えば式が 1 + 2 * 3 の場合、計算結果は9だが、7を表示させたい
     */
    OnClickListener equalKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Button button = (Button) view;
            Log.d(TAG, "operatorKeyClick: button=" + button.getText());

            switch (pushedStatus) {
                case NUMBER:
                    // 入力中文字列を数値に変換
                    double value = Double.parseDouble(inputStr.toString());
                    // 計算
                    result = calc(recentOperator, result, value);
                    Log.d(TAG, "operatorKeyClick: result=" + result);
                    // 結果を表示
                    textViewResult.setText(String.valueOf(result));
                    // 初期化
                    textViewCalc.setText("");
                    inputStr.setLength(0);
                    // 状態更新
                    pushedStatus = PushedStatus.EQUAL;
                    break;

                case OPERATOR:
                case EQUAL:
                    // 何もしない
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * クリアを押したときの動作
     */
    OnClickListener clearKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            recentOperator = R.id.btn_equal;
            result = 0;
            textViewCalc.setText("");
            textViewResult.setText("");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCalc = (TextView) findViewById(R.id.text_calc);
        textViewResult = (TextView) findViewById(R.id.text_result);

        setListener();

    }

    /**
     * 計算する
     * @param operator
     * @param value1
     * @param value2
     * @return
     */
    private double calc(int operator, double value1, double value2) {
        switch (operator) {
            case R.id.btn_tasu:
                return value1 + value2;
            case R.id.btn_hiku:
                return value1 - value2;
            case R.id.btn_kakeru:
                return value1 * value2;
            case R.id.btn_waru:
                return value1 / value2;
            default:
                return value1;
        }
    }


    private void setListener() {
        findViewById(R.id.btn_0).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_1).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_2).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_3).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_4).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_5).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_6).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_7).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_8).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_9).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_dot).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_equal).setOnClickListener(equalKeyClickListener);
        findViewById(R.id.btn_tasu).setOnClickListener(operatorKeyClickListener);
        findViewById(R.id.btn_hiku).setOnClickListener(operatorKeyClickListener);
        findViewById(R.id.btn_waru).setOnClickListener(operatorKeyClickListener);
        findViewById(R.id.btn_kakeru).setOnClickListener(operatorKeyClickListener);
        findViewById(R.id.btn_del).setOnClickListener(clearKeyClickListener);
        findViewById(R.id.btn_clear).setOnClickListener(clearKeyClickListener);
    }
}
