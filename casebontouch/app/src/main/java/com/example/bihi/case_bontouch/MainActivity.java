package com.example.bihi.case_bontouch;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText searchEditText;
    private boolean finishedDownloadingWords = false;
    private List<String> mWordList = new ArrayList<>();
    private ArrayList<ArrayList<String>> mWordListOfLists = new ArrayList<>();
    private List<String> mSubstringList = new ArrayList<>();
    private String [] infoText = {"Downloading wordlist!", "Search word not found!", "No results"};
    WordListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadWordsTask downloadWordsTask = new DownloadWordsTask();
        downloadWordsTask.execute();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        adapter = new WordListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.editText);

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String searchString = s.toString().trim();

                if(finishedDownloadingWords){
                    SubstringSearch substringSearch = new SubstringSearch();
                    if(!searchString.isEmpty() && isValidText(searchString)){
                        substringSearch.execute(searchString);
                    } else {
                        adapter.setWords(Arrays.asList(infoText[2]));
                    }
                } else {
                    adapter.setWords(Arrays.asList(infoText[0]));
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private class DownloadWordsTask extends AsyncTask<URL, Void, ArrayList<ArrayList<String>> > {
        protected ArrayList<ArrayList<String>> doInBackground(URL... urls){ return downloadRemoteTextFileContent(); }
        protected void onPostExecute(ArrayList<ArrayList<String>> wordList){
            mWordListOfLists = wordList;
            finishedDownloadingWords = true;
        }
    }

    private ArrayList<ArrayList<String>> downloadRemoteTextFileContent(){
        URL mUrl = null;
        ArrayList<String> wordList = new ArrayList<>();
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        ArrayList<ArrayList<String>> wordListOfLists = new ArrayList<>(alphabet.length);

        try{
            mUrl = new URL("http://runeberg.org/words/ss100.txt");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try{
            assert mUrl != null;
            URLConnection connection = mUrl.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            int i = 0;
            while((line = br.readLine()) != null){
                if (i < alphabet.length){
                    if(line.toLowerCase().charAt(0) == alphabet[i]) {
                        wordList.add(line);
                    } else{
                        wordListOfLists.add(wordList);
                        wordList = new ArrayList<>();
                        i++;
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wordListOfLists;
    }

    private class SubstringSearch extends AsyncTask<String, String, List<String> > {
        protected List<String> doInBackground(String... strings){ return substringSearch(strings[0]); }
        protected void onPostExecute(List<String> substringList){
            mSubstringList = substringList;
            if(mSubstringList.size() > 0){
                adapter.setWords(mSubstringList);
            }else{
                adapter.setWords(Arrays.asList(infoText[2]));
            }
        }
    }

    public List<String> substringSearch(String searchString) {
        List<String> substringList = new ArrayList<>();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        int index = alphabet.indexOf(searchString.toLowerCase().charAt(0));
        ArrayList<String> rowWordList = new ArrayList<>(mWordListOfLists.get(index));
        for (String word : rowWordList) {
            if (word.toLowerCase().startsWith(searchString.toLowerCase())) {
                substringList.add(word);
            }
        }
        return substringList;
    }

    private boolean isValidText(String text) {
        String TXT_PATTERN = "[a-zA-Z]+";
        Pattern pattern = Pattern.compile(TXT_PATTERN);
        Matcher matcher = pattern.matcher(text);
        return matcher.matches();
    }
}