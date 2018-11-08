package Tools;

import Entity.Book;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 1 on 17-10-27.
 */
public class SaveToDB{
    public void save(Book book){
        DB_con db_con = new DB_con();
        String checkSql = "select id from bookinfo";
        PreparedStatement checkPs = db_con.getPs(checkSql);
        List<Integer> bidList = new ArrayList<>();
        try{
            ResultSet set = checkPs.executeQuery();
            while(set.next()){
                int id = set.getInt(1);
                bidList.add(id);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }

        if(!bidList.contains(book.getId())){
            String sql = "insert into bookinfo (id , bookName , imgUrl , price , bookreview)" +
                    "values(? , ? , ? , ? , ?)";
            PreparedStatement ps = db_con.getPs(sql);
            try{
                ps.setInt(1 , book.getId());
                ps.setString(2 , book.getBookName());
                ps.setString(3 , book.getImgUrl());
                ps.setString(4 , book.getPrice());
                ps.setString(5 , String.valueOf(book.getBookReviews()));
                ps.execute();
                ps.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
    }
}
