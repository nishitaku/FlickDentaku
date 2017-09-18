package jp.co.nishitaku.dentaku;

import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

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

    BigDecimal result = BigDecimal.ZERO;             // 計算結果

    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private GestureDetector mGestureDetector;

    /**
     * 数値ボタンのクリックリスナー
     */
    OnClickListener numberKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // 計算エラーが発生している場合は無視する
            if (null == result) {
                return;
            }
            Button button = (Button) view;
            // 式に追加
            textViewCalc.append(button.getText());
        }
    };

    /**
     *  "5"ボタンのクリックリスナー
     */
    OnTouchListener fiveKeyTouchListner = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
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

    /**
     * 演算子ボタンの処理
     * @param opeBtn
     */
    private void operatorKeyAction(Button opeBtn) {
        Log.d(TAG, opeBtn.getText() + " pushed.");
        // 計算エラーが発生している場合は無視する
        if (null == result) {
            return;
        }
        // 式に追加
//        textViewCalc.append(" " + opeBtn.getText() + " ");
        textViewCalc.append(opeBtn.getText());
    }

    /**
     * "="ボタンのクリックリスナー
     * 計算結果を表示する
     */
    OnClickListener equalKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Button button = (Button) view;
            Log.d(TAG, "Calc execute : " + textViewCalc.getText().toString());
            // 計算
            result = Calc.parseExpression(textViewCalc.getText().toString());

            if (null == result) {
                // 計算エラーの場合
                Log.d(TAG, "equalKeyClickListener: calc error.");
                textViewResult.setText("E");
            } else if (result.equals(new BigDecimal("0E-12"))) {
                // 0を除算した場合
                textViewResult.setText("0");
            } else {
                textViewResult.setText(result.toPlainString());
            }

            // 初期化
            textViewCalc.setText("");
        }
    };

    /**
     * "CLEAR"ボタンのクリックリスナー
     */
    OnClickListener clearKeyClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            result = BigDecimal.ZERO;
            textViewCalc.setText("");
            textViewResult.setText("");
        }
    };

    /**
     *
     */
    OnTouchListener deleteFlingListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "onTouch: deleteFlickListener");
            return mGestureDetector.onTouchEvent(event);
        }
    };

    private final GestureDetector.SimpleOnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        // フリックイベント
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {

            try {

                // 移動距離・スピードを出力
                float distance_x = Math.abs((event1.getX() - event2.getX()));
                float velocity_x = Math.abs(velocityX);
                Log.d(TAG, "onFling: 横の移動距離:" + distance_x + " 横の移動スピード:" + velocity_x);

                // Y軸の移動距離が大きすぎる場合
                if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
                    Log.d(TAG, "onFling: 縦の移動距離が大きすぎ");
                }
                // 開始位置から終了位置の移動距離が指定値より大きい
                // X軸の移動速度が指定値より大きい
                else if  (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.d(TAG, "onFling: 右から左");

                }
                // 終了位置から開始位置の移動距離が指定値より大きい
                // X軸の移動速度が指定値より大きい
                else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.d(TAG, "onFling: 左から右");
                }

            } catch (Exception e) {
                // TODO
            }

            return false;
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

        mGestureDetector = new GestureDetector(getApplicationContext(), mOnGestureListener);

        setListener();

    }

    private void setOperatorBtnVisibility(int visibility) {
        btnKakeru.setVisibility(visibility);
        btnTasu.setVisibility(visibility);
        btnHiku.setVisibility(visibility);
        btnWaru.setVisibility(visibility);
    }

    private void setOperatorBtnBackground(int index) {
        for (int i = 0; i < 4; i++) {
            GradientDrawable drawable = new GradientDrawable();
            // 角を丸くする
            drawable.setCornerRadius(20);
            drawable.mutate();
            if (index == i) {
                drawable.setColor(ContextCompat.getColor(this, R.color.flickkeyNormal));
            } else {
                drawable.setColor(ContextCompat.getColor(this, R.color.flickkeyPressed));
            }
            operatorBtnList[i].setBackground(drawable);
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
        textViewResult.setOnTouchListener(deleteFlingListener);
        textViewCalc.setOnTouchListener(deleteFlingListener);
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
