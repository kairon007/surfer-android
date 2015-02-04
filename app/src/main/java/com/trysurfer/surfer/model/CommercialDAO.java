package com.trysurfer.surfer.model;

/**
 * Created by PRO on 10/9/2014.
 */
import java.io.Serializable;

public class CommercialDAO implements Serializable{

    private static final long serialVersionUID = 1L;

    private long id;
    private long commercial_id;
    private String picture;
    private String url;
    private byte[] pictureBitmap;

    public CommercialDAO(){

    }

    public CommercialDAO(long id, String picture, String url){
        setId(id);
        setPicture(picture);
        setUrl(url);
    }

    public CommercialDAO(long id, String picture, String url, byte[] pictureBitmap){
        setId(id);
        setPicture(picture);
        setUrl(url);
        setPictureBitmap(pictureBitmap);
    }

    public CommercialDAO(long id, long commercial_id, String picture, String url, byte[] pictureBitmap){
        setId(id);
        setCommercialId(commercial_id);
        setPicture(picture);
        setUrl(url);
        setPictureBitmap(pictureBitmap);
    }

    public long getId(){
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCommercialId(){
        return commercial_id;
    }

    public void setCommercialId(long commercial_id) {
        this.commercial_id = commercial_id;
    }

    public String getPicture(){
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getUrl(){
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getPictureBitmap(){
        return pictureBitmap;
    }

    public void setPictureBitmap(byte[] pictureBitmap){
        this.pictureBitmap = pictureBitmap;
    }

    @Override
    public String toString(){
        return "CommercialDAO [id=" + id + ", picture=" + picture + ", url=" + url + "]";
    }
}
