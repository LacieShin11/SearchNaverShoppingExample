package com.example.searchnavershoppingexample

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {
    private val clientId: String =  com.example.searchnavershoppingexample.id// 네이버 API 클라이언트 id
    private val clientSecret: String = com.example.searchnavershoppingexample.secret// 네이버 API 클라이언트 password

    val response: StringBuffer = StringBuffer()
    lateinit var searchText: String
    val display = 100 // 최대 검색 결과 개수

    lateinit var title: Array<String>
    lateinit var link: Array<String>
    lateinit var image: Array<String>

    var resultList = arrayListOf<ResultItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        result_list_recyclerview.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        result_list_recyclerview.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        title = Array<String>(display, {"내용 없음"})
        link = Array<String>(display, {"내용 없음"})
        image = Array<String>(display, {"내용 없음"})

        // 검색 버튼 눌렀을 때
        search_imagebutton.setOnClickListener {
            searchText = search_text_edittext.getText().toString()

            val searchTextExample = searchText.trim()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(search_text_edittext.windowToken, 0)

            if (searchTextExample.length > 0) {
                val thread = ThreadClass()
                thread.start()
            } else { // 사용자가 아무 내용을 입력하지 않았다면
                Toast.makeText(this, "검색어를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ThreadClass : Thread() {
        override fun run() {
            try {
                getResult(searchText)
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    fun getResult(vararg searchText: String): String {
        try {
            Log.d("searchText : ", searchText[0])

            val text: String = URLEncoder.encode(searchText[0], "UTF-8")
            val apiURL = "https://openapi.naver.com/v1/search/shop?query=" + text + "&display=" + display

            val url: URL = URL(apiURL + "query=" + text)
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("X-Naver-Client-Id", clientId)
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret)
            con.connect()

            val responseCode: Int = con.responseCode
            val br: BufferedReader
            if (responseCode == 200) { // 정상 호출
                br = BufferedReader(InputStreamReader(con.inputStream))
            } else { // 에러 발생
                br = BufferedReader(InputStreamReader(con.errorStream))
            }

            var inputLine: String? = null
            val response: StringBuilder = StringBuilder()
            do {
                inputLine = br.readLine()
                response.append(inputLine + "\n")
            } while (inputLine != null)

            var data: String = response.toString()

            Log.d("response 결과 : ", data)

            var array: List<String> = data.split("\n\"")

            var itemIndex: Int = 0

            if (array[4] == "display\": 0,") { // 검색 결과가 존재하지 않을 때
                val mHandler = Handler(Looper.getMainLooper())

                mHandler.postDelayed(Runnable {
                    Toast.makeText(this, "일치하는 상품이 없습니다.", Toast.LENGTH_SHORT).show()
                }, 0)
            } else { // 검색 결과가 존재할 때
                for (i in array.indices) {
                    Log.d("for문 i = ", i.toString() + " array[i] = " + array[i])
                    if (array[i].length > 3) {
                        if (array[i].substring(0, 5) == "title") {
                            title[itemIndex] = array[i].substring(9, array[i].length - 2)
                            Log.d("title : ", array[i].substring(9, array[i].length - 2))
                        }
                        if (array[i].substring(0, 4) == "link") {
                            link[itemIndex] = array[i].substring(8, array[i].length - 2)
                            Log.d("link : ", array[i].substring(8, array[i].length - 2))
                        }
                        if (array[i].substring(0, 5) == "image") {
                            image[itemIndex] = array[i].substring(9, array[i].length - 2)
                            Log.d("image : ", array[i].substring(9, array[i].length - 2))
                            itemIndex++
                        }
                    }
                }

                resultList.clear()

                for (i in 0 until itemIndex) {
                    // 검색어를 둘러싼 html 태그를 제거한 후 추가
                    resultList.add(
                        ResultItem(
                            Html.fromHtml(title[i]).toString(),
                            Html.fromHtml(link[i]).toString(),
                            Html.fromHtml(image[i]).toString()
                        )
                    )
                }

                val adapter = ResultListRecyclerViewAdapter(resultList)

                runOnUiThread(Runnable { result_list_recyclerview.adapter = adapter })
            }

            br.close()
            con.disconnect()

        } catch (e: Exception) {
            println(e)
        }

        return response.toString()
    }
}
