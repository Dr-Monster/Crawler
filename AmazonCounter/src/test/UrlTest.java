import Entity.Book;
import Entity.BookReview;
import Tools.DB_con;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 1 on 17-10-21.
 */
public class UrlTest{
    @Test
    public void getReview(){
        String url = "https://www.amazon.com/%C2%A1Mi-Raza-Primero-People-First/product-reviews/0520230183/ref=cm_cr_dp_d_show_all_btm?ie=UTF8&reviewerType=all_reviews";
        Document document = getDocument(url);
        Elements elements = document.getAllElements().select("span").select("[data-hook = rating-out-of-text]").select("[class=arp-rating-out-of-text]");
        //get rate
        Element rateEle = elements.first();
        String rateStr = rateEle.html();
        System.out.println(rateStr);
        Elements reviewEles = null;
        //comment pages flag
        Elements pageEle = document.getAllElements().select("div#cm_cr-pagination_bar");
        List<BookReview> reviews = new ArrayList<>();
        //if rate is not equals zero means it has comments
        if("0.0 out of 5 stars".equals(rateStr)){
            System.out.println("Zero");
        }else{
            if(pageEle == null){
                reviewEles = document.getAllElements().select("div").select("[class=a-section celwidget]");
                for(int i = 0; i < reviewEles.size(); i++){
                    Element detail = reviewEles.get(i);
                    BookReview bookReview = commentDetail(detail);
                    reviews.add(bookReview);
                }
            }else{
                //when it has page-flag it means comment more than ten , so still move to next page
                Elements pageButs = document.getAllElements().select("li").select("[class=page-button]");
                int lastPage = 1;
                if(pageButs.last() != null){
                    lastPage = Integer.valueOf(pageButs.last().text());
                }
                System.out.println(lastPage);
                for(int i = 0; i < lastPage; i++){
                    String pUrl = "https://www.amazon.com/" + "¡Mi-Raza-Primero-People-First" + "/product-reviews/" +
                            "0520230183" + "/ref=cm_cr_getr_d_paging_btm_" + i + "?ie=UTF8&pageNumber=" + i +
                            "&reviewerType=all_reviews";
                    Document newDocu = getDocument(pUrl);
                    reviewEles = newDocu.getAllElements().select("div").select("[class=a-section celwidget]");
                    for(int j = 0; j < reviewEles.size(); j++){
                        System.out.println(i + "," + j);
                        Element detail = reviewEles.get(j);
                        BookReview bookReview = commentDetail(detail);
                        reviews.add(bookReview);
                    }
                }
            }
        }
        System.out.println("WTF");
    }

    public org.jsoup.nodes.Document getDocument(String url){
        Map<String , String> cookies = new HashMap<>();
        cookies.put("s_dslv" , "1508555071598");
        cookies.put("s_nr" , "1508555071597-New");
        cookies.put("s_vnum" , "1940555065967%26vn%3D1");
        cookies.put("session-id" , "133-4957267-7603027");
        cookies.put("session-id-time" , "2082787201l");
        cookies.put("session-token" , "gJOzOOzaJQu5rWQKOPspIUVT5KuiiKgPFlJxXE0lHD3HybFe6FHQ3UrOIImCiJ0BAJ3OU+IGsAz/w87RxZsdnHTy49qy8GGlFgSJaDwJu1KoR9y5qANRDkesxjL284lf00rwmV+CBY65KpkGdaV3c5KRWBamDyMUYGJYBomlfEMIN9UJYlYLBjsd3MjINox1HUalL0emVP6vJ8Pe3c7Qw7FWPRZsiGAaCEbn78hBUoJS2ZvVgxhNNS1/6aQU2Imb");
        cookies.put("skin" , "noskin");
        cookies.put("ubid-main" , "134-4459652-1450121");
        cookies.put("x-wl-uid" , "1Yrmp16sLTzHL4E4xFhJ5OFhpW4aTmY7E5hztI9/K2A3omc+xwYcjM72rkNKp+XCGAvTrMXoIFUE=");
        cookies.put("s_vi" , "[CS]v1|2CF55C9D852ABF1C-40000301A00000B9[CE]");
        org.jsoup.nodes.Document document = null;
        try{
            document = Jsoup.connect(url).get();
        }catch(Exception e){
            e.printStackTrace();
        }
        return document;
    }


