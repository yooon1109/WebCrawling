package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {

        // 크롤링할 웹페이지 URL 설정
        String url = "https://www.bbc.co.uk/learningenglish/korean/course/essential-english-korean";
        // 한글을 제외하는 패턴
        Pattern pattern = Pattern.compile("^[^\\uAC00-\\uD7AF]*$");

        try {
            // Jsoup을 사용하여 웹페이지 가져오기
            Document doc = Jsoup.connect(url).get();

            // 웹페이지에서 원하는 요소 추출하기 (예: 모든 링크)
            Elements links = doc.select("li.item-session a[href]");

            // 중복된 링크를 제거하기 위해 HashSet을 사용
            Set<String> newLinks = new LinkedHashSet<>();
            for(Element link: links){
                newLinks.add(link.attr("href"));
            }
            // 추출된 링크들 출력하기
            for (String link : newLinks) {
                System.out.println("링크: https://www.bbc.co.uk" + link);
                String href = "https://www.bbc.co.uk" + link;
                try {
                    // 링크로 이동하여 HTML 내용 가져오기
                    Document linkDoc = Jsoup.connect(href).get();
                    // <div> 요소 중 클래스가 "text"인 요소의 <p> 태그 선택
                    Elements paragraphs = linkDoc.select("div.widget.widget-richtext.hide div.text p");
                    // 모든 p 태그에 대해 영어로만 이루어진 전체 문장이 포함된 태그 추출
                    for (Element paragraph : paragraphs) {

                        // <strong> 태그 내의 텍스트 제외
                        paragraph.select("strong").remove();
                        // <br> 태그 제거
                        paragraph.select("br").remove();
                        // p 태그 내의 텍스트 추출
                        String text = paragraph.text().trim();

                        // 추출된 텍스트가 영어로만 이루어진 전체 문장인지 판별
                        Matcher matcher = pattern.matcher(text);
                        if (matcher.matches()) {
                            // 영어로만 이루어진 전체 문장이 포함된 p 태그 출력
                            System.out.println(text);
                        }
                    }

//                    // 선택된 첫 번째 문단 출력
//                    System.out.println("첫 번째 문단: " + paragraph.text());

                }catch (IOException e){
                    System.out.println("권한이 없거나 존재하지 않는 페이지입니다.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // PDF 파일 다운로드 메서드
    private static void downloadPDF(String pdfUrl) {
        try {
            // PDF 파일 URL 생성
            URL url = new URL(pdfUrl);

            // HTTP 연결 생성
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // HTTP 연결이 정상적으로 이루어지면
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);

                // PDF 파일을 저장할 경로
                String saveFilePath = "C:\\Users\\jys_1\\IdeaProjects\\WebCrawling\\src\\main\\java\\org\\example\\downloads\\" + fileName;

                // 파일 스트림 생성
                InputStream inputStream = httpConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                // 파일 다운로드
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // 자원 정리
                outputStream.close();
                inputStream.close();

                System.out.println("PDF 파일 다운로드 완료: " + saveFilePath);
            } else {
                System.out.println("PDF 파일 다운로드 실패. 응답 코드: " + responseCode);
            }
            // 연결 해제
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}