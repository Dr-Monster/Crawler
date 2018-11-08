package Entity;

import java.util.List;

/**
 * Created by 1 on 17-10-25.
 */
public class Book{
    int id ;
    String bookName ;
    String imgUrl ;
    String price ;
    List<BookReview> bookReviews ;

    @Override
    public String toString(){
        return "Book{" +
                "id=" + id +
                ", bookName='" + bookName + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", price='" + price + '\'' +
                ", bookReviews=" + bookReviews +
                '}';
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getBookName(){
        return bookName;
    }

    public void setBookName(String bookName){
        this.bookName = bookName;
    }

    public String getImgUrl(){
        return imgUrl;
    }

    public void setImgUrl(String imgUrl){
        this.imgUrl = imgUrl;
    }

    public String getPrice(){
        return price;
    }

    public void setPrice(String price){
        this.price = price;
    }

    public List<BookReview> getBookReviews(){
        return bookReviews;
    }

    public void setBookReviews(List<BookReview> bookReviews){
        this.bookReviews = bookReviews;
    }
}