    public BookReview commentDetail(Element detail){
        BookReview bookReview = new BookReview();

        Elements rate = detail.select("span").select("[class=a-icon-alt]");
        if(rate.first() != null){
            String rateStr = rate.first().html();
            bookReview.setRate(rateStr);
        }

        Elements shortComs = detail.select("a").select("[data-hook=review-title]");
        if(shortComs.first() != null){
            String shortComStr = shortComs.first().html();
            bookReview.setBrief(shortComStr);
        }

        Elements comeFroms = detail.select("span").select("[data-hook=review-author]");
        if(comeFroms.first() != null){
            String comeFromStr = comeFroms.first().text().replace("By", "By ");
            bookReview.setComeFrom(comeFromStr);
        }

        Elements time = detail.select("span").select("[data-hook=review-date]");
        if(time.first() != null){
            String timeStr = time.first().html();
            bookReview.setTime(timeStr);
        }


        Elements formats = detail.select("a").select("[data-hook=format-strip]");
        if(formats.first() != null){
            String formatStr = formats.first().html();
            bookReview.setBookFormat(formatStr);
        }


        Elements purchases = detail.select("span").select("[data-hook=avp-badge]");
        if(purchases.first() != null){
            String purchaseStr = purchases.first().html();
            bookReview.setPurchase(purchaseStr);
        }

        Elements comments = detail.select("span").select("[data-hook=review-body]");
        if(comments.first() != null){
            String commentStr = comments.first().text();
            bookReview.setComment(commentStr);
        }

        return bookReview;
    }


