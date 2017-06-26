package jp.co.nishitaku.dentaku;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import jp.co.nishitaku.dentaku.Status.PushedStatus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SWIPE_MIN_DISTANCE_X = 50;           // X軸最低スワイプ距離
    private static final int SWIPE_THRESHOLD_VELOCITY_X = 200;    // X軸最低スワイプスピード
    private static final int SWIPE_MAX_OFF_PATH_Y = 250;          // Y軸の移動距離(これ以上なら横移動を判定しない)
    private static final int SWIPE_MIN_DISTANCE_Y = 50;           // Y軸最低スワイプ距離
    private static final int SWIPE_THRESHOLD_VELOCITY_Y = 200;    // Y軸最低スワイプスピード
    private static final int SWIPE_MAX_OFF_PATH_X = 250;          // X軸の移動距離(これ以上なら縦移動を判定しない)

    private TextView textViewCalc = null;
    private TextView textViewResult = null;

    private GestureDetector mGestureDetector;

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

    OnTouchListener fiveKeyTouchListner = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            Button button = (Button) view;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 押したときの動作
                    findViewById(R.id.btn_kakeru).setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_tasu).setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_hiku).setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_waru).setVisibility(View.VISIBLE);
                    Log.d(TAG, "onTouch: ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_UP:
                    // 離したときの動作
                    findViewById(R.id.btn_kakeru).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btn_tasu).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btn_hiku).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btn_waru).setVisibility(View.INVISIBLE);

                    // 式に追加
                    textViewCalc.append(button.getText());
                    // 入力中文字列に追加
                    inputStr.append(button.getText());
                    // 状態更新
                    pushedStatus = PushedStatus.NUMBER;
                    Log.d(TAG, "onTouch: ACTION_UP");

                    break;
            }
            return mGestureDetector.onTouchEvent(event);
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

    SimpleOnGestureListener flickListener = new SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            try {

                // 移動距離・スピードを出力
                float distance_x = Math.abs((event1.getX() - event2.getX()));
                float velocity_x = Math.abs(velocityX);
                float distance_y = Math.abs((event1.getY() - event2.getY()));
                float velocity_y = Math.abs(velocityY);
                Log.d(TAG, "onFling: 横の移動距離=" + distance_x + " 横の移動スピード=" + velocity_x);
                Log.d(TAG, "onFling: 縦の移動距離=" + distance_y + " 縦の移動スピード=" + velocity_y);

                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH_Y) {
                    // Y軸の移動距離が大きすぎる場合
                    Log.d(TAG, "onFling: 縦の移動距離が大きすぎ");
                } else if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE_X && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY_X) {
                    // 開始位置から終了位置の移動距離が指定値より大きい
                    // X軸の移動速度が指定値より大きい
                    Log.d(TAG, "onFling: 右から左");
                } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE_X && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY_X) {
                    // 終了位置から開始位置の移動距離が指定値より大きい
                    // X軸の移動速度が指定値より大きい
                    Log.d(TAG, "onFling: 左から右");
                } else if (Math.abs(event1.getX() - event2.getX()) > SWIPE_MAX_OFF_PATH_X) {
                    // X軸の移動距離が大きすぎる場合
                    Log.d(TAG, "onFling: 横の移動距離が大きすぎ");
                } else if (event2.getY() - event1.getY() > SWIPE_MIN_DISTANCE_Y && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY_Y) {
                    // 開始位置から終了位置の移動距離が指定値より大きい
                    // Y軸の移動速度が指定値より大きい
                    Log.d(TAG, "onFling: 上から下");
                } else if (event1.getY() - event2.getY() > SWIPE_MIN_DISTANCE_Y && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY_Y) {
                    // 終了位置から開始位置の移動距離が指定値より大きい
                    // Y軸の移動速度が指定値より大きい
                    Log.d(TAG, "onFling: 下から上");
                }
            } catch (Exception e) {
                // 何もしない
            }
            return super.onFling(event1, event2, velocityX, velocityY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCalc = (TextView) findViewById(R.id.text_calc);
        textViewResult = (TextView) findViewById(R.id.text_result);

        mGestureDetector = new GestureDetector(getApplicationContext(), flickListener);
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
        findViewById(R.id.btn_5).setOnTouchListener(fiveKeyTouchListner);
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
        findViewById(R.id.btn_clear).setOnClickListener(clearKeyClickListener);
    }
}
