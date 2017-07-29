package jp.co.nishitaku.dentaku;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

import jp.co.nishitaku.dentaku.Status.PushedStatus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView textViewCalc = null;
    private TextView textViewResult = null;

    private Button btn5 = null;
    private Button btnKakeru = null;
    private Button btnTasu = null;
    private Button btnHiku = null;
    private Button btnWaru = null;
    private Button[] operatorBtnList = null;

    PushedStatus pushedStatus = PushedStatus.UNKNOWN;

    StringBuilder inputStr = new StringBuilder();    // 入力中文字列
    BigDecimal result = BigDecimal.ZERO;             // 計算結果
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
            int x = (int)event.getRawX();
            int y = (int)event.getRawY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 押したときの動作
                    Log.d(TAG, "onTouch: ACTION_DOWN");
                    // 演算子ボタンを表示
                    setOperatorBtnVisibility(View.VISIBLE);
                    break;

                case MotionEvent.ACTION_MOVE:
                    // フリック中の動作
                    if (inViewBounds(btnKakeru, x, y)) {
                        // 「×」ボタンの色を変更
                        setOperatorBtnBackground(0);
                    } else if (inViewBounds(btnTasu, x, y)) {
                        // 「＋」ボタンの色を変更
                        setOperatorBtnBackground(1);
                    } else if (inViewBounds(btnHiku, x, y)) {
                        // 「−」ボタンの色を変更
                        setOperatorBtnBackground(2);
                    } else if (inViewBounds(btnWaru, x, y)) {
                        // 「÷」ボタンの色を変更
                        setOperatorBtnBackground(3);
                    } else {
                        // それ以外
                        setOperatorBtnBackground(4);
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    // 離したときの動作
                    Log.d(TAG, "onTouch: ACTION_UP");
                    // 演算子ボタンを非表示
                    setOperatorBtnVisibility(View.INVISIBLE);

                    if (inViewBounds(btnKakeru, x, y)) {
                        // 「×」ボタンの処理
                        operatorKeyAction(btnKakeru);
                    } else if (inViewBounds(btnTasu, x, y)) {
                        // 「＋」ボタンの処理
                        operatorKeyAction(btnTasu);
                    } else if (inViewBounds(btnHiku, x, y)) {
                        // 「−」ボタンの処理
                        operatorKeyAction(btnHiku);
                    } else if (inViewBounds(btnWaru, x, y)) {
                        // 「÷」ボタンの処理
                        operatorKeyAction(btnWaru);
                    }

                    setOperatorBtnBackground(4);
                    break;
            }
            return false;
        }
    };

    private void operatorKeyAction(Button operatorButton) {
        Log.d(TAG, "operatorKeyClick: button=" + operatorButton.getText());

        switch (pushedStatus) {
            case NUMBER:
                // 式に追加
                textViewCalc.append(" " + operatorButton.getText() + " ");
                // 入力中文字列を数値に変換
                BigDecimal value = new BigDecimal(inputStr.toString());

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

    /**
     * イコールを押したときの動作
     * 計算結果を表示する
     */
    OnClickListener equalKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Button button = (Button) view;
            Log.d(TAG, "operatorKeyClick: button=" + button.getText());

            switch (pushedStatus) {
                case NUMBER:
                    // 入力中文字列を数値に変換
                    BigDecimal value = new BigDecimal(inputStr.toString());
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
            result = BigDecimal.ZERO;
            textViewCalc.setText("");
            textViewResult.setText("");
            inputStr.setLength(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCalc = (TextView) findViewById(R.id.text_calc);
        textViewResult = (TextView) findViewById(R.id.text_result);

        btn5 = (Button) findViewById(R.id.btn_5);
        btnKakeru = (Button) findViewById(R.id.btn_kakeru);
        btnTasu = (Button) findViewById(R.id.btn_tasu);
        btnHiku = (Button) findViewById(R.id.btn_hiku);
        btnWaru = (Button) findViewById(R.id.btn_waru);
        operatorBtnList = new Button[]{btnKakeru, btnTasu, btnHiku, btnWaru};

        setListener();

    }

    private void setOperatorBtnVisibility(int visibility) {
        btnKakeru.setVisibility(visibility);
        btnTasu.setVisibility(visibility);
        btnHiku.setVisibility(visibility);
        btnWaru.setVisibility(visibility);
    }

    private void setOperatorBtnBackground(int index) {
        for(int i = 0; i < 4; i++) {
            if(index == i) {
                operatorBtnList[i].setBackgroundColor(Color.YELLOW);
            } else {
                operatorBtnList[i].setBackgroundColor(Color.argb(127, 0, 0, 255));
            }
        }
    }

    /**
     * 計算する
     * @param operator
     * @param value1
     * @param value2
     * @return
     */
    private BigDecimal calc(int operator, BigDecimal value1, BigDecimal value2) {
        switch (operator) {
            case R.id.btn_tasu:
                return value1.add(value2);
            case R.id.btn_hiku:
                return value1.subtract(value2);
            case R.id.btn_kakeru:
                return value1.multiply(value2);
            case R.id.btn_waru:
                return value1.divide(value2, 12, BigDecimal.ROUND_HALF_UP);
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
        btn5.setOnClickListener(numberKeyClickListener);
        btn5.setOnTouchListener(fiveKeyTouchListner);
        findViewById(R.id.btn_6).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_7).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_8).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_9).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_dot).setOnClickListener(numberKeyClickListener);
        findViewById(R.id.btn_equal).setOnClickListener(equalKeyClickListener);
        findViewById(R.id.btn_clear).setOnClickListener(clearKeyClickListener);
    }


    /**
     * 入力x, yがビュー領域内かどうか判定する
     */
    private boolean inViewBounds(final View view, int x, int y) {
        Rect outRect = new Rect();
        view.getDrawingRect(outRect);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }
}
