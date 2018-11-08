package Entity;

/**
 * Created by 1 on 17-10-25.
 */
public class BookReview{
    String rate ;
    String brief ;
    String comeFrom ;
    String time ;
    String bookFormat ;
    String purchase;
    String comment ;

    @Override
    public String toString(){
        return "BookReview{" +
                "rate='" + rate + '\'' +
                ", brief='" + brief + '\'' +
                ", comeFrom='" + comeFrom + '\'' +
                ", time='" + time + '\'' +
                ", bookFormat='" + bookFormat + '\'' +
                ", purchase='" + purchase + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    public String getRate(){
        return rate;
    }

    public void setRate(String rate){
        this.rate = rate;
    }

    public String getBrief(){
        return brief;
    }

    public void setBrief(String brief){
        this.brief = brief;
    }

    public String getComeFrom(){
        return comeFrom;
    }

    public void setComeFrom(String comeFrom){
        this.comeFrom = comeFrom;
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getBookFormat(){
        return bookFormat;
    }

    public void setBookFormat(String bookFormat){
        this.bookFormat = bookFormat;
    }

    public String getPurchase(){
        return purchase;
    }

    public void setPurchase(String purchase){
        this.purchase = purchase;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }
}
