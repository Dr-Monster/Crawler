package Program;

import Entity.Book;
import Entity.BookReview;
import Tools.DB_con;
import Tools.FileSave;
import Tools.ImageSave;
import Tools.SaveToDB;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by 1 on 17-10-24.
 */
public class UrlAnalyze{

    public static void main(String[] args){
        try{
            new UrlAnalyze().missionStart();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void missionStart() throws Exception{
        DB_con dbCon = new DB_con();
        List<Book> books = new ArrayList<>();
        String sql = "select id , title from eb_book where title != " +
                "' '";
        PreparedStatement ps = dbCon.getPs(sql);
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
        List<Book> bookList = new ArrayList<>();
        Thread thread = new Thread(){
            @Override
            public void run(){
                super.run();
                for(int i = 0; i < books.size(); i++){
                    Book book = books.get(i);
                    String bookName = book.getBookName();
                    int id = book.getId();
                    System.err.println(id);
                    String strTrans = bookName.replace("\"", "").replace("\'", "")
                            .replace(" ", "+");
                    if(strTrans.startsWith("+")){
                        strTrans = strTrans.substring(1);
                    }
                    int timeA = new Random().nextInt(6) + 1;
                    try{
                        Thread.sleep(1000 * timeA);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    String url = "https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Dstripbooks&field-keywords=" + strTrans;
                    Map<String,String> map = getImageUrl(url);
                    String code = map.get("Code");
                    book.setBookName(bookName);
                    if(code != null){
                        String imageUrl = map.get("ImageUrl");
                        System.out.println(imageUrl);
                        String price = getPrice(code);
                        List<BookReview> reviews = getReview(strTrans, code);
                        book.setImgUrl(imageUrl);
                        book.setPrice(price);
                        book.setBookReviews(reviews);
                        if(imageUrl != null){
                            int timeB = new Random().nextInt(10) + 1;
                            try{
                                Thread.sleep(timeB * 1000);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }
                            saveImage(imageUrl, id + " " + bookName.replace("\"", "").replace("\'", ""));
                        }
                    }
                    new SaveToDB().save(book);
                    bookList.add(book);
                }
            }
        };
        thread.start();
    }


    public org.jsoup.nodes.Document getDocument(String url){
        org.jsoup.nodes.Document document = null;
        try{
            document = Jsoup.connect(url).timeout(1000*60*5).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31").get();
        }catch(Exception e){
            e.printStackTrace();
        }
        return document;
    }

    public Map<String,String> getImageUrl(String url){
        System.err.println(url + "\n");
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
        return map;
    }


    public String getPrice(String code){
        String url = "https://www.amazon.com/gp/offer-listing/" + code + "/ref=olp_f_new?ie=UTF8&f_all=true&f_new=true";
        Document document = getDocument(url);
        Elements prices = document.getAllElements().select("span").select("[class = a-size-large a-color-price olpOfferPrice a-text-bold]");
        String priceStr = null;
        if(prices.first() != null){
            Element price = prices.first();
            priceStr = price.text();
        }
        return priceStr;
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

    public List<BookReview> getReview(String name, String code){
        String url = "https://www.amazon.com/" +
                name + "/product-reviews/" +
                code +
                "/ref=cm_cr_dp_d_show_all_btm?ie=UTF8&reviewerType=all_reviews";
        Document document = getDocument(url);
        List<BookReview> reviews = new ArrayList<>();
        if(document != null){
            Elements elements = document.getAllElements().select("span").select("[data-hook = rating-out-of-text]").select("[class=arp-rating-out-of-text]");
            //get rate
            Element rateEle = elements.first();
            String rateStr = "0.0 out of 5 stars";
            if(rateEle != null){
                rateStr = rateEle.html();
            }
            //comment pages flag
            Elements pageEle = document.getAllElements().select("div#cm_cr-pagination_bar");

            //if rate is not equals zero means it has comments
            if("0.0 out of 5 stars".equals(rateStr)){
            }else{
                if(pageEle == null){
                    Elements reviewEles = document.getAllElements().select("div").select("[class=a-section celwidget]");
                    for(int i = 0; i < reviewEles.size(); i++){
                        Element detail = reviewEles.get(i);
                        BookReview bookReview = getCommentDetail(detail);
                        reviews.add(bookReview);
                    }
                }else{

                    //when it has page-flag it means comment more than ten , so still move to next page
                    Elements pageButs = document.getAllElements().select("li").select("[class=page-button]");
                    int lastPage = 1;
                    if(pageButs.last() != null){
                        lastPage = Integer.valueOf(pageButs.last().text());
                    }
                    for(int i = 0; i < lastPage; i++){
                        String pUrl = "https://www.amazon.com/" + name + "/product-reviews/" +
                                code + "/ref=cm_cr_getr_d_paging_btm_" + i + "?ie=UTF8&pageNumber=" + i +
                                "&reviewerType=all_reviews";
                        int timeC = new Random().nextInt(15) + 1;
                        try{
                            Thread.sleep(timeC * 1000);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                        Document newDocu = getDocument(pUrl);
                        if(newDocu != null){
                            Elements reviewEles = newDocu.getAllElements().select("div").select("[class=a-section celwidget]");
                            for(int j = 0; j < reviewEles.size(); j++){
                                Element detail = reviewEles.get(j);
                                BookReview bookReview = getCommentDetail(detail);
                                reviews.add(bookReview);
                            }
                        }
                    }
                }
            }
        }
        return reviews;
    }

    public void saveImage(String urlString, String f_Name){
//        String urlString = "https://images-na.ssl-images-amazon.com/images/I/41TpqGzb8lL._AC_US436_FMwebp_QL65_.jpg";
//        String f_Name = "Test1.jpg";
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
        File storeFile = new File("F:\\image\\" + f_Name + ".jpg");
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
        get.releaseConnection();
    }
}
