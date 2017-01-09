package com.example.laurabravopriegue.petiquiz;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Multiplayer extends Fragment {
    FragmentActivity faActivity;
    LinearLayout llLayout;

    private static final String TAG = "Multiplayer";
    private static final String KEY_INDEX = "index";

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mSkipButton;
    private Button mCheatButton;

    private TextView mQuestionTextView;
    private TextView player1Score;
    private TextView player2Score;
    private TextView currentPlayerText;

    private int mCurrentIndex = 0;

    private void updateQuestion() {
        Question currentQuestion = Questions.mQuestionBank.get(mCurrentIndex);
        String question = currentQuestion.getTextResId();
        mQuestionTextView.setText(question);

        mTrueButton = (Button) llLayout.findViewById(R.id.true_button);
        mFalseButton = (Button) llLayout.findViewById(R.id.false_button);
        mSkipButton = (Button) llLayout.findViewById(R.id.skip_button);
        mCheatButton = (Button) llLayout.findViewById(R.id.cheat_button);

        if (currentQuestion.mUserAnswer != null) {
            //DisableButtons();
            Toast.makeText(super.getActivity(), "You've already answered this question!", Toast.LENGTH_SHORT)
                    .show();
        }
        else {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
            mSkipButton.setEnabled(true);
            mCheatButton.setEnabled(true);
        }
    }

    private void checkAnswer(boolean userPressed) {
        Question currentQuestion = Questions.mQuestionBank.get(mCurrentIndex);
        if (currentQuestion.mUserAnswer != null) {
            Toast.makeText(super.getActivity(), "You've already answered this question!", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        boolean answerIsTrue = currentQuestion.isAnswerTrue();
        int messageResId = 0;

        if (userPressed == answerIsTrue) {
            messageResId = R.string.correct_toast;

            SharedPreferences pref = super.getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            int currentPlayer = pref.getInt("currentPlayer", 0);
            SharedPreferences.Editor editor = pref.edit();
            Integer score = 0;
            switch(currentPlayer) {
                case 1:
                    score = pref.getInt("player1Score", 0);
                    score += 10;
                    editor.putInt("player1Score", score); // Storing integer
                    editor.commit(); // commit changes
                    //update score on screen
                    player1Score.setText(score.toString());
                    break;
                case 2:
                    score = pref.getInt("player2Score", 0);
                    score += 10;
                    editor.putInt("player2Score", score); // Storing integer
                    editor.commit(); // commit changes
                    //update score on screen
                    player2Score.setText(score.toString());
                    break;
            }
        } else {
            messageResId = R.string.incorrect_toast;
        }
        if (userPressed) {
            currentQuestion.mUserAnswer = 1;
        }
        else {
            currentQuestion.mUserAnswer = 0;
        }
        Toast.makeText(super.getActivity(), messageResId, Toast.LENGTH_SHORT)
                .show();
        NextPlayer();
        UpdateText();
        NextQuestion();
    }

    private void UpdateText() {
        SharedPreferences pref = super.getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        int currentPlayer = pref.getInt("currentPlayer", 0);
        int player1ScoreInt = pref.getInt("player1Score", 0);
        int player2ScoreInt = pref.getInt("player2Score", 0);
        currentPlayerText.setText("Player " + currentPlayer + " answers the question!");
        player1Score.setText(Integer.toString(player1ScoreInt));
        player2Score.setText(Integer.toString(player2ScoreInt));
    }

    private void NextPlayer() {
        SharedPreferences pref = super.getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        int currentPlayer = pref.getInt("currentPlayer", 0);
        SharedPreferences.Editor editor = pref.edit();
        switch(currentPlayer) {
            case 1:
                editor.putInt("currentPlayer",2);
                editor.commit(); // commit changes
                break;
            case 2:
                editor.putInt("currentPlayer",1);
                editor.commit(); // commit changes
                break;
        }
    }

    private void skipAnswer() {
        Question currentQuestion = Questions.mQuestionBank.get(mCurrentIndex);
        if (currentQuestion.mUserAnswer != null) {
            Toast.makeText(super.getActivity(), "You've already answered this question!", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        currentQuestion.mUserAnswer = 2;
        Toast.makeText(super.getActivity(), "Skipped question", Toast.LENGTH_SHORT)
                .show();
        NextPlayer();
        UpdateText();
        NextQuestion();
    }

    private void cheatAnswer() {
        Question currentQuestion = Questions.mQuestionBank.get(mCurrentIndex);
        if (currentQuestion.mUserAnswer != null) {
            Toast.makeText(super.getActivity(), "You've already answered this question!", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        currentQuestion.mUserAnswer = 3;
        Toast.makeText(super.getActivity(), currentQuestion.isAnswerTrue()+": "+currentQuestion.getExplanation(), Toast.LENGTH_SHORT)
                .show();
        NextPlayer();
        UpdateText();
        NextQuestion();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mCurrentIndex = arguments.getInt("question");
        }

        faActivity  = super.getActivity();
        llLayout    = (LinearLayout) inflater.inflate(R.layout.multiplayer, container, false);

        mQuestionTextView = (TextView) llLayout.findViewById(R.id.question_text_view);
        player1Score = (TextView) llLayout.findViewById(R.id.player1Score);
        player2Score = (TextView) llLayout.findViewById(R.id.player2Score);
        currentPlayerText = (TextView) llLayout.findViewById(R.id.playerTurnText);

        SharedPreferences pref = super.getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("currentPlayer", 1);
        editor.putInt("player1Score", 0);
        editor.putInt("player2Score", 0);
        editor.commit();
        UpdateText();


        mTrueButton = (Button) llLayout.findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);
            }
        });

        mFalseButton = (Button) llLayout.findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }
        });

        mSkipButton = (Button) llLayout.findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipAnswer();
            }
        });

        mCheatButton = (Button) llLayout.findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cheatAnswer();
            }
        });

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
        }

        DownloadQuestions downloadQuestions = new DownloadQuestions();
        downloadQuestions.execute();

        llLayout.findViewById(R.id.linLayout_quiz);
        return llLayout;
    }

    public void NextQuestion() {
        if (!Questions.AllQuestionsAnswered()) {
            mCurrentIndex = (mCurrentIndex + 1) % Questions.mQuestionBank.size();
            updateQuestion();
        }
        else {
            SharedPreferences pref = super.getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            int player1 = pref.getInt("player1Score", 0);
            int player2 = pref.getInt("player2Score", 0);
            if (player1 > player2) {
                Toast.makeText(super.getActivity(), "Player 1 wins!", Toast.LENGTH_SHORT)
                        .show();
                currentPlayerText.setText("Player 1 wins!");
            }
            else if (player2 > player1) {
                Toast.makeText(super.getActivity(), "Player 2 wins!", Toast.LENGTH_SHORT)
                        .show();
                currentPlayerText.setText("Player 2 wins!");
            }
            else {
                Toast.makeText(super.getActivity(), "It's a tie!", Toast.LENGTH_SHORT)
                        .show();
                currentPlayerText.setText("It's a tie!");
            }
            DisableButtons();
        }
    }

    private void DisableButtons() {
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
        mSkipButton.setEnabled(false);
        mCheatButton.setEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
    }

    public void QuestionsLoaded(JSONArray response) throws JSONException {
        int len = response.length();
        ArrayList<Question> questionsBank = new ArrayList<Question>();
        for (int i = 0; i < len; i++) {
            JSONObject question = response.getJSONObject(i);
            Boolean answer;
            Integer userAnswer = null;
            if (question.getInt("answer") == 0) {
                answer = false;
            }
            else {
                answer = true;
            }
            if (!question.isNull("userAnswer")) {
                userAnswer = question.getInt("userAnswer");
            }
            String questionText = question.getString("question");
            String explanationText = question.getString("explanation");
            questionsBank.add(i, new Question(questionText, answer, userAnswer, explanationText));
        }
        //mQuestionBank = questionsBank;
        Questions.mQuestionBank = questionsBank;
        Questions.questionsLoaded = false;
        updateQuestion();
    }

    public class DownloadQuestions extends AsyncTask<Void, Void, JSONArray>
    {
        @Override
        public JSONArray doInBackground(Void... params)
        {
            SharedPreferences pref = faActivity.getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            boolean loggedIn = pref.getBoolean("loggedIn", false);
            String str;
            /* This can be used if we want to store all user answers online
            if (loggedIn) {
                int userId = pref.getInt("userId", 0);
                str="http://mfcfund.ml/petiapp/GetQuestionsWithAnswers.php"+"?userId="+userId;
            }
            else {
                str="http://mfcfund.ml/petiapp/GetQuestions.php";
            }*/
            str="https://petiapp.000webhostapp.com/GetQuestions.php";
            URLConnection urlConn = null;
            BufferedReader bufferedReader = null;
            try
            {
                URL url = new URL(str);
                urlConn = url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                StringBuffer stringBuffer = new StringBuffer();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuffer.append(line);
                }

                return new JSONArray(stringBuffer.toString());
            }
            catch(Exception ex)
            {
                Log.e("App", "yourDataTask", ex);
                return null;
            }
            finally
            {
                if(bufferedReader != null)
                {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onPostExecute(JSONArray response)
        {
            if(response != null)
            {
                try {
                    QuestionsLoaded(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