    @Test
    public void saveImage(){
        String urlString = "https://images-na.ssl-images-amazon.com/images/I/41TpqGzb8lL._AC_US436_FMwebp_QL65_.jpg";
        String f_Name = "Test1.jpg";
        HttpClient client = new HttpClient();
        GetMethod get = new GetMethod(urlString);
        get.setRequestHeader("Accept" , "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        get.setRequestHeader("Accept-Encoding" , "gzip, deflate, sdch");
        get.setRequestHeader("Accept-Language" , "zh-CN,zh;q=0.8");
        get.setRequestHeader("Cache-Control" , "no-cache");
        get.setRequestHeader("Connection" , "keep-alive");
        get.setRequestHeader("Host" , "images-na.ssl-images-amazon.com");
        get.setRequestHeader("Pragma" , "no-cache");
        get.setRequestHeader("Upgrade-Insecure-Requests" , "1");
        get.setRequestHeader("User-Agent" , "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36");

        try{
            client.executeMethod(get);
        }catch(IOException e){
            e.printStackTrace();
        }
        File storeFile = new File("F:\\image\\" + f_Name);
        FileOutputStream output = null;
        try{
            output = new FileOutputStream(storeFile);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        //得到网络资源的字节数组,并写入文件
        try{
            output.write(get.getResponseBody());
        }catch(IOException e){
            e.printStackTrace();
        }
        try{
            output.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("WTF");
    }

    @Test
    public void dbTest(){
        DB_con db_con = new DB_con();
        List<Book> books = new ArrayList<>();
        String sql = "select id , title from eb_book where title != " +
                "' '";
        PreparedStatement ps = db_con.getPs(sql);
        ResultSet set = null;
        try{
            set = ps.executeQuery();
            while(set.next()){
                Book book = new Book();
                int id = set.getInt(1);
                String title = set.getString(2);
                book.setId(id);
                book.setBookName(title);
                books.add(book);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        System.out.println("WTF");
    }

    @Test
    public void imgTest(){
        String url = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Dstripbooks&field-keywords=Bright+Boys";
        org.jsoup.nodes.Document document = getDocument(url);
        String nameUrl = "";
        String imageUrl = "";
        String code = "";
        Element check = document.getAllElements().select("h1#noResultsTitle").first();
        Map<String,String> map = new HashMap<>();

        Element result = document.getAllElements().select("a").first();

        if(check != null){
            nameUrl = "no result";
        }else{
            Element result1 = document.getAllElements().select("[class=a-link-normal s-access-detail-page  s-color-twister-title-link a-text-normal]").first();
            nameUrl = result1.attr("href");
            Elements image = document.getAllElements().select("img").select("[alt]").select("[alt=Product Details]").select("[srcset]");
            Element imageEle = image.first();
            String imageStr = imageEle.attr("srcset");
            String[] strings = imageStr.split(" ");
            imageUrl = strings[strings.length - 2];
            code = nameUrl.substring(nameUrl.indexOf("dp/") + 3, nameUrl.indexOf("/ref"));
            map.put("NameUrl", nameUrl);
            map.put("ImageUrl", imageUrl);
            map.put("Code", code);
        }
        System.out.println("WTF");
    }


    @Test
    public void getImageUrl(){
        String url = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Dstripbooks&field-keywords=Travelling+in+a+Palimpsest:+Finnish+Nineteenth+Century+Painters+Encounters+with+Spanish+Art+and+Culture";
        org.jsoup.nodes.Document document = getDocument(url);
        String nameUrl = "";
        String imageUrl = "";
        String code = "";
        Element check = document.getAllElements().select("h1#noResultsTitle").first();
        Elements image = null ;
        Map<String,String> map = new HashMap<>();
        if(check != null){
            nameUrl = "no result";
        }else{
            Element result = document.getAllElements().select("a").select("[class=a-link-normal s-access-detail-page  s-color-twister-title-link a-text-normal]").first();
            nameUrl = result.attr("href");
            image = document.getAllElements().select("img").select("[alt]").select("[alt=Product Details]").select("[srcset]");
            if(image.size() != 0){
                Element imageEle = image.first();
                String imageStr = imageEle.attr("srcset");
                String[] strings = imageStr.split(" ");
                imageUrl = strings[strings.length - 2];
            }else{
                image = document.getAllElements().select("img").select("[alt=Product Details]").select("[class=s-access-image cfMarker]").select("[data-search-image-load]");
                Element imageEle = image.first();
                String imageStr = imageEle.attr("src");
                imageUrl = imageStr ;
            }
            code = nameUrl.substring(nameUrl.indexOf("dp/") + 3, nameUrl.indexOf("/ref"));
            map.put("NameUrl", nameUrl);
            map.put("ImageUrl", imageUrl);
            map.put("Code", code);
        }
        System.out.println("WTF");
    }

    @Test
    public void commentTest(){
        String url = "https://www.amazon.com/Position+Location+Techniques+and+Applications/product-reviews/0123743532/ref=cm_cr_getr_d_paging_btm_1?ie=UTF8&pageNumber=1&reviewerType=all_reviews";
        Document document = getDocument(url);
        Elements elements = document.getAllElements().select("div").select("[class=a-section celwidget]");
        List<BookReview> reviews = new ArrayList<>();
        for(int j = 0; j < elements.size(); j++){
            Element detail = elements.get(j);
            BookReview bookReview = getCommentDetail(detail);
            reviews.add(bookReview);
        }
        System.out.println("WTF");
    }

    public BookReview getCommentDetail(Element detail){
        BookReview bookReview = new BookReview();

        Elements rate = detail.select("span").select("[class=a-icon-alt]");
        if(rate.first() != null){
            String rateStr = rate.first().html();
            bookReview.setRate(rateStr);
        }

        Elements shortComs = detail.select("a").select("[data-hook=review-title]");
        if(shortComs.first() != null){
            String shortComStr = shortComs.first().html();
            bookReview.setBrief(shortComStr);
        }

        Elements comeFroms = detail.select("span").select("[data-hook=review-author]");
        if(comeFroms.first() != null){
            String comeFromStr = comeFroms.first().text().replace("By", "By ");
            bookReview.setComeFrom(comeFromStr);
        }

        Elements time = detail.select("span").select("[data-hook=review-date]");
        if(time.first() != null){
            String timeStr = time.first().html();
            bookReview.setTime(timeStr);
        }


        Elements formats = detail.select("a").select("[data-hook=format-strip]");
        if(formats.first() != null){
            String formatStr = formats.first().html();
            bookReview.setBookFormat(formatStr);
        }


        Elements purchases = detail.select("span").select("[data-hook=avp-badge]");
        if(purchases.first() != null){
            String purchaseStr = purchases.first().html();
            bookReview.setPurchase(purchaseStr);
        }

        Elements comments = detail.select("span").select("[data-hook=review-body]");
        if(comments.first() != null){
            String commentStr = comments.first().text();
            bookReview.setComment(commentStr);
        }

        return bookReview;
    }
}
